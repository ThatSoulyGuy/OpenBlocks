package com.openblocks.interaction;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.registry.OpenBlocksSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Bear trap that captures living entities when they step on it.
 * Closed traps hold entities in place until the trap is broken or the entity dies.
 */
public class BearTrapBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<BearTrapBlock> CODEC = simpleCodec(BearTrapBlock::new);
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    private static final VoxelShape SHAPE_OPEN = Block.box(0, 0, 0, 16, 3, 16);
    private static final VoxelShape SHAPE_CLOSED = Block.box(0, 0, 0, 16, 6, 16);

    public BearTrapBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CLOSED, false));
    }

    @Override
    protected MapCodec<? extends BearTrapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLOSED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(CLOSED) ? SHAPE_CLOSED : SHAPE_OPEN;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BearTrapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, OpenBlocksBlockEntities.BEAR_TRAP.get(),
                    (lvl, pos, st, be) -> be.serverTick());
        }
        return null;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && !state.getValue(CLOSED)) {
            if (level.getBlockEntity(pos) instanceof BearTrapBlockEntity trap) {
                trap.capture(living);
                level.setBlock(pos, state.setValue(CLOSED, true), 3);
                level.playSound(null, pos, OpenBlocksSounds.BEAR_TRAP_CLOSE.get(),
                        SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    @Override
    protected void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (level.getBlockEntity(pos) instanceof BearTrapBlockEntity trap) {
            trap.release();
            if (state.getValue(CLOSED)) {
                level.playSound(null, pos, OpenBlocksSounds.BEAR_TRAP_OPEN.get(),
                        SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(CLOSED) ? 15 : 0;
    }
}
