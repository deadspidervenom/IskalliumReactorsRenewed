package zairus.iskalliumreactors.compat.create;

import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import zairus.iskalliumreactors.IRConstants;
import zairus.iskalliumreactors.blockentity.BlockEntityIRController;

/**
 * All real Create integration lives here. This class is ONLY ever loaded/called
 * from sites guarded by zairus.iskalliumreactors.compat.ModCompat.CREATE_LOADED.
 * Never call anything in this class without that guard - see ModCompat's javadoc
 * for why.
 */
public class CreateCompat
{
    private static final DeferredRegister.Blocks CREATE_BLOCKS =
            DeferredRegister.createBlocks(IRConstants.MOD_ID);
    private static final DeferredRegister<Item> CREATE_ITEMS =
            DeferredRegister.create(Registries.ITEM, IRConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> CREATE_BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IRConstants.MOD_ID);

    public static final DeferredBlock<BlockIRPowerTapCreate> STEEL_POWERTAP_CREATE =
            CREATE_BLOCKS.register("steel_powertap_create",
                    () -> new BlockIRPowerTapCreate(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .sound(SoundType.STONE)
                            .strength(1.5F, 2000.0F)));

    public static final DeferredHolder<Item, BlockItem> STEEL_POWERTAP_CREATE_ITEM =
            CREATE_ITEMS.register("steel_powertap_create",
                    () -> new BlockItem(STEEL_POWERTAP_CREATE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityIRPowerTapCreate>> IR_POWER_TAP_CREATE =
            CREATE_BLOCK_ENTITIES.register("ir_power_tap_create", () -> BlockEntityType.Builder.of(
                    BlockEntityIRPowerTapCreate::new, STEEL_POWERTAP_CREATE.get()).build(null));

    /** Called once from the main mod constructor, only when ModCompat.CREATE_LOADED is true. */
    public static void init(IEventBus modEventBus)
    {
        CREATE_BLOCKS.register(modEventBus);
        CREATE_ITEMS.register(modEventBus);
        CREATE_BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(CreateCompat::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event)
    {
        // Registered purely so goggles/tooltips can show a sensible generated-RPM
        // hint for this block. The actual generated speed always comes from
        // BlockEntityIRPowerTapCreate#getGeneratedSpeed(), driven by IRConfig.
        event.enqueueWork(() -> BlockStressValues.RPM.register(
                STEEL_POWERTAP_CREATE.get(),
                new BlockStressValues.GeneratedRpm(zairus.iskalliumreactors.IRConfig.createPowertapGeneratedRpm, false)));
    }

    // --- Helpers called from BlockEntityIRController (only under ModCompat.CREATE_LOADED) ---
    // These methods deliberately only take/return vanilla types (Block, BlockEntity) so that
    // BlockEntityIRController's own bytecode never has to reference a Create type directly.

    public static Block getPowerTapBlock()
    {
        return STEEL_POWERTAP_CREATE.get();
    }

    public static void attachControllerIfPowerTap(BlockEntity blockEntity, BlockEntityIRController controller)
    {
        if (blockEntity instanceof BlockEntityIRPowerTapCreate tap)
        {
            tap.setController(controller);
        }
    }

    private CreateCompat() {}
}
