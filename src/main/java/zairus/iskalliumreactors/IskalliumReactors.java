package zairus.iskalliumreactors;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zairus.iskalliumreactors.block.ModBlocks;
import zairus.iskalliumreactors.blockentity.ModBlockEntities;
import zairus.iskalliumreactors.creativetab.ModCreativeTabs;
import zairus.iskalliumreactors.item.ModItems;
import zairus.iskalliumreactors.menu.ModMenuTypes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import zairus.iskalliumreactors.worldgen.ModFeatures;
import zairus.iskalliumreactors.compat.ModCompat;
import zairus.iskalliumreactors.compat.ConfigEnabledCondition;
import zairus.iskalliumreactors.compat.create.CreateCompat;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.common.conditions.ICondition;
import com.mojang.serialization.MapCodec;

@Mod(IRConstants.MOD_ID)
public class IskalliumReactors
{
    private static final Logger LOGGER = LogManager.getLogger(IRConstants.MOD_NAME);

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, IRConstants.MOD_ID);

    private static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigEnabledCondition>> CONFIG_ENABLED_CONDITION =
            CONDITIONS.register("config_enabled", () -> ConfigEnabledCondition.CODEC);

    public IskalliumReactors(IEventBus modEventBus, ModContainer modContainer)
    {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
		ModFeatures.CONFIGURED_FEATURES.register(modEventBus);
		ModFeatures.PLACED_FEATURES.register(modEventBus);
        CONDITIONS.register(modEventBus);

        if (ModCompat.CREATE_LOADED)
        {
            CreateCompat.init(modEventBus);
        }

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::onConfigLoad);

        modContainer.registerConfig(ModConfig.Type.COMMON, IRConfig.SPEC);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        // Lets other mods' energy-handling machinery see the power tap as an IEnergyStorage.
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.IR_POWER_TAP.get(),
                (be, side) -> be);
    }

    private void onConfigLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == IRConfig.SPEC)
        {
            IRConfig.bake();
        }
    }

    public static void logInfo(String message)
    {
        LOGGER.info(message);
    }
}
