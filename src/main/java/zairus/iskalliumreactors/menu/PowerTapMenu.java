package zairus.iskalliumreactors.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import zairus.iskalliumreactors.blockentity.BlockEntityIRPowerTap;

public class PowerTapMenu extends AbstractContainerMenu
{
    private final ContainerData data;

    public PowerTapMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData)
    {
        this(containerId, playerInv, resolve(playerInv, extraData.readBlockPos()), false);
    }

    public PowerTapMenu(int containerId, Inventory playerInv, BlockEntityIRPowerTap tap)
    {
        this(containerId, playerInv, tap, true);
    }

    private PowerTapMenu(int containerId, Inventory playerInv, BlockEntityIRPowerTap tap, boolean serverSide)
    {
        super(ModMenuTypes.POWER_TAP_MENU.get(), containerId);

        this.data = new ContainerData()
        {
            private final int[] synced = new int[2];

            @Override
            public int get(int index)
            {
                if (serverSide && tap != null)
                {
                    synced[0] = tap.energy;
                    synced[1] = tap.lastTransferred;
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
                return 2;
            }
        };

        this.addDataSlots(this.data);
    }

    private static BlockEntityIRPowerTap resolve(Inventory playerInv, BlockPos pos)
    {
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        return be instanceof BlockEntityIRPowerTap tap ? tap : null;
    }

    public int getEnergyStored()
    {
        return this.data.get(0);
    }

    public int getTransferRate()
    {
        return this.data.get(1);
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
