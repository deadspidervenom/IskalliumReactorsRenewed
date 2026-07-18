package zairus.iskalliumreactors;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config for the mod. Values are read once via bake() after config load (see
 * IRConfig.bake(), hooked from ModConfigEvent.Loading / Reloading in the main mod class).
 */
public class IRConfig
{
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue EACH_ISKALLIUM_BLOCK_POWER_VALUE;
    private static final ModConfigSpec.IntValue ISKALLIUM_GENERATION_WEIGHT;
    private static final ModConfigSpec.IntValue ISKALLIUM_GENERATION_PATCH_SIZE;
    private static final ModConfigSpec.BooleanValue LOG_ISKALLIUM_ORE_GENERATION;
    private static final ModConfigSpec.BooleanValue ISKALLIUM_ORE_SLIME_SOUND;
    private static final ModConfigSpec.IntValue MIN_REACTOR_DIMENSION;
    private static final ModConfigSpec.IntValue REACTOR_SCAN_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue POWERTAP_CAPACITY;
    private static final ModConfigSpec.IntValue POWERTAP_BASE_TRANSFER_RATE;
    private static final ModConfigSpec.IntValue ISKALLIUM_ESSENCE_BLOCK_LIGHT_LEVEL;
    private static final ModConfigSpec.IntValue ISKALLIUM_ORE_LIGHT_LEVEL;
    private static final ModConfigSpec.IntValue ISKALLIUM_ORE_MIN_XP;
    private static final ModConfigSpec.IntValue ISKALLIUM_ORE_MAX_XP;
    private static final ModConfigSpec.BooleanValue ISKALLIUM_ORE_SILK_TOUCH;
    private static final ModConfigSpec.IntValue CREATE_POWERTAP_GENERATED_RPM;
    private static final ModConfigSpec.DoubleValue CREATE_SU_PER_FE;
    private static final ModConfigSpec.BooleanValue CREATE_POWERTAP_SHAFT_RENDER;
    private static final ModConfigSpec.BooleanValue USE_BLAST_FURNACE_STEEL_RECIPE;

