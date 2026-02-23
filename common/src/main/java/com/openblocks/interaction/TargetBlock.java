package com.openblocks.interaction;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Target block that emits a redstone signal when hit by a projectile.
 * Signal strength (0-15) depends on how close to the center the projectile hit.
 */
public class TargetBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<TargetBlock> CODEC = simpleCodec(TargetBlock::new);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public TargetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends TargetBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TargetBlockEntity(pos, state);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            BlockPos pos = hit.getBlockPos();
            Vec3 hitLocation = hit.getLocation();

            // Calculate accuracy: distance from center of hit face
            Vec3 center = Vec3.atCenterOf(pos);
            double dx = hitLocation.x - center.x;
            double dy = hitLocation.y - center.y;
            double dz = hitLocation.z - center.z;

            // Use the two axes perpendicular to the hit face
            Direction face = hit.getDirection();
            double dist;
            if (face.getAxis() == Direction.Axis.X) {
                dist = Math.sqrt(dy * dy + dz * dz);
            } else if (face.getAxis() == Direction.Axis.Y) {
                dist = Math.sqrt(dx * dx + dz * dz);
            } else {
                dist = Math.sqrt(dx * dx + dy * dy);
            }

            // Max distance from center to corner is ~0.707, map to 1-15 signal
            double maxDist = 0.707;
            int signalStrength = (int) Math.max(1, Math.round(15.0 * (1.0 - Math.min(dist / maxDist, 1.0))));

            if (level.getBlockEntity(pos) instanceof TargetBlockEntity target) {
                target.setSignalStrength(signalStrength);
            }

            level.setBlock(pos, state.setValue(POWERED, true), 3);
            level.updateNeighborsAt(pos, this);

            // Schedule un-power after 20 ticks (1 second)
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            level.updateNeighborsAt(pos, this);
            if (level.getBlockEntity(pos) instanceof TargetBlockEntity target) {
                target.setSignalStrength(0);
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!state.getValue(POWERED)) return 0;
        if (level.getBlockEntity(pos) instanceof TargetBlockEntity target) {
            return target.getSignalStrength();
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }
}
