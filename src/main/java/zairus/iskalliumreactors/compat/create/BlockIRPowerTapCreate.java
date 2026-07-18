package zairus.iskalliumreactors.compat.create;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * The Create-compat variant of the Power Tap. Functionally it's still a Power
 * Tap that only works attached to a valid Iskallium Reactor controller, but
 * instead of pushing FE through an IEnergyStorage capability, it spins a shaft
 * through its center (like a vanilla log's core) and feeds Stress Units into
 * whatever Create kinetic network that shaft is part of.
 *
 * Extending RotatedPillarKineticBlock (Create's own base for axis-based kinetic
 * blocks, used by e.g. the plain Shaft) gives us the AXIS blockstate property,
 * placement logic that picks the right axis automatically, and rotation
 * handling for free - this is the "simplifies the code" part: we only need to
 * say which faces have a shaft and which axis the block spins on.
 */
public class BlockIRPowerTapCreate extends RotatedPillarKineticBlock implements IBE<BlockEntityIRPowerTapCreate>
{
    public BlockIRPowerTapCreate(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face)
    {
        // Intentionally connects on all 6 faces (not just the 2 matching AXIS
        // like a plain shaft) - the tap is meant to work as a hub players can
        // wire into from any side. getRotationAxis is purely a rendering/AXIS-
        // blockstate concern; RotationPropagator's shaft-to-shaft speed modifier
        // (getAxisModifier) only differs from 1 for DirectionalShaftHalves
        // blocks (gearboxes/split shafts), which this isn't, so there's no
        // physical-axis-mismatch problem in accepting a connection from any face.
        return true;
    }

    @Override
    public Axis getRotationAxis(BlockState state)
    {
        return state.getValue(AXIS);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType)
    {
        return false;
    }

    @Override
    public Class<BlockEntityIRPowerTapCreate> getBlockEntityClass()
    {
        return BlockEntityIRPowerTapCreate.class;
    }

    @Override
    public BlockEntityType<? extends BlockEntityIRPowerTapCreate> getBlockEntityType()
    {
        return CreateCompat.IR_POWER_TAP_CREATE.get();
    }
}
