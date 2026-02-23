package com.openblocks.utility;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A crafting ingredient that removes enchantments and lore from items.
 * Has an enchanted visual effect (glint). Has durability — each craft use
 * consumes one point of durability.
 */
public class EpicEraserItem extends Item {

    public EpicEraserItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
