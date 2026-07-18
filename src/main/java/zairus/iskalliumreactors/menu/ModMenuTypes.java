package zairus.iskalliumreactors.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import zairus.iskalliumreactors.IRConstants;

import java.util.function.Supplier;

/**
 * Menu types for the controller and power tap screens.
 */
public class ModMenuTypes
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, IRConstants.MOD_ID);

    public static final Supplier<MenuType<ControllerMenu>> CONTROLLER_MENU =
            MENU_TYPES.register("controller_menu", () -> IMenuTypeExtension.create(ControllerMenu::new));

    public static final Supplier<MenuType<PowerTapMenu>> POWER_TAP_MENU =
            MENU_TYPES.register("power_tap_menu", () -> IMenuTypeExtension.create(PowerTapMenu::new));
}
