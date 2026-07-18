package zairus.iskalliumreactors.block;

import net.minecraft.world.level.block.DropExperienceBlock;

/**
 * The stone-tier Iskallium ore. Drops (2-6 Iskallium Essence, boosted by Fortune, or
 * the ore itself with Silk Touch if enabled) live in
 * data/iskalliumreactors/loot_table/blocks/iskallium_stone_ore.json. The XP range comes
 * from ConfigDrivenOreXp so it stays in sync with IRConfig.iskalliumOreMinXp/MaxXp.
 */
public class BlockIskalliumStoneOre extends DropExperienceBlock
{
    public BlockIskalliumStoneOre(Properties properties)
    {
        super(ConfigDrivenOreXp.INSTANCE, properties);
    }
}
