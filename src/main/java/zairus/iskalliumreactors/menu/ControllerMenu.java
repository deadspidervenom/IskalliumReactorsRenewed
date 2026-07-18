package zairus.iskalliumreactors.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import zairus.iskalliumreactors.blockentity.BlockEntityIRController;

public class ControllerMenu extends AbstractContainerMenu
{
    private final ContainerData data;
    private final BlockPos controllerPos;

    public ControllerMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData)
    {
        this(containerId, playerInv, extraData.readBlockPos());
    }

    public ControllerMenu(int containerId, Inventory playerInv, BlockEntityIRController controller)
    {
        this(containerId, playerInv, controller.getBlockPos());
    }

    private ControllerMenu(int containerId, Inventory playerInv, BlockPos pos)
    {
        super(ModMenuTypes.CONTROLLER_MENU.get(), containerId);

        this.controllerPos = pos;
        BlockEntityIRController controller = resolve(playerInv, pos);
        boolean serverSide = !playerInv.player.level().isClientSide;

this.data = new ContainerData()
{
    private final int[] synced = new int[7];

    @Override
    public int get(int index)
    {
        if (serverSide && controller != null)
        {
            synced[0] = controller.getIsValidReactor() ? 1 : 0;
            synced[1] = controller.getCoreCount();
            synced[2] = controller.getWidth();
            synced[3] = controller.getHeight();
            synced[4] = controller.getDepth();
            synced[5] = controller.getCurrentFeOutput();
            synced[6] = controller.isUsingCreateTap() ? 1 : 0;
        }

        return synced[index];
    }

    @Override
    public void set(int index, int value)
    {
        synced[index] = value;
    }

    @Override
    public int getCount()
    {
        return 7;
    }
};

        this.addDataSlots(this.data);
    }

    public BlockPos getControllerPos()
    {
        return this.controllerPos;
    }

    /** Client-side only: reads the issue text straight off the synced BlockEntity (see
     * BlockEntityIRController's getUpdateTag/handleUpdateTag) rather than through
     * ContainerData, since ContainerData's int[] slots can't carry free text. */
    public String getIssueSummary(net.minecraft.world.level.Level level)
    {
        BlockEntity be = level.getBlockEntity(this.controllerPos);
        return be instanceof BlockEntityIRController controller ? controller.getIssueSummary() : "";
    }

    private static BlockEntityIRController resolve(Inventory playerInv, BlockPos pos)
    {
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        return be instanceof BlockEntityIRController controller ? controller : null;
    }

    public boolean isRunning()
    {
        return this.data.get(0) == 1;
    }

    public int getCoreCount()
    {
        return this.data.get(1);
    }

    public int getReactorWidth() { return this.data.get(2); }
public int getReactorHeight() { return this.data.get(3); }
public int getReactorDepth() { return this.data.get(4); }

    public int getFeOutput()
    {
        return this.data.get(5);
    }

    public boolean isUsingCreateTap()
    {
        return this.data.get(6) == 1;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index)
    {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