    // Live, "baked" values that the rest of the mod actually reads.
    public static int eachIskalliumBlockPowerValue = 160;
    public static int iskalliumGenerationWeight = 60;
    public static int iskalliumGenerationPatchSize = 1;
    public static boolean logIskalliumOreGeneration = false;
    public static boolean iskalliumOreSlimeSound = false;
    public static int minReactorDimension = 3;
    public static int reactorScanIntervalTicks = 20;
    public static int powerTapCapacity = 1000000;
    public static int powerTapBaseTransferRate = 1000;
    public static int iskalliumEssenceBlockLightLevel = 3;
    public static int iskalliumOreLightLevel = 0;
    public static int iskalliumOreMinXp = 3;
    public static int iskalliumOreMaxXp = 7;
    public static boolean iskalliumOreSilkTouch = true;
    public static int createPowertapGeneratedRpm = 64;
    public static double createSuPerFe = 0.8;
    public static boolean createPowertapShaftRender = true;
    public static boolean useBlastFurnaceSteelRecipe = false;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("ENERGY_VALUES");
        EACH_ISKALLIUM_BLOCK_POWER_VALUE = builder
                .comment("How much energy each Iskallium Block provides")
                .defineInRange("eachIskalliumBlockPowerValue", 160, 0, 255);
        POWERTAP_CAPACITY = builder
                .comment("Maximum FE the (non-Create) Power Tap can buffer internally before it",
                        "stops accepting new production from the reactor.")
                .defineInRange("powerTapCapacity", 1000000, 1000, Integer.MAX_VALUE);
        POWERTAP_BASE_TRANSFER_RATE = builder
                .comment("Flat FE/t the Power Tap can always push out on top of whatever the reactor",
                        "is currently producing. This is what lets a Power Tap slowly drain its own",
                        "buffer even after the reactor stops, rather than being capped exactly at the",
                        "live reactor yield.")
                .defineInRange("powerTapBaseTransferRate", 1000, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("WORLD_GENERATION");
        // NOTE: world generation is entirely datapack-driven now (see
        // data/iskalliumreactors/worldgen/{configured_feature,placed_feature}) rather than a
        // custom Java Feature reading these live, like the original 1.12 IWorldGenerator did.
        // These two values are kept/exposed for parity with the original config and for other
        // datapacks/KubeJS scripts to read, but changing them here does NOT retroactively alter
        // the packaged configured_feature/placed_feature JSON - override those files directly
        // (or regenerate them) if you need a different rarity/vein size in-game.
        ISKALLIUM_GENERATION_WEIGHT = builder
                .comment("Weight for ore generation of Iskallium. Informational only - see the NOTE",
                        "above this section; edit the placed_feature JSON to actually change generation.")
                .defineInRange("iskalliumGenerationWeight", 60, 0, 255);
        ISKALLIUM_GENERATION_PATCH_SIZE = builder
                .comment("Maximum patch size of Iskallium generated. Informational only - see the NOTE",
                        "above this section; edit the configured_feature JSON to actually change generation.")
                .defineInRange("iskalliumGenerationPatchSize", 1, 1, 255);
        builder.pop();

        builder.push("GENERAL");
        LOG_ISKALLIUM_ORE_GENERATION = builder
                .comment("Set to true to see information in the logs about Iskallium Ore generation position")
                .define("logIskalliumOreGeneration", false);
        ISKALLIUM_ORE_SLIME_SOUND = builder
                .comment("If true, the Iskallium block plays the squishy Slime Block sound effect when",
                        "walked on / placed / broken. If false (default), it uses a plain Stone-like sound",
                        "instead.")
                .define("iskalliumOreSlimeSound", false);
        MIN_REACTOR_DIMENSION = builder
                .comment("The smallest a reactor's width, height, or depth is allowed to be for it to go",
                        "online. Below this on any axis there's no room for a core, so the reactor reports",
                        "itself offline rather than attempting to validate further. Vanilla default is a",
                        "3x3x3 minimum (a 1-block hollow interior surrounded by casing).")
                .defineInRange("minReactorDimension", 3, 1, 999);
        REACTOR_SCAN_INTERVAL_TICKS = builder
                .comment("How often (in ticks, 20 = 1 second) a reactor Controller re-checks its",
                        "structure. Lower values notice broken/repaired reactors faster but cost more",
                        "server performance on large worlds with many reactors; higher values are",
                        "cheaper but slower to react to structure changes.")
                .defineInRange("reactorScanIntervalTicks", 20, 1, 72000);
        builder.pop();

        builder.push("BLOCKS");
        ISKALLIUM_ESSENCE_BLOCK_LIGHT_LEVEL = builder
                .comment("Light level (0-15) emitted by the Iskallium Essence Block.")
                .defineInRange("iskalliumEssenceBlockLightLevel", 3, 0, 15);
        ISKALLIUM_ORE_LIGHT_LEVEL = builder
                .comment("Light level (0-15) emitted by Iskallium Ore blocks (both the Stone and",
                        "Deepslate variants).")
                .defineInRange("iskalliumOreLightLevel", 0, 0, 15);
        ISKALLIUM_ORE_MIN_XP = builder
                .comment("Minimum experience dropped when an Iskallium Ore block (Stone or Deepslate) is",
                        "broken with a non-Silk-Touch tool.")
                .defineInRange("iskalliumOreMinXp", 3, 0, 100);
        ISKALLIUM_ORE_MAX_XP = builder
                .comment("Maximum experience dropped when an Iskallium Ore block (Stone or Deepslate) is",
                        "broken with a non-Silk-Touch tool.")
                .defineInRange("iskalliumOreMaxXp", 7, 0, 100);
        ISKALLIUM_ORE_SILK_TOUCH = builder
                .comment("If true (default), mining Iskallium Ore with a Silk Touch tool drops the ore",
                        "block itself instead of Iskallium Essence. If false, Silk Touch has no special",
                        "effect on Iskallium Ore and it always drops Essence (still Fortune-boosted).",
                        "Like other loot-table conditions, this needs a datapack reload (or rejoining",
                        "the world) after changing to take effect.")
                .define("iskalliumOreSilkTouch", true);
        builder.pop();

        builder.push("CREATE_COMPAT");
        CREATE_POWERTAP_GENERATED_RPM = builder
                .comment("Only used if Create is installed. The rotation speed (RPM) that the SU variant",
                        "of the Power Tap spins its shaft at. Its Stress Unit output scales so that the",
                        "total SU delivered to the network always equals reactor FE output * createSuPerFe,",
                        "regardless of this value - changing this only changes the RPM, not the total power.")
                .defineInRange("createPowertapGeneratedRpm", 64, 1, 256);
        CREATE_SU_PER_FE = builder
                .comment("Only used if Create is installed. Conversion ratio from reactor FE output to",
                        "Stress Units delivered by the SU Power Tap. Default 0.8 means 1000 FE/t = 800 SU",
                        "(a single-core reactor's default 160 FE/t becomes 128 SU).")
                .defineInRange("createSuPerFe", 0.8, 0.0001, 100.0);
        CREATE_POWERTAP_SHAFT_RENDER = builder
                .comment("Only used if Create is installed. Whether the SU Power Tap renders its little",
                        "spinning shaft stubs on exposed faces. Purely cosmetic - turn off if you have",
                        "render/performance issues with it.")
                .define("createPowertapShaftRender", true);
        builder.pop();

        builder.push("RECIPES");
        USE_BLAST_FURNACE_STEEL_RECIPE = builder
                .comment("If false (default), Steel Ingots are crafted with the original 3x3 iron+coal block",
                        "shaped recipe. If true, that recipe is replaced with a Blast Furnace recipe that",
                        "smelts a single Iron Ingot into a Steel Ingot instead. Only one of the two recipes",
                        "is ever active at a time. Requires a datapack reload (or rejoining the world) after",
                        "changing - like all recipe conditions, this isn't re-evaluated live.")
                .define("useBlastFurnaceSteelRecipe", false);
        builder.pop();

        SPEC = builder.build();
    }

