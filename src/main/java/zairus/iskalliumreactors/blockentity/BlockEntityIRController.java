package zairus.iskalliumreactors.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import zairus.iskalliumreactors.IskalliumReactors;
import zairus.iskalliumreactors.IRConfig;
import zairus.iskalliumreactors.block.ModBlocks;
import zairus.iskalliumreactors.compat.ModCompat;
import zairus.iskalliumreactors.compat.create.CreateCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * The heart of a reactor: scans the surrounding structure for a valid casing/glass
 * box with a controller and power tap, and tracks how much power it should be
 * generating while that structure holds.
 */
public class BlockEntityIRController extends BlockEntity
{
    private final List<Block> structureBlocks = new ArrayList<>();
    private final List<Block> casingBlocks = new ArrayList<>();
    private final List<Block> generatorBlocks = new ArrayList<>();

    private boolean isValidReactor = false;

    /**
     * True once this controller has a known, previously-validated "8 corners" bounding
     * box (xStart..zEnd below) that it trusts without re-walking outward from scratch.
     * Persists across chunk unload/reload and across the reactor temporarily going
     * offline. Only cleared (forcing a full outward rescan next check) when
     * {@link #wipeKnownBounds(Level, String)} is called - see checkPillarColumn().
     */
    private boolean hasKnownBounds = false;
    private int xStart = 0;
    private int yStart = 0;
    private int zStart = 0;
    private int xEnd = 0;
    private int yEnd = 0;
    private int zEnd = 0;
    private int coreCount = 0;
    private int reactorSize = 0;
    private int controlTicks = 0;
    private Boolean lastLoggedResult = null;
    private String issueSummary = "";
    private boolean usingCreateTap = false;

    public BlockEntityIRController(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.IR_CONTROLLER.get(), pos, state);

        structureBlocks.add(ModBlocks.STEEL_CASING.get());
        structureBlocks.add(ModBlocks.STEEL_CONTROLLER.get());
        structureBlocks.add(ModBlocks.STEEL_POWERTAP.get());
        structureBlocks.add(ModBlocks.ISKALLIUM_GLASS.get());
        structureBlocks.add(ModBlocks.ISKALLIUM_TINTED_GLASS.get());

        casingBlocks.add(ModBlocks.STEEL_CASING.get());
        casingBlocks.add(ModBlocks.STEEL_CONTROLLER.get());
        casingBlocks.add(ModBlocks.STEEL_POWERTAP.get());

        generatorBlocks.add(ModBlocks.ISKALLIUM.get());
        generatorBlocks.add(Blocks.AIR);

        // Create's SU-generating Power Tap is an alternate structural block for the
        // same "tap" slot - see checkStructure()'s tapCount logic below for why a
        // reactor can only ever have one FE tap OR one SU tap, never both.
        if (ModCompat.CREATE_LOADED)
        {
            Block createTap = CreateCompat.getPowerTapBlock();
            structureBlocks.add(createTap);
            casingBlocks.add(createTap);
        }
    }

    public int getCoreCount()
    {
        return this.coreCount;
    }
	
	public int getWidth() {
    return xEnd - xStart + 1;
}

public int getHeight() {
    return yStart - yEnd + 1;
}

