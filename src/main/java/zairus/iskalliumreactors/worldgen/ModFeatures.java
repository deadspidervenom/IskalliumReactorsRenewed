package zairus.iskalliumreactors.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.registries.DeferredRegister;
import zairus.iskalliumreactors.IRConstants;

/**
 * Registers the ConfiguredFeature and PlacedFeature for world generation.
 * This is required in NeoForge 1.21+ alongside the data JSON files.
 */
public class ModFeatures {

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
            DeferredRegister.create(Registries.CONFIGURED_FEATURE, IRConstants.MOD_ID);

    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registries.PLACED_FEATURE, IRConstants.MOD_ID);

    // Configured Features
    public static final ResourceKey<ConfiguredFeature<?, ?>> ISKALLIUM_ORE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(IRConstants.MOD_ID, "iskallium_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> ISKALLIUM_DEEPSLATE_ORE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(IRConstants.MOD_ID, "iskallium_deepslate_ore"));

    // Placed Features
    public static final ResourceKey<PlacedFeature> PLACED_ISKALLIUM_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(IRConstants.MOD_ID, "iskallium_ore"));

    public static final ResourceKey<PlacedFeature> PLACED_ISKALLIUM_DEEPSLATE_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(IRConstants.MOD_ID, "iskallium_deepslate_ore"));
}