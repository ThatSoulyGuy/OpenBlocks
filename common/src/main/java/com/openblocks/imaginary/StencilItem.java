package com.openblocks.imaginary;

import com.openblocks.canvas.CanvasBlock;
import com.openblocks.canvas.CanvasBlockEntity;
import com.openblocks.core.registry.OpenBlocksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * A patterned stencil item. The pattern is stored in CustomData.
 * Right-click on a canvas block to place the stencil on a face.
 */
public class StencilItem extends Item {

    public StencilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        // Only works on canvas blocks
        if (!(state.getBlock() instanceof CanvasBlock)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CanvasBlockEntity canvas)) {
            return InteractionResult.PASS;
        }

        StencilPattern pattern = getPattern(stack);
        if (pattern == null) {
            return InteractionResult.PASS;
        }

        if (canvas.placeStencil(face, pattern)) {
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        StencilPattern pattern = getPattern(stack);
        if (pattern != null) {
            tooltipComponents.add(Component.translatable("openblocks.stencil." + pattern.getSerializedName()));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        StencilPattern pattern = getPattern(stack);
        if (pattern != null) {
            return Component.translatable("item.openblocks.stencil.patterned",
                    Component.translatable("openblocks.stencil." + pattern.getSerializedName()));
        }
        return super.getName(stack);
    }

    public static StencilPattern getPattern(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Pattern")) {
            int ordinal = tag.getInt("Pattern");
            StencilPattern[] values = StencilPattern.values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
        }
        return null;
    }

    public static ItemStack createStencil(StencilPattern pattern) {
        ItemStack stack = new ItemStack(OpenBlocksItems.STENCIL.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("Pattern", pattern.ordinal());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(pattern.ordinal() + 1));
        return stack;
    }
}
