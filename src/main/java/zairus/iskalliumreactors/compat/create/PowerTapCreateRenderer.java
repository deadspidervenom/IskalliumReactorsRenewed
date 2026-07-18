package zairus.iskalliumreactors.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import zairus.iskalliumreactors.IRConfig;

/**
 * Vanilla-renderer fallback for the Create-compat Power Tap, used only when
 * Flywheel visualization isn't active for this level (see the early return
 * below) - the normal case is handled instead by {@link PowerTapVisual}.
 *
 * This mirrors Create's own {@code GearboxRenderer} almost exactly - same
 * base class, same {@code CachedBuffers.partialFacing}/{@code kineticRotationTransform}
 * utility calls Create itself uses for every kinetic block, just looping over
 * this tap's runtime {@code visibleFaces} bitmask instead of Gearbox's fixed
 * "every face except my AXIS" rule. Deliberately does NOT re-derive quads,
 * UVs, or transforms by hand (an earlier version of this class did, using
 * BakedModel#getQuads - see git history) - CachedBuffers.partialFacing already
 * does exactly that, correctly, for any Create partial model, which is what
 * broke down before.
 */
public class PowerTapCreateRenderer extends KineticBlockEntityRenderer<BlockEntityIRPowerTapCreate>
{
    // How far each stub is nudged out past the tap's own face, in block units.
    // Kept in sync with PowerTapVisual's own NUDGE constant so both rendering
    // paths look identical. SHAFT_HALF's geometry runs flush from the block
    // center out to the face - coplanar with (and z-fighting against) the
    // tap's own cube there - so this pushes it just past that face instead.
    private static final float NUDGE = 0.002f;

    public PowerTapCreateRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected void renderSafe(BlockEntityIRPowerTapCreate be, float partialTicks, PoseStack ms,
                               MultiBufferSource buffer, int light, int overlay)
    {
        // Flywheel handles rendering entirely on its own when active - see
        // KineticBlockEntityRenderer#renderSafe / GearboxRenderer for the same
        // guard on every one of Create's own kinetic blocks. Only reachable at
        // all when Flywheel's backend is OFF (or unsupported) for this level.
        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        if (!IRConfig.createPowertapShaftRender)
            return;

        byte visibleFaces = be.getVisibleShaftFaces();
        if (visibleFaces == 0)
            return;

        BlockPos pos = be.getBlockPos();

        for (Direction dir : Direction.values())
        {
            if ((visibleFaces & (1 << dir.ordinal())) == 0)
                continue;

            Axis axis = dir.getAxis();

            // Same reasoning as PowerTapVisual#updateLight: `light` was computed
            // by the renderer dispatcher for the tap's own block position, but
            // each stub visually sits in the neighboring block it pokes into -
            // sampling light there instead keeps a tap embedded in something
            // dark (e.g. reactor casing) from darkening stubs that face out into
            // well-lit space.
            int faceLight = LevelRenderer.getLightColor(be.getLevel(), pos.relative(dir));

            SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), dir);
            float angle = getAngleForBe(be, pos, axis);

            kineticRotationTransform(shaft, be, axis, angle, faceLight);
            // Nudge is along the same axis rotateCentered above just rotated around,
            // so it's unaffected by that rotation - safe to apply after.
            shaft.translate(dir.getStepX() * NUDGE, dir.getStepY() * NUDGE, dir.getStepZ() * NUDGE);
            shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }
}