    /** Copies the live config values into the plain static fields the rest of the mod reads. */
    public static void bake()
    {
        eachIskalliumBlockPowerValue = EACH_ISKALLIUM_BLOCK_POWER_VALUE.get();
        iskalliumGenerationWeight = ISKALLIUM_GENERATION_WEIGHT.get();
        iskalliumGenerationPatchSize = ISKALLIUM_GENERATION_PATCH_SIZE.get();
        logIskalliumOreGeneration = LOG_ISKALLIUM_ORE_GENERATION.get();
        iskalliumOreSlimeSound = ISKALLIUM_ORE_SLIME_SOUND.get();
        minReactorDimension = MIN_REACTOR_DIMENSION.get();
        reactorScanIntervalTicks = REACTOR_SCAN_INTERVAL_TICKS.get();
        powerTapCapacity = POWERTAP_CAPACITY.get();
        powerTapBaseTransferRate = POWERTAP_BASE_TRANSFER_RATE.get();
        iskalliumEssenceBlockLightLevel = ISKALLIUM_ESSENCE_BLOCK_LIGHT_LEVEL.get();
        iskalliumOreLightLevel = ISKALLIUM_ORE_LIGHT_LEVEL.get();
        iskalliumOreMinXp = ISKALLIUM_ORE_MIN_XP.get();
        iskalliumOreMaxXp = ISKALLIUM_ORE_MAX_XP.get();
        iskalliumOreSilkTouch = ISKALLIUM_ORE_SILK_TOUCH.get();
        createPowertapGeneratedRpm = CREATE_POWERTAP_GENERATED_RPM.get();
        createSuPerFe = CREATE_SU_PER_FE.get();
        createPowertapShaftRender = CREATE_POWERTAP_SHAFT_RENDER.get();
        useBlastFurnaceSteelRecipe = USE_BLAST_FURNACE_STEEL_RECIPE.get();
    }
}
