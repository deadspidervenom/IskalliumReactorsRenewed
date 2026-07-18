package zairus.iskalliumreactors.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import zairus.iskalliumreactors.IRConfig;

/**
 * A recipe/loot/etc. condition that reads one of IRConfig's boolean flags by name.
 * Evaluated at datapack load time (world join, /reload) - like all JSON conditions,
 * changing the underlying config value doesn't retroactively re-evaluate recipes
 * already loaded; a reload is needed, same as any other condition-gated recipe.
 *
 * Usage in a recipe's "neoforge:conditions": { "type": "iskalliumreactors:config_enabled", "flag": "useBlastFurnaceSteelRecipe" }
 * Wrap with "neoforge:not" for the inverse.
 */
public record ConfigEnabledCondition(String flag) implements ICondition
{
    public static final MapCodec<ConfigEnabledCondition> CODEC =
            Codec.STRING.fieldOf("flag").xmap(ConfigEnabledCondition::new, ConfigEnabledCondition::flag);

    @Override
    public boolean test(IContext context)
    {
        return switch (flag)
        {
            case "useBlastFurnaceSteelRecipe" -> IRConfig.useBlastFurnaceSteelRecipe;
            case "iskalliumOreSilkTouch" -> IRConfig.iskalliumOreSilkTouch;
            default -> false;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec()
    {
        return CODEC;
    }

    @Override
    public String toString()
    {
        return "config_enabled(\"" + flag + "\")";
    }
}
