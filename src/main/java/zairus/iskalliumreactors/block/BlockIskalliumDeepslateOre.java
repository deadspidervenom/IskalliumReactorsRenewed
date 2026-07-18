package zairus.iskalliumreactors.block;

import net.minecraft.world.level.block.DropExperienceBlock;

/** Deepslate variant of {@link BlockIskalliumStoneOre} - see that class for XP/loot-table notes. */
public class BlockIskalliumDeepslateOre extends DropExperienceBlock
{
    public BlockIskalliumDeepslateOre(Properties properties)
    {
        super(ConfigDrivenOreXp.INSTANCE, properties);
    }
}