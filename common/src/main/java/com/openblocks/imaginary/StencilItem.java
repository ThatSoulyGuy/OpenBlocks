package com.openblocks.imaginary;

import com.openblocks.core.registry.OpenBlocksItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * A patterned stencil item. The pattern is stored in CustomData.
 */
public class StencilItem extends Item {

    public StencilItem(Properties properties) {
        super(properties);
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
        return stack;
    }
}
