package zairus.iskalliumreactors.block;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import zairus.iskalliumreactors.IRConfig;

/**
 * The {@link net.minecraft.world.level.block.DropExperienceBlock} constructor takes a single
 * {@link IntProvider} and stores it for the block's whole lifetime, so a plain
 * {@code UniformInt.of(min, max)} would bake IRConfig's XP range in permanently at block
 * registration (mod startup) rather than honoring config changes/reloads like every other
 * config value in this mod does. This tiny IntProvider instead re-reads
 * {@link IRConfig#iskalliumOreMinXp} / {@link IRConfig#iskalliumOreMaxXp} on every single call,
 * so it stays live for the same reason {@code ModBlocks}'s {@code .lightLevel(state -> ...)}
 * lambdas do.
 *
 * Not registered with a real {@link IntProviderType} since it's only ever used directly in
 * Java (as the DropExperienceBlock constructor argument) and never round-tripped through a
 * Codec/JSON - getType() is just enough to satisfy the abstract class, matching UNIFORM's shape
 * since that's what this most closely resembles.
 */
public final class ConfigDrivenOreXp extends IntProvider
{
    public static final ConfigDrivenOreXp INSTANCE = new ConfigDrivenOreXp();

    private ConfigDrivenOreXp() {}

    @Override
    public int sample(RandomSource random)
    {
        int min = Math.min(IRConfig.iskalliumOreMinXp, IRConfig.iskalliumOreMaxXp);
        int max = Math.max(IRConfig.iskalliumOreMinXp, IRConfig.iskalliumOreMaxXp);
        return min + random.nextInt(max - min + 1);
    }

    @Override
    public int getMinValue()
    {
        return Math.min(IRConfig.iskalliumOreMinXp, IRConfig.iskalliumOreMaxXp);
    }

    @Override
    public int getMaxValue()
    {
        return Math.max(IRConfig.iskalliumOreMinXp, IRConfig.iskalliumOreMaxXp);
    }

    @Override
    public IntProviderType<?> getType()
    {
        return IntProviderType.UNIFORM;
    }
}