public int getDepth() {
    return zEnd - zStart + 1;
}

    /** True if the reactor's tap slot is filled by the Create SU variant rather than the FE one. */
    public boolean isUsingCreateTap()
    {
        return this.usingCreateTap;
    }

    /** Current reactor FE/t output (before any SU conversion) - 0 unless the reactor is running. */
    public int getCurrentFeOutput()
    {
        return this.isValidReactor ? this.coreCount * zairus.iskalliumreactors.IRConfig.eachIskalliumBlockPowerValue : 0;
    }

    public boolean getIsValidReactor()
    {
        return this.isValidReactor;
    }

    public int getReactorSize()
    {
        return this.reactorSize;
    }

    /** Short player-facing description of the last problem found, or "" if the reactor is valid. */
    public String getIssueSummary()
    {
        return this.issueSummary;
    }

    /** True if pos falls within this controller's last-known validated bounding box (inclusive). */
    public boolean isWithinReactorBounds(BlockPos pos)
    {
        return this.isValidReactor
                && pos.getX() >= xStart && pos.getX() <= xEnd
                && pos.getZ() >= zStart && pos.getZ() <= zEnd
                && pos.getY() <= yStart && pos.getY() >= yEnd;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityIRController be)
    {
        ++be.controlTicks;

        if (be.controlTicks >= 1000000)
            be.controlTicks = 0;

        if (!level.isClientSide && (be.controlTicks == 1 || be.controlTicks % Math.max(1, IRConfig.reactorScanIntervalTicks) == 0))
        {
            be.checkStructure(level);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);

        this.isValidReactor = tag.getBoolean("IsValidReactor");
        this.coreCount = tag.getInt("CoreCount");
        this.reactorSize = tag.getInt("ReactorSize");
        this.issueSummary = tag.getString("IssueSummary");
        this.usingCreateTap = tag.getBoolean("UsingCreateTap");
        this.hasKnownBounds = tag.getBoolean("HasKnownBounds");
        this.xStart = tag.getInt("XStart");
        this.yStart = tag.getInt("YStart");
        this.zStart = tag.getInt("ZStart");
        this.xEnd = tag.getInt("XEnd");
        this.yEnd = tag.getInt("YEnd");
        this.zEnd = tag.getInt("ZEnd");
    }

    /**
     * Re-claims this controller's known bounds in the (purely in-memory) ReactorRegistry
     * as soon as the block entity loads, so neighboring controllers scanning on the very
     * same tick already see this reactor's space as claimed instead of racing it.
     */
    @Override
    public void onLoad()
    {
        super.onLoad();
        if (this.hasKnownBounds && this.level != null && !this.level.isClientSide)
        {
            ReactorRegistry.register(this.level, this.getBlockPos(), this.currentBounds());
        }
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if (this.level != null && !this.level.isClientSide)
        {
            ReactorRegistry.unregister(this.level, this.getBlockPos());
        }
    }

    private ReactorRegistry.Bounds currentBounds()
    {
        return new ReactorRegistry.Bounds(xStart, xEnd, yStart, yEnd, zStart, zEnd);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);

        tag.putBoolean("IsValidReactor", this.isValidReactor);
        tag.putInt("CoreCount", this.coreCount);
        tag.putInt("ReactorSize", this.reactorSize);
        tag.putString("IssueSummary", this.issueSummary);
        tag.putBoolean("UsingCreateTap", this.usingCreateTap);
        tag.putBoolean("HasKnownBounds", this.hasKnownBounds);
        tag.putInt("XStart", this.xStart);
        tag.putInt("YStart", this.yStart);
        tag.putInt("ZStart", this.zStart);
        tag.putInt("XEnd", this.xEnd);
        tag.putInt("YEnd", this.yEnd);
        tag.putInt("ZEnd", this.zEnd);
    }

    // Standard vanilla "push this BE's data to nearby clients" boilerplate - without
    // these three, saveAdditional/loadAdditional only ever run for disk save/load,
    // never for keeping the client's copy in sync, which both the GUI (issueSummary)
    // and the Create tap's face-occlusion logic (bounds) depend on.
    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries)
    {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        loadAdditional(tag, registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket()
    {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    private void checkStructure(Level level)
    {
        if (this.hasKnownBounds)
        {
            this.validateKnownBounds(level);
        }
        else
        {
            this.scanAndValidateFromScratch(level);
        }
    }

    /**
     * Fast path used once this controller already has a persisted "8 corners" box from
     * a previous successful formation. Skips the outward corner-walk entirely and just
     * re-checks the remembered box directly - this is what lets a reactor be "rebuilt"
     * (e.g. after a griefer or explosion) by simply replacing blocks in place, with no
     * need to ever re-derive the shape.
     * <p>
     * A casing failure found specifically along one of the 4 vertical corner posts (the
     * "pillars") doesn't immediately invalidate the box. Instead {@link #checkPillarColumn}
     * inspects the rest of that pillar; if it still looks like a pillar mid-repair, this
     * method reports the reactor merely offline (not wiped) and keeps re-checking on the
     * normal schedule until the missing block(s) are replaced. Only a pillar column that
     * no longer looks like a pillar at all (see checkPillarColumn) wipes the known bounds
     * and forces a fresh outward rescan next time.
     */
    private void validateKnownBounds(Level level)
    {
        boolean isReactor = true;
        boolean waitingOnPillarRepair = false;
        boolean hasController = false;
        boolean hasTap = false;
        int tapCount = 0;
        int controllerCount = 0;
        int generatorBlockCount = 0;
        BlockPos pos;
        Block b;

        box_check:
        for (int x = xStart; x <= xEnd; ++x)
        {
            for (int z = zStart; z <= zEnd; ++z)
            {
                boolean isCornerPost = (x == xStart || x == xEnd) && (z == zStart || z == zEnd);

                for (int y = yStart; y >= yEnd; --y)
                {
                    pos = new BlockPos(x, y, z);

                    // A neighboring reactor's claimed space is never ours to judge, even
                    // if our remembered box happens to overlap it.
                    if (ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
                        continue;

                    b = level.getBlockState(pos).getBlock();

                    if (b == ModBlocks.STEEL_CONTROLLER.get())
                    {
                        hasController = true;
                        ++controllerCount;
                    }

                    if (b == ModBlocks.STEEL_POWERTAP.get())
                    {
                        BlockEntity te = level.getBlockEntity(pos);
                        if (te instanceof BlockEntityIRPowerTap tap)
                            tap.setController(this);

                        ++tapCount;
                        hasTap = true;
                        usingCreateTap = false;
                    }

                    if (ModCompat.CREATE_LOADED && b == CreateCompat.getPowerTapBlock())
                    {
                        CreateCompat.attachControllerIfPowerTap(level.getBlockEntity(pos), this);
                        ++tapCount;
                        hasTap = true;
                        usingCreateTap = true;
                    }

                    if (y == yStart || y == yEnd || isCornerPost)
                    {
                        if (!casingBlocks.contains(b))
                        {
                            if (isCornerPost)
                            {
                                // Break somewhere along a vertical corner post - give the
                                // pillar a grace period rather than failing outright.
                                PillarCheckResult result = this.checkPillarColumn(level, x, z);
                                if (result == PillarCheckResult.INVALID)
                                {
                                    this.wipeKnownBounds(level, "pillar at x=" + x + ", z=" + z
                                            + " no longer looks like a pillar (found a non-pillar"
                                            + " structure block, a core block, or nothing but air"
                                            + " while re-checking it)");
                                    return;
                                }

                                waitingOnPillarRepair = true;
                                isReactor = false;
                                continue;
                            }

                            isReactor = false;
                            this.applyResult(false, 0, 0, "known-bounds check failed at " + pos
                                    + " (top/bottom cap) - found " + b + ". Box was x[" + xStart + "," + xEnd
                                    + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd + "]",
                                    "Invalid block at " + posText(pos) + ": found " + blockName(b));
                            break box_check;
                        }
                    }
                    else if (x == xStart || x == xEnd || z == zStart || z == zEnd)
                    {
                        if (!structureBlocks.contains(b))
                        {
                            isReactor = false;
                            this.applyResult(false, 0, 0, "known-bounds check failed at " + pos
                                    + " (side wall, non-corner) - found " + b + ". Box was x[" + xStart + "," + xEnd
                                    + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd + "]",
                                    "Invalid block at " + posText(pos) + ": found " + blockName(b));
                            break box_check;
                        }
                    }
                    else
                    {
                        if (!generatorBlocks.contains(b))
                        {
                            isReactor = false;
                            this.applyResult(false, 0, 0, "known-bounds check failed at " + pos
                                    + " (interior) - found " + b + ". Box was x[" + xStart + "," + xEnd
                                    + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd + "]",
                                    "Invalid block at " + posText(pos) + ": found " + blockName(b));
                            break box_check;
                        }

                        if (b == ModBlocks.ISKALLIUM.get())
                            ++generatorBlockCount;
                    }
                }
            }
        }

        if (!isReactor)
        {
            if (waitingOnPillarRepair)
            {
                this.applyResult(false, 0, 0, "known-bounds check: one or more corner posts are"
                        + " mid-repair, waiting for them to be replaced before going back online",
                        "Waiting for a corner post to be repaired");
            }
            return;
        }

        if (!hasTap || !hasController || tapCount > 1 || controllerCount > 1)
        {
            String shortMsg;
            if (tapCount > 1)
                shortMsg = "Reactor can only have 1 Power Tap";
            else if (controllerCount > 1)
                shortMsg = "Reactor can only have 1 Controller";
            else if (!hasTap)
                shortMsg = "Missing a Power Tap block somewhere in the structure";
            else
                shortMsg = "Missing the Controller block somewhere in the structure";

            this.applyResult(false, 0, 0, "known-bounds box shape was valid but hasTap=" + hasTap
                    + " hasController=" + hasController + " tapCount=" + tapCount
                    + " controllerCount=" + controllerCount + " (need exactly one of each).",
                    shortMsg);
            return;
        }

        int size = (xEnd - xStart + 1) * (yStart - yEnd + 1) * (zEnd - zStart + 1);

        this.applyResult(true, generatorBlockCount, size, "known-bounds reactor re-validated. Box x["
                + xStart + "," + xEnd + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd
                + "] coreCount=" + generatorBlockCount + " reactorSize=" + size, "");
    }

    private enum PillarCheckResult { WAITING, INVALID }

    /**
     * Called when one of the 8 corner blocks (top or bottom of a vertical corner post)
     * is found missing/wrong during {@link #validateKnownBounds}. Walks the rest of that
     * pillar (excluding the two corner endpoints themselves - the "pillar pair") looking
     * for what's there:
     * <ul>
     *   <li>A core block (Iskallium), any non-pillar structure block (e.g. Iskallium
     *       Glass), or nothing but air the whole way down &rarr; this doesn't look like a
     *       pillar being repaired at all, so the known bounds are wiped and a fresh
     *       outward rescan will run next check.</li>
     *   <li>Otherwise, as long as at least one expected pillar block (casing/controller/
     *       power tap) remains somewhere in the column, this is treated as a pillar mid
     *       repair/extension and tolerated indefinitely - the reactor stays offline but
     *       keeps its known bounds and keeps re-checking on schedule.</li>
     * </ul>
     */
    private PillarCheckResult checkPillarColumn(Level level, int x, int z)
    {
        boolean foundPillarBlock = false;

        for (int y = yEnd + 1; y <= yStart - 1; ++y)
        {
            BlockPos pos = new BlockPos(x, y, z);
            Block b = level.getBlockState(pos).getBlock();

            if (b == ModBlocks.ISKALLIUM.get())
                return PillarCheckResult.INVALID;

            if (structureBlocks.contains(b) && !casingBlocks.contains(b))
                return PillarCheckResult.INVALID;

            if (casingBlocks.contains(b))
                foundPillarBlock = true;
        }

        return foundPillarBlock ? PillarCheckResult.WAITING : PillarCheckResult.INVALID;
    }

    /** Drops this controller's known bounds, forcing a full outward rescan on the next check. */
    private void wipeKnownBounds(Level level, String reason)
    {
        ReactorRegistry.unregister(level, this.getBlockPos());
        this.hasKnownBounds = false;
        this.applyResult(false, 0, 0, "known 8-corner bounds wiped: " + reason,
                "Reactor structure changed significantly - rescanning from scratch");
    }

    /**
     * Full outward corner-walk used whenever this controller has no known bounds yet
     * (first formation, or after a wipe). Any position already claimed by another
     * controller's known bounds is treated as "not structure" so two reactors built
     * side by side never walk into or merge with each other.
     */
    private void scanAndValidateFromScratch(Level level)
    {
        boolean isReactor = true;
        boolean hasController = false;
        boolean hasTap = false;
        int tapCount = 0;
        int controllerCount = 0;

        boolean checking = true;
        yStart = this.getBlockPos().getY();
        BlockPos pos = this.getBlockPos();
        Block b;

        while (checking)
        {
            pos = pos.above();
            b = level.getBlockState(pos).getBlock();
            if (this.structureBlocks.contains(b) && !ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
                yStart = pos.getY();
            else
                checking = false;
        }

        checking = true;
        pos = this.getBlockPos();
        yEnd = this.getBlockPos().getY();

        while (checking)
        {
            pos = pos.below();
            b = level.getBlockState(pos).getBlock();
            if (this.structureBlocks.contains(b) && !ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
                yEnd = pos.getY();
            else
                checking = false;
        }

        pos = new BlockPos(this.worldPosition.getX(), yStart, this.worldPosition.getZ());

        Direction f = null;

        for (Direction facing : Direction.Plane.HORIZONTAL)
        {
            b = level.getBlockState(pos.relative(facing)).getBlock();
            if (!structureBlocks.contains(b))
                f = facing;
        }

        if (f == null)
        {
            this.applyResult(false, 0, 0, "no single 'outward' direction found from controller at "
                    + this.getBlockPos() + " (yStart=" + yStart + ") - controller is probably on a"
                    + " corner post, or more than one neighbor at the top layer is non-structure."
                    + " Neighbor blocks: N=" + level.getBlockState(pos.north()).getBlock()
                    + " S=" + level.getBlockState(pos.south()).getBlock()
                    + " E=" + level.getBlockState(pos.east()).getBlock()
                    + " W=" + level.getBlockState(pos.west()).getBlock(),
                    "Controller's top layer must have exactly one open side facing out of the reactor");
            return;
        }

        f = f.getOpposite();

        checking = true;

        int x1 = pos.getX();
        int z1 = pos.getZ();
        int x2 = 0;
        int z2 = 0;
        int x3 = 0;
        int z3 = 0;
        int x4 = 0;
        int z4 = 0;

        while (checking)
        {
            pos = pos.relative(f);
            b = level.getBlockState(pos).getBlock();
            if (this.structureBlocks.contains(b) && !ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
            {
                x2 = pos.getX();
                z2 = pos.getZ();
            }
            else
            {
                checking = false;
            }
        }

        f = f.getClockWise();
        pos = new BlockPos(this.worldPosition.getX(), yStart, this.worldPosition.getZ());
        checking = true;

        while (checking)
        {
            pos = pos.relative(f);
            b = level.getBlockState(pos).getBlock();
            if (this.structureBlocks.contains(b) && !ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
            {
                x3 = pos.getX();
                z3 = pos.getZ();
            }
            else
            {
                checking = false;
            }
        }

        f = f.getOpposite();
        pos = new BlockPos(this.worldPosition.getX(), yStart, this.worldPosition.getZ());
        checking = true;

        while (checking)
        {
            pos = pos.relative(f);
            b = level.getBlockState(pos).getBlock();
            if (this.structureBlocks.contains(b) && !ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
            {
                x4 = pos.getX();
                z4 = pos.getZ();
            }
            else
            {
                checking = false;
            }
        }

        xStart = Math.min(Math.min(x1, x2), Math.min(x3, x4));
        xEnd = Math.max(Math.max(x1, x2), Math.max(x3, x4));

        zStart = Math.min(Math.min(z1, z2), Math.min(z3, z4));
        zEnd = Math.max(Math.max(z1, z2), Math.max(z3, z4));

        int width = xEnd - xStart + 1;
        int height = yStart - yEnd + 1;
        int depth = zEnd - zStart + 1;
        int minDim = zairus.iskalliumreactors.IRConfig.minReactorDimension;

        if (width < minDim || height < minDim || depth < minDim)
        {
            this.applyResult(false, 0, 0,
                    "reactor is " + width + "x" + height + "x" + depth + " which is smaller than the configured"
                            + " minimum of " + minDim + " on at least one axis - there's no room for a core."
                            + " Box was x[" + xStart + "," + xEnd + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd + "]",
                    "Reactor too small (" + width + "x" + height + "x" + depth + ", need at least "
                            + minDim + "x" + minDim + "x" + minDim + ")");
            return;
        }

        int generatorBlockCount = 0;

        box_check:
        for (int x = xStart; x <= xEnd; ++x)
        {
            for (int z = zStart; z <= zEnd; ++z)
            {
                for (int y = yStart; y >= yEnd; --y)
                {
                    pos = new BlockPos(x, y, z);

                    // Shouldn't normally occur here (the outward walk already stopped at
                    // claimed space), but skip defensively rather than judge a neighbor's blocks.
                    if (ReactorRegistry.isClaimedByAnother(level, pos, this.getBlockPos()))
                        continue;

                    b = level.getBlockState(pos).getBlock();

                    if (b == ModBlocks.STEEL_CONTROLLER.get())
                    {
                        hasController = true;
                        ++controllerCount;
                    }

                    if (b == ModBlocks.STEEL_POWERTAP.get())
                    {
                        BlockEntity te = level.getBlockEntity(pos);

                        if (te instanceof BlockEntityIRPowerTap tap)
                            tap.setController(this);

                        ++tapCount;
                        hasTap = true;
                        usingCreateTap = false;
                    }

                    if (ModCompat.CREATE_LOADED && b == CreateCompat.getPowerTapBlock())
                    {
                        CreateCompat.attachControllerIfPowerTap(level.getBlockEntity(pos), this);

                        ++tapCount;
                        hasTap = true;
                        usingCreateTap = true;
                    }

                    if (y == yStart
                            || y == yEnd
                            || (x == xStart && z == zStart)
                            || (x == xEnd && z == zEnd)
                            || (x == xStart && z == zEnd)
                            || (x == xEnd && z == zStart))
                    {
                        if (!casingBlocks.contains(b))
                        {
                            isReactor = false;
                            this.applyResult(false, 0, 0, "box_check failed at " + pos
                                    + " (top/bottom cap or corner post) - found " + b
                                    + " but only steel_casing/steel_controller/steel_powertap are"
                                    + " allowed there. Box was x[" + xStart + "," + xEnd
                                    + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd + "]",
                                    "Invalid block at " + posText(pos) + ": found " + blockName(b));
                            break box_check;
                        }
                    }
                    else if (x == xStart || x == xEnd || z == zStart || z == zEnd)
                    {
                        if (!structureBlocks.contains(b))
                        {
                            isReactor = false;
                            this.applyResult(false, 0, 0, "box_check failed at " + pos
                                    + " (side wall, non-corner) - found " + b
                                    + " but only casing/glass/controller/powertap are allowed there."
                                    + " Box was x[" + xStart + "," + xEnd + "] y[" + yStart + "," + yEnd
                                    + "] z[" + zStart + "," + zEnd + "]",
                                    "Invalid block at " + posText(pos) + ": found " + blockName(b));
                            break box_check;
                        }
                    }
                    else
                    {
                        if (!generatorBlocks.contains(b))
                        {
                            isReactor = false;
                            this.applyResult(false, 0, 0, "box_check failed at " + pos
                                    + " (interior) - found " + b
                                    + " but only the Iskallium essence block or air are allowed inside."
                                    + " Box was x[" + xStart + "," + xEnd + "] y[" + yStart + "," + yEnd
                                    + "] z[" + zStart + "," + zEnd + "]",
                                    "Invalid block at " + posText(pos) + ": found " + blockName(b));
                            break box_check;
                        }

                        if (b == ModBlocks.ISKALLIUM.get())
                            ++generatorBlockCount;
                    }
                }
            }
        }

        if (!isReactor)
            return;

        if (!hasTap || !hasController || tapCount > 1 || controllerCount > 1)
        {
            String shortMsg;
            if (tapCount > 1)
                shortMsg = "Reactor can only have 1 Power Tap";
            else if (controllerCount > 1)
                shortMsg = "Reactor can only have 1 Controller";
            else if (!hasTap)
                shortMsg = "Missing a Power Tap block somewhere in the structure";
            else
                shortMsg = "Missing the Controller block somewhere in the structure";

            this.applyResult(false, 0, 0, "box shape was valid but hasTap=" + hasTap
                    + " hasController=" + hasController + " tapCount=" + tapCount
                    + " controllerCount=" + controllerCount
                    + " (need exactly one of each). Box was x[" + xStart + "," + xEnd
                    + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd + "]",
                    shortMsg);
            return;
        }

        int size = (xEnd - xStart + 1) * (yStart - yEnd + 1) * (zEnd - zStart + 1);

        // Freeze the 8 corners we just derived: from now on this controller trusts
        // validateKnownBounds() instead of re-walking outward, and claims this space so
        // any neighboring reactor's scan excludes it.
        this.hasKnownBounds = true;
        ReactorRegistry.register(level, this.getBlockPos(), this.currentBounds());

        this.applyResult(true, generatorBlockCount, size, "reactor validated. Box x[" + xStart + "," + xEnd
                + "] y[" + yStart + "," + yEnd + "] z[" + zStart + "," + zEnd
                + "] coreCount=" + generatorBlockCount + " reactorSize=" + size,
                "");
    }

    private static String posText(BlockPos pos)
    {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    private static String blockName(Block b)
    {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(b).toString();
    }

    /**
     * The single place that sets the reactor's public state. Server-authoritative:
     * called only from checkStructure() on the logical server. Whenever the reactor
     * becomes invalid, all three values are reset together so nothing can read a
     * stale coreCount/reactorSize alongside isValidReactor=false.
     */
    private void applyResult(boolean valid, int core, int size, String logMessage, String shortSummary)
    {
        this.isValidReactor = valid;
        this.coreCount = valid ? core : 0;
        this.reactorSize = valid ? size : 0;
        this.issueSummary = valid ? "" : shortSummary;

        this.logIfChanged(valid, logMessage);

        this.setChanged();
        if (this.level != null && !this.level.isClientSide)
        {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void logIfChanged(boolean result, String message)
    {
        if (this.lastLoggedResult == null || this.lastLoggedResult != result)
        {
            this.lastLoggedResult = result;
            IskalliumReactors.logInfo("[IskalliumReactors] Controller at " + this.getBlockPos()
                    + ": " + message);
        }
    }
}
