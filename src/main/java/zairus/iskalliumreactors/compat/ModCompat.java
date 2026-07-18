package zairus.iskalliumreactors.compat;

import net.neoforged.fml.ModList;

/**
 * IMPORTANT: This class must NEVER import or reference any class belonging to
 * an optional dependency (e.g. anything under com.simibubi.create).
 *
 * Because JVM class verification resolves the full type hierarchy referenced
 * by a class the moment that class is loaded, a class that mixes "safe" checks
 * (like isLoaded) with actual references to an optional mod's types can crash
 * with NoClassDefFoundError even if only the "safe" method is ever called -
 * the mere act of loading the class to run any of its methods forces the
 * verifier to resolve every type it mentions, including ones in methods that
 * never execute.
 *
 * So: this class is intentionally kept 100% free of Create references, and all
 * real Create integration lives in zairus.iskalliumreactors.compat.create.CreateCompat,
 * which is ONLY ever touched from call sites guarded by CREATE_LOADED below.
 */
public class ModCompat
{
    public static final boolean CREATE_LOADED = ModList.get().isLoaded("create");

    private ModCompat() {}
}
