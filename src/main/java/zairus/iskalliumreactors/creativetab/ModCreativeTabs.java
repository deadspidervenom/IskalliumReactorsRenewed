package zairus.iskalliumreactors.creativetab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import zairus.iskalliumreactors.IRConstants;
import zairus.iskalliumreactors.block.ModBlocks;

/**
 * Registers the creative-mode tab that holds all of this mod's items.
 */
public class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IRConstants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ISKALLIUM_REACTORS_TAB = TABS.register(
            "iskalliumreactors",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + IRConstants.MOD_ID))
                    .icon(() -> new ItemStack(ModBlocks.ISKALLIUM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.ISKALLIUM.get());
                        output.accept(ModBlocks.ISKALLIUM_STONE_ORE.get());
						output.accept(ModBlocks.ISKALLIUM_DEEPSLATE_ORE.get());
                        output.accept(ModBlocks.ISKALLIUM_GLASS.get());
                        output.accept(ModBlocks.ISKALLIUM_TINTED_GLASS.get());
                        output.accept(ModBlocks.STEEL_CASING.get());
                        output.accept(ModBlocks.STEEL_CONTROLLER.get());
                        output.accept(ModBlocks.STEEL_POWERTAP.get());
                        output.accept(zairus.iskalliumreactors.item.ModItems.ISKALLIUM_ESSENCE.get());
                        output.accept(zairus.iskalliumreactors.item.ModItems.STEEL_INGOT.get());

                        if (zairus.iskalliumreactors.compat.ModCompat.CREATE_LOADED)
                        {
                            output.accept(zairus.iskalliumreactors.compat.create.CreateCompat.STEEL_POWERTAP_CREATE_ITEM.get());
                        }
                    })
                    .build());
}
