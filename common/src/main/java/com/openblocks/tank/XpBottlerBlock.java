package com.openblocks.tank;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * XP Bottler block. Place on top of a tank containing liquid XP.
 * Automatically converts glass bottles into bottles o' enchanting
 * by consuming liquid XP from the tank below.
 */
public class XpBottlerBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<XpBottlerBlock> CODEC = simpleCodec(XpBottlerBlock::new);

    public XpBottlerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XpBottlerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :
                createTickerHelper(type, OpenBlocksBlockEntities.XP_BOTTLER.get(), XpBottlerBlockEntity::tick);
    }

    @Override
    protected void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (level.getBlockEntity(pos) instanceof XpBottlerBlockEntity bottler) {
            Containers.dropContents(level, pos, bottler.getItems());
        }
    }
}
