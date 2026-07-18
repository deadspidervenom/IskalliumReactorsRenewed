package zairus.iskalliumreactors.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry (rebuilt from each controller's persisted NBT as controllers load -
 * this class itself saves nothing to disk) of every controller's known "8 corners"
 * bounding box, keyed by the owning controller's BlockPos and scoped per dimension.
 * <p>
 * A {@link BlockEntityIRController} registers itself here the moment it has a known,
 * validated bounding box (see {@code hasKnownBounds} in that class) and removes itself
 * when that box is wiped or the controller block is broken. Other controllers consult
 * this registry while scanning outward for their own structure so that two reactors
 * built side by side (potentially sharing a wall) never walk into, claim, or otherwise
 * consider blocks that already belong to a neighboring reactor.
 */
public final class ReactorRegistry
{
    /** Inclusive axis-aligned bounding box - equivalent to the reactor's 8 corners. */
    public record Bounds(int xStart, int xEnd, int yStart, int yEnd, int zStart, int zEnd)
    {
        public boolean contains(BlockPos pos)
        {
            return pos.getX() >= xStart && pos.getX() <= xEnd
                    && pos.getZ() >= zStart && pos.getZ() <= zEnd
                    && pos.getY() <= yStart && pos.getY() >= yEnd;
        }
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, Bounds>> PER_LEVEL = new ConcurrentHashMap<>();

    private ReactorRegistry() {}

    private static Map<BlockPos, Bounds> mapFor(Level level)
    {
        return PER_LEVEL.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
    }

    /** Claims/updates a controller's known bounds. Called whenever those bounds are (re)established. */
    public static void register(Level level, BlockPos controllerPos, Bounds bounds)
    {
        mapFor(level).put(controllerPos.immutable(), bounds);
    }

    /** Releases a controller's claim - called on wipe (rebuild required) or on block removal. */
    public static void unregister(Level level, BlockPos controllerPos)
    {
        Map<BlockPos, Bounds> map = PER_LEVEL.get(level.dimension());
        if (map != null)
            map.remove(controllerPos);
    }

    /** True if pos falls inside some *other* controller's currently registered bounds. */
    public static boolean isClaimedByAnother(Level level, BlockPos pos, BlockPos selfControllerPos)
    {
        Map<BlockPos, Bounds> map = PER_LEVEL.get(level.dimension());
        if (map == null)
            return false;

        for (Map.Entry<BlockPos, Bounds> entry : map.entrySet())
        {
            if (entry.getKey().equals(selfControllerPos))
                continue;
            if (entry.getValue().contains(pos))
                return true;
        }
        return false;
    }
}
