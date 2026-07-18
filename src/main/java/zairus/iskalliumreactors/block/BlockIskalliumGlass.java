package zairus.iskalliumreactors.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Behaves like vanilla glass: non-opaque, silk-touch only, and skips rendering
 * internal faces shared with another glass block of the same type.
 */
public class BlockIskalliumGlass extends Block
{
    public BlockIskalliumGlass(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side)
    {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }
}
