package zairus.iskalliumreactors.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Iskallium Glass, but crafted with Tinted Glass instead of regular glass. Behaves exactly
 * like {@link BlockIskalliumGlass} (same "skip shared internal faces" rendering) except it
 * also blocks light the same way vanilla Tinted Glass does: it doesn't let skylight pass
 * down through it, and it counts as a full light-blocking block for light propagation,
 * even though it's still see-through.
 *
 * Signatures here are for NeoForge 1.21.0/1.21.1, where both of these BlockBehaviour
 * methods still take (BlockState, BlockGetter, BlockPos) - this was later simplified to a
 * single BlockState parameter in 1.21.10+, so if this project is ever bumped past 1.21.1,
 * these overrides need to drop the BlockGetter/BlockPos params to match.
 */
public class BlockIskalliumTintedGlass extends BlockIskalliumGlass
{
    public BlockIskalliumTintedGlass(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return false;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos)
    {
        return 15;
    }
}
