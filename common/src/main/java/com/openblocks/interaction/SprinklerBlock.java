package com.openblocks.interaction;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Sprinkler block that waters nearby crops. Consumes water from a tank below it
 * and optionally uses bonemeal from its internal inventory for faster growth.
 * Placed on top of a tank block.
 */
public class SprinklerBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<SprinklerBlock> CODEC = simpleCodec(SprinklerBlock::new);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public SprinklerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SprinklerBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SprinklerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, OpenBlocksBlockEntities.SPRINKLER.get(),
                    (lvl, pos, st, be) -> be.serverTick());
        }
        return null;
    }

    @Override
    protected void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (level.getBlockEntity(pos) instanceof SprinklerBlockEntity sprinkler) {
            Containers.dropContents(level, pos, sprinkler.getItems());
        }
    }
}
