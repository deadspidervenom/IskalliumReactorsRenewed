package zairus.iskalliumreactors.compat.create;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import zairus.iskalliumreactors.IRConfig;
import zairus.iskalliumreactors.block.ModBlocks;
import zairus.iskalliumreactors.blockentity.BlockEntityIRController;

import java.util.List;

/**
 * SU-generating variant of the Power Tap. Same reactor-output math as the FE
 * Power Tap (BlockEntityIRPowerTap), just fed into Create's kinetic network
 * instead of pushed through an energy capability - which is why there's no
 * energy buffer, capacity, push-to-neighbors, or GUI here at all: Create's
 * stress network is an instantaneous "how much is available right now" model,
 * not a storable buffer like FE, so most of the FE variant's bookkeeping
 * simply doesn't apply here.
 *
 * Ratio: 1000 FE/t of reactor output = 800 SU delivered to the network,
 * i.e. SU = reactorYield * IRConfig.createSuPerFe (default 0.8). The shaft always
 * spins at IRConfig.createPowertapGeneratedRpm regardless of output; only the Stress
 * Capacity contributed changes with reactor output.
 */
public class BlockEntityIRPowerTapCreate extends GeneratingKineticBlockEntity
{
    private BlockEntityIRController controller;
    private int lastReactorYield = -1;

    /**
     * True once `controller` has been set at least once this "session" (i.e.
     * since this BE was constructed - freshly placed or freshly loaded from
     * disk). Guards against the load-time race described on tick() below.
     */
    private boolean controllerLinked = false;

    /** Bit i (matching Direction.values()[i].ordinal()) set = render+allow a shaft stub on that face. */
    private byte visibleFaces = 0;

    public BlockEntityIRPowerTapCreate(BlockPos pos, BlockState state)
    {
        super(CreateCompat.IR_POWER_TAP_CREATE.get(), pos, state);
    }

    public void setController(BlockEntityIRController c)
    {
        this.controller = c;
    }

    public byte getVisibleShaftFaces()
    {
        return this.visibleFaces;
    }

    /** Current visual spin angle in degrees, for a given partial tick - client-side render use only. */
    public float getShaftAngleDegrees(float partialTick)
    {
        if (level == null)
            return 0;

        float time = level.getGameTime() + partialTick;
        // Same scaling Create itself uses to turn "RPM" into degrees/tick (see
        // KineticBlockEntityRenderer.getAngleForBe in Create's own source).
        return (time * getSpeed() * 3f / 10f) % 360f;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours)
    {
        // No extra behaviours needed - just the base kinetic bookkeeping.
    }

    @Override
    public void tick()
    {
        super.tick();

        if (level == null || level.isClientSide)
            return;

        // Controller validity - mirrors BlockEntityIRPowerTap's check.
        if (controller != null
                && level.getBlockState(controller.getBlockPos()).getBlock() != ModBlocks.STEEL_CONTROLLER.get())
        {
            controller = null;
        }

        // Don't trust a null `controller` as "the reactor produces 0 output"
        // until we've actually been linked at least once. Right after a world
        // load, `controller` starts null and only becomes non-null again once
        // the reactor's controller BE re-links us during its own tick (see
        // BlockEntityIRController#tick -> checkStructure) - which may run
        // before or after this tick depending on chunk block-entity order.
        // Reading "null" as "0 output" here would force a spurious
        // real-speed -> 0 -> real-speed round trip through Create's kinetic
        // network on load, and that transient churn is what let Create's own
        // network-conflict safety checks (RotationPropagator#propagateNewSource,
        // GeneratingKineticBlockEntity#applyNewSpeed) destroy nearby shafts -
        // inconsistently, since it only bit whichever reactors/axes happened
        // to tick the tap before the controller. Simplest fix: just wait.
        if (controller == null && !controllerLinked)
        {
            recalculateVisibleFaces();
            return;
        }

        if (controller != null)
            controllerLinked = true;

        int reactorYield = (controller != null && controller.getIsValidReactor())
                ? controller.getCoreCount() * IRConfig.eachIskalliumBlockPowerValue
                : 0;

        if (reactorYield != lastReactorYield)
        {
            lastReactorYield = reactorYield;
            updateGeneratedRotation();
        }

        recalculateVisibleFaces();
    }

    @Override
    public void onLoad()
    {
        super.onLoad();

        // Minecraft finishes calling onLoad() on every block entity in a
        // chunk before that chunk's block entities begin ticking at all - a
        // real, guaranteed ordering, unlike "first tick," which just happens
        // to run in whatever order the chunk's block-entity list iterates in.
        // Doing the neighbor suppression here instead of on our own first
        // tick means we deterministically win the race against every
        // neighboring shaft's first tick within this same chunk, rather than
        // "usually" winning it.
        if (level != null && !level.isClientSide)
            suppressNearbyReattachOnLoad();
    }

