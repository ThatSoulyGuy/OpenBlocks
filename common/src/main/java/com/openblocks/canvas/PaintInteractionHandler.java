package com.openblocks.canvas;

import com.openblocks.imaginary.StencilItem;
import com.openblocks.imaginary.StencilPattern;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Global right-click handler for painting any non-canvas block.
 * Registered on InteractionEvent.RIGHT_CLICK_BLOCK.
 */
public final class PaintInteractionHandler {

    public static EventResult onRightClickBlock(Player player, InteractionHand hand,
                                                 BlockPos pos, Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        Level level = player.level();
        BlockState state = level.getBlockState(pos);

        // Skip canvas blocks — let CanvasBlock.useItemOn handle those
        if (state.getBlock() instanceof CanvasBlock) return EventResult.pass();
        // Skip canvas glass too
        if (state.getBlock() instanceof CanvasGlassBlock) return EventResult.pass();

        if (stack.getItem() instanceof PaintBrushItem) {
            return handlePaintBrush(player, hand, pos, face, stack, state, level);
        }
        if (stack.getItem() instanceof StencilItem) {
            return handleStencil(player, hand, pos, face, stack, state, level);
        }
        if (stack.getItem() instanceof SqueegeeItem) {
            return handleSqueegee(player, hand, pos, face, stack, state, level);
        }

        return EventResult.pass();
    }

    private static EventResult handlePaintBrush(Player player, InteractionHand hand,
                                                  BlockPos pos, Direction face,
                                                  ItemStack stack, BlockState state, Level level) {
        int color = PaintBrushItem.getColor(stack);
        if (color == 0) return EventResult.pass();
        if (level.isClientSide()) return EventResult.interruptTrue();

        ServerLevel serverLevel = (ServerLevel) level;
        PaintSavedData data = PaintSavedData.get(serverLevel);

        Direction[] facesToPaint;
        if (player.isShiftKeyDown()) {
            facesToPaint = Direction.values();
        } else {
            facesToPaint = new Direction[]{face};
        }

        PaintEntry entry = data.getOrCreate(pos);
        boolean painted = false;
        for (Direction f : facesToPaint) {
            painted |= entry.applyPaint(color, f);
        }

        if (painted) {
            if (entry.isEmpty()) {
                data.remove(pos);
            } else {
                data.setDirty();
            }
            broadcastPaintUpdate(serverLevel, pos, entry);
            level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }

        return EventResult.interruptTrue();
    }

    private static EventResult handleStencil(Player player, InteractionHand hand,
                                               BlockPos pos, Direction face,
                                               ItemStack stack, BlockState state, Level level) {
        // Stencils only work on full-cube blocks
        if (!state.isCollisionShapeFullBlock(level, pos)) return EventResult.pass();
        if (level.isClientSide()) return EventResult.interruptTrue();

        StencilPattern pattern = StencilItem.getPattern(stack);
        if (pattern == null) return EventResult.pass();

        ServerLevel serverLevel = (ServerLevel) level;
        PaintSavedData data = PaintSavedData.get(serverLevel);
        PaintEntry entry = data.getOrCreate(pos);

        if (entry.placeStencil(face, pattern)) {
            data.setDirty();
            broadcastPaintUpdate(serverLevel, pos, entry);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
        }

        return EventResult.interruptTrue();
    }

    private static EventResult handleSqueegee(Player player, InteractionHand hand,
                                                BlockPos pos, Direction face,
                                                ItemStack stack, BlockState state, Level level) {
        if (level.isClientSide()) return EventResult.interruptTrue();

        ServerLevel serverLevel = (ServerLevel) level;
        PaintSavedData data = PaintSavedData.get(serverLevel);
        PaintEntry entry = data.get(pos);
        if (entry == null) return EventResult.pass();

        if (player.isShiftKeyDown()) {
            // Drop all stencil covers, then clear all faces
            for (Direction d : Direction.values()) {
                dropStencilCover(entry, d, level, pos);
            }
            entry.clearAllFaces();
        } else {
            dropStencilCover(entry, face, level, pos);
            entry.clearFace(face);
        }

        if (entry.isEmpty()) {
            data.remove(pos);
        } else {
            data.setDirty();
        }
        broadcastPaintUpdate(serverLevel, pos, entry);
        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);

        return EventResult.interruptTrue();
    }

    public static void broadcastPaintUpdate(ServerLevel level, BlockPos pos, PaintEntry entry) {
        PaintUpdateS2CPacket packet = PaintUpdateS2CPacket.fromEntry(pos, entry);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64 * 64) {
                NetworkManager.sendToPlayer(player, packet);
            }
        }
    }

    private static void dropStencilCover(PaintEntry entry, Direction face, Level level, BlockPos pos) {
        if (entry.hasStencilCover(face)) {
            StencilPattern pattern = entry.getStencil(face);
            if (pattern != null) {
                ItemStack stencilStack = StencilItem.createStencil(pattern);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stencilStack);
            }
        }
    }

    private PaintInteractionHandler() {}
}
