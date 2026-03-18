package com.openblocks.imaginary;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Drawing table block for crafting stencils from unprepared stencils.
 */
public class DrawingTableBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<DrawingTableBlock> CODEC = simpleCodec(DrawingTableBlock::new);

    public DrawingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrawingTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof DrawingTableBlockEntity table
                && player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, table, buf -> buf.writeBlockPos(pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (level.getBlockEntity(pos) instanceof DrawingTableBlockEntity table) {
            Containers.dropContents(level, pos, table);
        }
    }
}
