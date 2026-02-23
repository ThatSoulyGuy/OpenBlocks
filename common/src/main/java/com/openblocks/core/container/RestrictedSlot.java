package com.openblocks.core.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * A slot that only accepts items matching a predicate.
 * Replaces OpenModsLib's RestrictedSlot.
 */
public class RestrictedSlot extends Slot {

    private final Predicate<ItemStack> validator;

    public RestrictedSlot(Container container, int index, int x, int y, Predicate<ItemStack> validator) {
        super(container, index, x, y);
        this.validator = validator;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return validator.test(stack);
    }
}
