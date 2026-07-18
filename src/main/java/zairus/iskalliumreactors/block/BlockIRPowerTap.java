package zairus.iskalliumreactors.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import zairus.iskalliumreactors.blockentity.BlockEntityIRPowerTap;
import zairus.iskalliumreactors.blockentity.ModBlockEntities;
import zairus.iskalliumreactors.menu.PowerTapMenu;

public class BlockIRPowerTap extends BaseEntityBlock
{
    public BlockIRPowerTap(Properties properties)
    {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockIRPowerTap> codec()
    {
        return simpleCodec(BlockIRPowerTap::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BlockEntityIRPowerTap(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, ModBlockEntities.IR_POWER_TAP.get(), BlockEntityIRPowerTap::tick);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
    {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof BlockEntityIRPowerTap tap)
        {
            return new SimpleMenuProvider(
                    (containerId, inv, player) -> new PowerTapMenu(containerId, inv, tap),
                    Component.translatable("menu.iskalliumreactors.power_tap"));
        }

        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer)
        {
            MenuProvider provider = this.getMenuProvider(state, level, pos);

            if (provider != null)
            {
                serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
            }
        }

        return InteractionResult.SUCCESS;
    }
}
