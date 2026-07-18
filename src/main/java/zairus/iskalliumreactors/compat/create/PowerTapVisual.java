package zairus.iskalliumreactors.compat.create;

import java.util.EnumMap;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.SectionTrackedVisual.SectionCollector;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.model.Models;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;

/**
 * Flywheel visual for the Create-compat Power Tap.
 *
 * This is the instanced-rendering counterpart to {@link PowerTapCreateRenderer}'s
 * vanilla fallback, and both deliberately mirror Create's own
 * {@code com.simibubi.create.content.kinetics.gearbox.GearboxVisual} /
 * {@code GearboxRenderer} pair - the Gearbox is Create's own "one block, several
 * shaft stubs sticking out of several faces" block, which is exactly this tap's
 * shape. The only real difference is *which* faces get a stub: Gearbox decides
 * with a fixed "not my AXIS" rule, this tap decides with the runtime
 * {@link BlockEntityIRPowerTapCreate#getVisibleShaftFaces()} bitmask (which can
 * change as neighboring blocks are placed/broken), so instances are created and
 * removed on demand instead of once at construction.
 *
 * Each stub is a real, unmodified copy of Create's own
 * {@link AllPartialModels#SHAFT_HALF} partial model (the same piece used by
 * Gearbox) - a half-block-length shaft authored to sit flush against a face and
 * meet a neighboring shaft at the block boundary. Reusing it outright means
 * correct texturing/lighting/shading for free and a seamless visual join with
 * whatever Create shaft the player attaches, without any manual UV or geometry
 * math.
 */
public class PowerTapVisual extends KineticBlockEntityVisual<BlockEntityIRPowerTapCreate> {

	// How far each stub is nudged out past the tap's own face, in block units.
	// Kept in sync with PowerTapCreateRenderer's own NUDGE constant so both
	// rendering paths look identical.
	private static final float NUDGE = 0.002f;

	private final EnumMap<Direction, RotatingInstance> stubs = new EnumMap<>(Direction.class);
	private byte lastVisibleFaces = 0;

	public PowerTapVisual(VisualizationContext context, BlockEntityIRPowerTapCreate blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		rebuildStubs();
	}

	/**
	 * Adds/removes stub instances so `stubs` matches the block entity's current
	 * visibleFaces bitmask. Instancers must be re-fetched from
	 * instancerProvider() every time we need one (they're not safe to cache
	 * across frames), so this always asks fresh rather than storing one on the
	 * class - see {@code InstancerProvider#instancer}'s own documentation.
	 */
	private void rebuildStubs() {
		byte visibleFaces = blockEntity.getVisibleShaftFaces();
		lastVisibleFaces = visibleFaces;

		for (Direction dir : Direction.values()) {
			boolean shouldShow = (visibleFaces & (1 << dir.ordinal())) != 0;
			RotatingInstance existing = stubs.get(dir);

			if (shouldShow && existing == null) {
				Instancer<RotatingInstance> instancer = instancerProvider()
					.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));

				RotatingInstance instance = instancer.createInstance();
				instance.setup(blockEntity, dir.getAxis())
					.setPosition(getVisualPosition())
					.rotateToFace(Direction.SOUTH, dir)
					// SHAFT_HALF's own geometry runs flush from the block center out to
					// the face - coplanar with the tap's own cube there, which z-fights
					// against it. Nudging the whole instance a hair further out along
					// the face's own normal moves the coplanar surface just past the
					// tap's face without visibly detaching it from the block.
					.nudge(dir.getStepX() * NUDGE, dir.getStepY() * NUDGE, dir.getStepZ() * NUDGE)
					.setChanged();

				stubs.put(dir, instance);

				// Fixes a lighting bug: a brand new instance only ever gets its light
				// sampled reactively, the next time the framework notices one of our
				// tracked sections change (see setSectionCollector/updateLight below).
				// Nothing forces that to happen right now, so without this the stub
				// renders pitch black - regardless of the actual light level - until
				// some unrelated nearby block update happens to trigger a light-section
				// refresh. Sampling it immediately here means a freshly placed/rebuilt
				// stub is correctly lit from the very first frame it exists.
				relight(pos.relative(dir), instance);
			} else if (!shouldShow && existing != null) {
				existing.delete();
				stubs.remove(dir);
			}
		}
	}

	@Override
	public void update(float partialTick) {
		if (blockEntity.getVisibleShaftFaces() != lastVisibleFaces)
			rebuildStubs();

		for (var entry : stubs.entrySet())
			entry.getValue()
				.setup(blockEntity, entry.getKey().getAxis())
				.setChanged();
	}

	@Override
	public void setSectionCollector(SectionCollector collector) {
		// Each stub visually sits in a neighboring block's space (that's the
		// whole point - it's meant to look like it's poking out to meet a
		// neighboring shaft), so its light should track that neighbor's
		// section too, not just the tap's own. That only differs from the
		// tap's own section at a 16-block chunk-section boundary, but costs
		// nothing to just always include.
		this.lightSections = collector;

		LongSet sections = new LongOpenHashSet();
		sections.add(SectionPos.asLong(pos));
		for (Direction dir : Direction.values())
			sections.add(SectionPos.asLong(pos.relative(dir)));
		collector.sections(sections);
	}

	@Override
	public void updateLight(float partialTick) {
		// relight(FlatLit...)'s default samples light at the tap's own
		// position for every instance - fine for a stub that's flush with the
		// tap's own block, wrong for one that's visually poking out into a
		// neighboring block. A tap embedded in something solid (e.g. reactor
		// casing) has poor light at its own position, which used to darken
		// every stub uniformly even where the neighbor it points into is
		// well-lit. Sample each stub's own neighbor position instead.
		for (var entry : stubs.entrySet()) {
			BlockPos neighborPos = pos.relative(entry.getKey());
			relight(neighborPos, entry.getValue());
		}
	}

	@Override
	protected void _delete() {
		stubs.values().forEach(AbstractInstance::delete);
		stubs.clear();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		stubs.values().forEach(consumer);
	}
}
