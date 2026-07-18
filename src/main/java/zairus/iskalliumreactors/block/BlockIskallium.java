package zairus.iskalliumreactors.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import zairus.iskalliumreactors.IRConfig;

/**
 * The Iskallium essence block. Random-tick "melts" nearby stone into cobblestone, and
 * has a slippery, soft-landing feel similar to a slime block.
 */
public class BlockIskallium extends Block
{
    public BlockIskallium(Properties properties)
    {
        super(properties);
    }

    /**
     * Sound effect is a config-driven toggle (iskalliumOreSlimeSound): the classic squishy
     * Slime Block sound if enabled, otherwise a plain Stone sound by default. Resolved live
     * here (rather than baked into the block's Properties at registration time) so changing
     * the config and reloading takes effect without needing to restart with different
     * registered Properties.
     */
    @Override
    public SoundType getSoundType(BlockState state)
    {
        return IRConfig.iskalliumOreSlimeSound ? SoundType.SLIME_BLOCK : SoundType.STONE;
    }

    @Override
    public float getFriction(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, Entity entity)
    {
        return 0.98F;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        super.randomTick(state, level, pos, random);

        BlockPos targetPos = pos.offset(2 - random.nextInt(5), 2 - random.nextInt(5), 2 - random.nextInt(5));

        if (level.getBlockState(targetPos).is(Blocks.STONE))
        {
            level.setBlockAndUpdate(targetPos, Blocks.COBBLESTONE.defaultBlockState());
        }
    }

    @Override
    public void fallOn(net.minecraft.world.level.Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance)
    {
        if (entity.isCrouching())
        {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
        else
        {
            entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
        }
    }

    @Override
    public void updateEntityAfterFallOn(net.minecraft.world.level.BlockGetter level, Entity entity)
    {
        if (entity.isCrouching())
        {
            super.updateEntityAfterFallOn(level, entity);
        }
        else
        {
            Vec3 motion = entity.getDeltaMovement();
            if (motion.y < 0.0D)
            {
                double newY = -motion.y;
                if (!(entity instanceof LivingEntity))
                {
                    newY *= 0.8D;
                }
                entity.setDeltaMovement(motion.x, newY, motion.z);
            }
        }
    }
}
