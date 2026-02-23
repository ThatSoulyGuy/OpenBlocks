package com.openblocks.core.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for blocks with BlockEntities, replacing OpenModsLib's OpenBlock
 * with built-in tile entity support. Provides common patterns for entity blocks.
 */
public abstract class OpenBlocksEntityBlock extends BaseEntityBlock {

    protected OpenBlocksEntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            onBlockRemoved(state, level, pos, newState);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Called when the block is removed (different block type replaces it).
     * Override to handle inventory drops, etc.
     */
    protected void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState) {
    }
}
