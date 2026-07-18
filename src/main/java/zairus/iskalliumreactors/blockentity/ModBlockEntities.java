package zairus.iskalliumreactors.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import zairus.iskalliumreactors.IRConstants;
import zairus.iskalliumreactors.block.ModBlocks;

public class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, IRConstants.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityIRController>> IR_CONTROLLER =
            BLOCK_ENTITIES.register("ir_controller", () -> BlockEntityType.Builder.of(
                    BlockEntityIRController::new, ModBlocks.STEEL_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityIRPowerTap>> IR_POWER_TAP =
            BLOCK_ENTITIES.register("ir_power_tap", () -> BlockEntityType.Builder.of(
                    BlockEntityIRPowerTap::new, ModBlocks.STEEL_POWERTAP.get()).build(null));
}
