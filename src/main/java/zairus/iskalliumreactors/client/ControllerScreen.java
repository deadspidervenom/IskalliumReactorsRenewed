package zairus.iskalliumreactors.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import zairus.iskalliumreactors.menu.ControllerMenu;

/**
 * Shows the reactor's RUNNING/OFFLINE status, core count, size, and either the current
 * generation rate or a short description of what's wrong, all read live from the
 * controller's ContainerData.
 */
public class ControllerScreen extends AbstractContainerScreen<ControllerMenu>
{
    public ControllerScreen(ControllerMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 100;
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

        boolean running = this.menu.isRunning();
        String status = running ? "RUNNING" : "OFFLINE";
        int color = running ? 0x55FF55 : 0xFF5555;

        guiGraphics.drawCenteredString(this.font, status, x + this.imageWidth / 2, y + 28, color);

String coreLine = "Cores: " + this.menu.getCoreCount();
String sizeLine = this.menu.getReactorWidth() + "x" + 
                  this.menu.getReactorHeight() + "x" + 
                  this.menu.getReactorDepth();

        guiGraphics.drawCenteredString(this.font, coreLine, x + this.imageWidth / 2, y + 46, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, sizeLine, x + this.imageWidth / 2, y + 58, 0xFFFFFF);

        // Bottom slot: structure issue if offline, current generation rate if running.
        if (!running)
        {
            String issue = this.menu.getIssueSummary(this.minecraft.level);
            if (!issue.isEmpty())
            {
                int maxWidth = this.imageWidth - 12;
                java.util.List<net.minecraft.util.FormattedCharSequence> lines =
                        this.font.split(Component.literal(issue), maxWidth);

                int lineY = y + 74;
                for (net.minecraft.util.FormattedCharSequence line : lines)
                {
                    guiGraphics.drawCenteredString(this.font, line, x + this.imageWidth / 2, lineY, 0xFFAA55);
                    lineY += this.font.lineHeight + 1;
                }
            }
        }
        else
        {
            int fe = this.menu.getFeOutput();
            String genLine;
            if (this.menu.isUsingCreateTap())
            {
                int su = (int) (fe * zairus.iskalliumreactors.IRConfig.createSuPerFe);
                genLine = "Generating: " + su + " SU";
            }
            else
            {
                genLine = "Generating: " + fe + " FE/t";
            }

            guiGraphics.drawCenteredString(this.font, genLine, x + this.imageWidth / 2, y + 74, 0x55FF55);
        }
    }
}

