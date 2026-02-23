package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Forces the player to drop held items and some armor with 50% chance per piece.
 */
public class ItemDropFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();
    private static final EquipmentSlot[] DROP_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.MAINHAND
    };

    @Override
    public boolean execute(ServerPlayer target) {
        boolean dropped = false;
        for (EquipmentSlot slot : DROP_SLOTS) {
            if (RANDOM.nextBoolean()) {
                ItemStack stack = target.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    target.drop(stack.copy(), true, false);
                    target.setItemSlot(slot, ItemStack.EMPTY);
                    dropped = true;
                }
            }
        }
        return dropped;
    }
}
