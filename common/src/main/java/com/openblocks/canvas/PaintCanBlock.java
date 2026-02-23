package com.openblocks.canvas;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A paint can block that stores a single color.
 * Right-click with a paint brush to dip it in the can.
 */
public class PaintCanBlock extends BaseEntityBlock {

    public static final MapCodec<PaintCanBlock> CODEC = simpleCodec(PaintCanBlock::new);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 11, 12);

    public PaintCanBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return OpenBlocksBlockEntities.PAINT_CAN.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand,
                                               BlockHitResult hit) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PaintCanBlockEntity paintCan)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Dip paint brush into paint can
        if (stack.getItem() instanceof PaintBrushItem) {
            if (paintCan.getAmount() > 0) {
                PaintBrushItem.setColor(stack, 0xFF000000 | paintCan.getColor());
                paintCan.consumeUse();
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.5f, 1.2f);

                // Remove paint can if empty
                if (paintCan.getAmount() <= 0) {
                    level.removeBlock(pos, false);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
