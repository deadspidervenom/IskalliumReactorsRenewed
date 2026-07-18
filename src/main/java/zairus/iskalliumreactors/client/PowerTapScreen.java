package zairus.iskalliumreactors.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import zairus.iskalliumreactors.menu.PowerTapMenu;

/**
 * Shows the power tap's stored FE and its transfer rate for the last tick.
 */
public class PowerTapScreen extends AbstractContainerScreen<PowerTapMenu>
{
    public PowerTapScreen(PowerTapMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 80;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
    {
        // No background texture; everything is drawn directly in render() below.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF8B8B8B);

        guiGraphics.drawCenteredString(this.font, this.title, x + this.imageWidth / 2, y + 10, 0xFFFFFF);

        String energyLine = "Stored: " + this.menu.getEnergyStored() + " FE";
        String rateLine = "Rate: " + this.menu.getTransferRate() + " FE/t";

        guiGraphics.drawCenteredString(this.font, energyLine, x + this.imageWidth / 2, y + 32, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, rateLine, x + this.imageWidth / 2, y + 46, 0xFFFF55);
    }
}
