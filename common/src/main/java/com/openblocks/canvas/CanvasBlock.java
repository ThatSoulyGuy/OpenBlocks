package com.openblocks.canvas;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.imaginary.StencilItem;
import com.openblocks.imaginary.StencilPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A full opaque canvas block that can be painted per-face with a paint brush.
 * Stencils can be placed on faces to mask paint application.
 */
public class CanvasBlock extends BaseEntityBlock {

    public static final MapCodec<CanvasBlock> CODEC = simpleCodec(CanvasBlock::new);

    public CanvasBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return OpenBlocksBlockEntities.CANVAS.get().create(pos, state);
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
        if (!(be instanceof CanvasBlockEntity canvas)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Direction face = hit.getDirection();

        // Stencil interaction — place stencil on face
        if (stack.getItem() instanceof StencilItem) {
            StencilPattern pattern = StencilItem.getPattern(stack);
            if (pattern != null) {
                if (canvas.placeStencil(face, pattern)) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        // Paint brush interaction
        if (stack.getItem() instanceof PaintBrushItem) {
            int color = PaintBrushItem.getColor(stack);
            if (color != 0) {
                Direction[] faces = player.isShiftKeyDown() ? Direction.values() : new Direction[]{face};
                boolean painted = false;
                for (Direction f : faces) {
                    painted |= canvas.applyPaint(color, f);
                }
                if (painted) {
                    level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                            ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                            : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        // Squeegee interaction — also removes stencil covers
        if (stack.getItem() instanceof SqueegeeItem) {
            if (player.isShiftKeyDown()) {
                // Drop any stencil covers before clearing
                for (Direction d : Direction.values()) {
                    dropStencilCover(canvas, d, level, pos);
                }
                canvas.clearAllFaces();
            } else {
                dropStencilCover(canvas, face, level, pos);
                canvas.clearFace(face);
            }
            level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CanvasBlockEntity canvas)) return InteractionResult.PASS;

        Direction face = hit.getDirection();

        // Sneak + empty hand on face with stencil cover — remove stencil
        if (player.isShiftKeyDown() && canvas.hasStencilCover(face)) {
            dropStencilCover(canvas, face, level, pos);
            canvas.removeStencilCover(face);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void dropStencilCover(CanvasBlockEntity canvas, Direction face, Level level, BlockPos pos) {
        StencilPattern cover = canvas.hasStencilCover(face) ? canvas.getStencil(face) : null;
        if (cover != null) {
            ItemStack stencilStack = StencilItem.createStencil(cover);
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stencilStack);
        }
    }
}
