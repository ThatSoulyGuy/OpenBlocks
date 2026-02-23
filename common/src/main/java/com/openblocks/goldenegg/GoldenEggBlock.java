package com.openblocks.goldenegg;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The golden egg block. Once placed, it progresses through a state machine
 * that eventually hatches a MiniMe entity.
 */
public class GoldenEggBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<GoldenEggBlock> CODEC = simpleCodec(GoldenEggBlock::new);

    public GoldenEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldenEggBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, OpenBlocksBlockEntities.GOLDEN_EGG.get(),
                    GoldenEggBlockEntity::clientTick);
        } else {
            return createTickerHelper(type, OpenBlocksBlockEntities.GOLDEN_EGG.get(),
                    GoldenEggBlockEntity::serverTick);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GoldenEggBlockEntity egg) {
            egg.setPlacedBy(placer);
        }
    }
}
