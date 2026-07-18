package zairus.iskalliumreactors.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import zairus.iskalliumreactors.IRConstants;
import zairus.iskalliumreactors.menu.ModMenuTypes;

@EventBusSubscriber(modid = IRConstants.MOD_ID, value = Dist.CLIENT)
public class ClientSetup
{
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event)
    {
        event.register(ModMenuTypes.CONTROLLER_MENU.get(), ControllerScreen::new);
        event.register(ModMenuTypes.POWER_TAP_MENU.get(), PowerTapScreen::new);
    }
}
