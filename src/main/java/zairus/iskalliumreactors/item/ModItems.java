package zairus.iskalliumreactors.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import zairus.iskalliumreactors.IRConstants;

/**
 * Registers the plain (non-block) items this mod adds.
 */
public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IRConstants.MOD_ID);

    public static final DeferredHolder<Item, Item> ISKALLIUM_ESSENCE = ITEMS.registerSimpleItem(
            "iskallium_essence", new Item.Properties());

    public static final DeferredHolder<Item, Item> STEEL_INGOT = ITEMS.registerSimpleItem(
            "steel_ingot", new Item.Properties());
}
