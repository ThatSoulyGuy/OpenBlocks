package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shuffles the player's main inventory randomly.
 */
public class InventoryShuffleFlimFlam implements IFlimFlamEffect {

    @Override
    public boolean execute(ServerPlayer target) {
        // Only shuffle if no container is open
        if (target.containerMenu != target.inventoryMenu) return false;

        List<ItemStack> items = new ArrayList<>();
        int mainSize = target.getInventory().items.size();

        for (int i = 0; i < mainSize; i++) {
            items.add(target.getInventory().items.get(i).copy());
        }

        Collections.shuffle(items);

        for (int i = 0; i < mainSize; i++) {
            target.getInventory().items.set(i, items.get(i));
        }

        target.inventoryMenu.broadcastChanges();
        return true;
    }
}
