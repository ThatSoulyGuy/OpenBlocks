package com.openblocks.interaction;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Village highlighter block. Detects nearby village structures and
 * emits a redstone signal proportional to village size / door count.
 * In 1.21.1 this uses POI-based detection since villages changed in 1.14+.
 */
public class VillageHighlighterBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<VillageHighlighterBlock> CODEC = simpleCodec(VillageHighlighterBlock::new);

    public VillageHighlighterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VillageHighlighterBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VillageHighlighterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, OpenBlocksBlockEntities.VILLAGE_HIGHLIGHTER.get(),
                    (lvl, pos, st, be) -> be.serverTick());
        }
        return null;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof VillageHighlighterBlockEntity vh) {
            return vh.getSignalStrength();
        }
        return 0;
    }
}
