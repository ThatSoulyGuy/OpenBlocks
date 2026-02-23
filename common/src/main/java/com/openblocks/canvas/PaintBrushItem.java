package com.openblocks.canvas;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * A paint brush that stores a color and can apply it to canvas blocks.
 * Load color by right-clicking a paint can. Apply by right-clicking canvas blocks.
 * 24 uses per dip.
 */
public class PaintBrushItem extends Item {

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    /**
     * Get the stored color from the brush (ARGB). Returns 0 if no color.
     */
    public static int getColor(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Color")) {
            return tag.getInt("Color");
        }
        return 0;
    }

    /**
     * Set the stored color on the brush and reset durability.
     */
    public static void setColor(ItemStack stack, int color) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("Color", color);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        // Reset damage so brush is fully loaded
        stack.setDamageValue(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int color = getColor(stack);
        if (color != 0) {
            tooltip.add(Component.literal(String.format("#%06X", color & 0xFFFFFF))
                    .withStyle(style -> style.withColor(color & 0xFFFFFF)));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getColor(stack) != 0;
    }
}