    /**
     * Every Create KineticBlockEntity - not just this one - forces itself to
     * fully re-verify/re-broadcast its speed the first time it ticks after any
     * load (see the read() comment below for why). That's harmless for a
     * block sitting in isolation, but for a *loop* of shafts - which this tap
     * enables by design, connecting on all 6 faces instead of the usual 2 -
     * it means several independent shafts can all try to re-verify the same,
     * already-consistent network at once, in whatever order the chunk happens
     * to tick them in. This is the same "large Create systems sometimes hiccup
     * on map load" behavior Create players occasionally run into in general -
     * our tap just makes the hub/loop shape that triggers it much easier to
     * build than it'd otherwise be. If one shaft is compared against a
     * neighbor that hasn't re-verified yet and still reads as spinning
     * differently for that one moment, Create's own conflict safety check
     * pops it - which matches exactly what's being reported (the first couple
     * of blocks right next to the tap, inconsistently, depending on tick
     * order).
     *
     * We can't stop two *other* shafts from racing each other - that's inside
     * Create's own ShaftBlockEntity, not ours. What we *can* do is reach out
     * to the shafts immediately around us (out to the same "1-2 blocks"
     * radius being reported) as soon as we're loaded, and suppress their
     * pending auto-reattach too, the same way we suppress our own in read().
     * That leaves our own explicit updateGeneratedRotation() call (from
     * tick(), once the reactor's real state is known) as the only thing that
     * ever walks this immediate neighborhood on this load, instead of several
     * independent, uncoordinated ones colliding.
     *
     * Being called from onLoad() rather than tick() is what makes this
     * reliable within the same chunk (see onLoad()'s own comment above) - the
     * remaining gap is a neighbor in a chunk that hasn't finished loading yet,
     * which level.isLoaded() below simply skips (that neighbor will do its
     * own onLoad() in due course, unaffected by anything happening here).
     */
    private void suppressNearbyReattachOnLoad()
    {
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.List<BlockPos> frontier = new java.util.ArrayList<>();
        visited.add(worldPosition);
        frontier.add(worldPosition);

        for (int depth = 0; depth < 2; depth++)
        {
            java.util.List<BlockPos> next = new java.util.ArrayList<>();

            for (BlockPos p : frontier)
            {
                for (Direction dir : Direction.values())
                {
                    BlockPos neighbor = p.relative(dir);
                    if (!visited.add(neighbor))
                        continue;

                    if (!level.isLoaded(neighbor))
                        continue;

                    BlockEntity neighborBe = level.getBlockEntity(neighbor);
                    if (neighborBe instanceof KineticBlockEntity kbe)
                        kbe.updateSpeed = false;

                    next.add(neighbor);
                }
            }

            frontier = next;
        }
    }

    /**
     * A face gets a rendered shaft stub only if it's genuinely "exposed" - not
     * blocked by a neighboring block (which would just clip through it - that's
     * the "successful construction" case the block/Create shaft/etc already
     * visually represents the connection) and not facing into the reactor's own
     * interior (nothing to connect to in there, and it'd be a weird thing to see
     * poking into the core chamber).
     */
    private void recalculateVisibleFaces()
    {
        byte newFaces = 0;

        for (Direction dir : Direction.values())
        {
            BlockPos neighborPos = worldPosition.relative(dir);

            if (controller != null && controller.isWithinReactorBounds(neighborPos))
                continue;

            if (!level.getBlockState(neighborPos).isAir())
                continue;

            newFaces |= (byte) (1 << dir.ordinal());
        }

        if (newFaces != visibleFaces)
        {
            visibleFaces = newFaces;
            notifyUpdate();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket)
    {
        super.write(tag, registries, clientPacket);
        tag.putByte("VisibleFaces", visibleFaces);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket)
    {
        super.read(tag, registries, clientPacket);
        visibleFaces = tag.getByte("VisibleFaces");

        // KineticBlockEntity's constructor always sets updateSpeed = true, and
        // nothing in its own read() restores it to false from NBT - so on
        // *every* load, Create's own tick() (called via our super.tick() above)
        // unconditionally fires a full attachKinetics()/RotationPropagator walk
        // for this block, whether or not anything actually changed. That's true
        // for every kinetic block in the world, not just this one - so a whole
        // reactor's worth of shafts + this tap all potentially re-walk the
        // network on the same load, in whatever order the chunk happens to tick
        // block entities in.
        //
        // That walk isn't needed for correctness on our end: KineticBlockEntity
        // #initialize() already re-registers this block with its correct,
        // persisted network silently (no BFS, no conflict checks) before any
        // tick runs. We only need to actually push a change through the network
        // when the reactor's real output differs from what was persisted - our
        // own tick() below already detects exactly that via lastReactorYield.
        // So: suppress Create's blanket reattach-on-load for this block and let
        // our own tick() be the only thing that ever calls
        // updateGeneratedRotation() here. That removes the tap - the hub all the
        // other faces fan out from - from the pile of blocks independently
        // renegotiating the network on the same load, which is what created the
        // opportunity for a stale, not-yet-reattached neighbor to be read as
        // spinning the "wrong way" for the moment it took everything to settle.
        updateSpeed = false;
    }

    @Override
    public float getGeneratedSpeed()
    {
        return lastReactorYield > 0 ? IRConfig.createPowertapGeneratedRpm : 0;
    }

    @Override
    public float calculateAddedStressCapacity()
    {
        float speed = getGeneratedSpeed();
        if (speed == 0)
        {
            this.lastCapacityProvided = 0;
            return 0;
        }

        // BlockStressValues-style "capacity at 1 RPM" - Create multiplies this
        // by |getGeneratedSpeed()| to get the actual SU added to the network,
        // so dividing by the speed here keeps the delivered SU equal to
        // reactorYield / 10 no matter what RPM is configured.
        float suToDeliver = (float) (lastReactorYield * IRConfig.createSuPerFe);
        float capacityPerRpm = suToDeliver / Math.abs(speed);

        this.lastCapacityProvided = capacityPerRpm;
        return capacityPerRpm;
    }
}
