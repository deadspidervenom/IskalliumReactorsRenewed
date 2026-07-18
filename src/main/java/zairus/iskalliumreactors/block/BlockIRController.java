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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import zairus.iskalliumreactors.blockentity.BlockEntityIRController;
import zairus.iskalliumreactors.blockentity.ModBlockEntities;
import zairus.iskalliumreactors.menu.ControllerMenu;

/**
 * The reactor's controller block. Opens the controller GUI on right-click and provides
 * the BlockEntity that scans and tracks the reactor structure.
 */
public class BlockIRController extends BaseEntityBlock
{
    public BlockIRController(Properties properties)
    {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockIRController> codec()
    {
        return simpleCodec(BlockIRController::new);
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
        return new BlockEntityIRController(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type)
    {
        return createTickerHelper(type, ModBlockEntities.IR_CONTROLLER.get(), BlockEntityIRController::tick);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
    {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof BlockEntityIRController controller)
        {
            return new SimpleMenuProvider(
                    (containerId, inv, player) -> new ControllerMenu(containerId, inv, controller),
                    Component.translatable("menu.iskalliumreactors.controller"));
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
