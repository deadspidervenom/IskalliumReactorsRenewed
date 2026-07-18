package zairus.iskalliumreactors.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import zairus.iskalliumreactors.IRConstants;

/**
 * Registers every block this mod adds, along with its BlockItem.
 */
public class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IRConstants.MOD_ID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(
            net.minecraft.core.registries.Registries.ITEM, IRConstants.MOD_ID);

    public static final DeferredBlock<BlockIskallium> ISKALLIUM = BLOCKS.register("iskallium_essence_block",
            () -> new BlockIskallium(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .lightLevel(state -> zairus.iskalliumreactors.IRConfig.iskalliumEssenceBlockLightLevel)
                    .sound(SoundType.STONE) // fallback only - BlockIskallium#getSoundType overrides this live based on IRConfig.iskalliumOreSlimeSound
                    .randomTicks()
                    .strength(0.5F)
                    .noOcclusion()));

public static final DeferredBlock<BlockIskalliumStoneOre> ISKALLIUM_STONE_ORE = BLOCKS.register("iskallium_stone_ore",
    () -> new BlockIskalliumStoneOre(BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .lightLevel(state -> zairus.iskalliumreactors.IRConfig.iskalliumOreLightLevel)
        .sound(SoundType.STONE)
        .strength(3F, 5F)               // hardness matches Iron Ore
        .requiresCorrectToolForDrops()  // needs a pickaxe of the right tier to drop
));

    public static final DeferredBlock<BlockIskalliumGlass> ISKALLIUM_GLASS = BLOCKS.register("iskallium_glass",
            () -> new BlockIskalliumGlass(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .sound(SoundType.GLASS)
                    .strength(0.3F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)));

    public static final DeferredBlock<BlockIskalliumTintedGlass> ISKALLIUM_TINTED_GLASS = BLOCKS.register("iskallium_tinted_glass",
            () -> new BlockIskalliumTintedGlass(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .sound(SoundType.GLASS)
                    .strength(0.3F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)));

    public static final DeferredBlock<BlockSteelCasing> STEEL_CASING = BLOCKS.register("steel_casing",
            () -> new BlockSteelCasing(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(1.5F)));

    public static final DeferredBlock<BlockIRController> STEEL_CONTROLLER = BLOCKS.register("steel_controller",
            () -> new BlockIRController(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.STONE)
                    .strength(1.5F)));

    public static final DeferredBlock<BlockIRPowerTap> STEEL_POWERTAP = BLOCKS.register("steel_powertap",
            () -> new BlockIRPowerTap(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.STONE)
                    .strength(1.5F)));
					
public static final DeferredBlock<BlockIskalliumDeepslateOre> ISKALLIUM_DEEPSLATE_ORE = BLOCKS.register("iskallium_deepslate_ore",
    () -> new BlockIskalliumDeepslateOre(BlockBehaviour.Properties.of()
        .mapColor(MapColor.DEEPSLATE)
        .lightLevel(state -> zairus.iskalliumreactors.IRConfig.iskalliumOreLightLevel)
        .sound(SoundType.DEEPSLATE)
        .strength(4.5F, 6F)
        .requiresCorrectToolForDrops()
));

public static final DeferredHolder<Item, BlockItem> ISKALLIUM_DEEPSLATE_ORE_ITEM = BLOCK_ITEMS.register("iskallium_deepslate_ore",
        () -> new BlockItem(ISKALLIUM_DEEPSLATE_ORE.get(), new Item.Properties()));

    // BlockItems for every block above, registered under the same registry name.
    public static final DeferredHolder<Item, BlockItem> ISKALLIUM_ITEM = BLOCK_ITEMS.register("iskallium_essence_block",
            () -> new BlockItem(ISKALLIUM.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ISKALLIUM_STONE_ORE_ITEM = BLOCK_ITEMS.register("iskallium_stone_ore",
            () -> new BlockItem(ISKALLIUM_STONE_ORE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ISKALLIUM_GLASS_ITEM = BLOCK_ITEMS.register("iskallium_glass",
            () -> new BlockItem(ISKALLIUM_GLASS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ISKALLIUM_TINTED_GLASS_ITEM = BLOCK_ITEMS.register("iskallium_tinted_glass",
            () -> new BlockItem(ISKALLIUM_TINTED_GLASS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> STEEL_CASING_ITEM = BLOCK_ITEMS.register("steel_casing",
            () -> new BlockItem(STEEL_CASING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> STEEL_CONTROLLER_ITEM = BLOCK_ITEMS.register("steel_controller",
            () -> new BlockItem(STEEL_CONTROLLER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> STEEL_POWERTAP_ITEM = BLOCK_ITEMS.register("steel_powertap",
            () -> new BlockItem(STEEL_POWERTAP.get(), new Item.Properties()));
}
