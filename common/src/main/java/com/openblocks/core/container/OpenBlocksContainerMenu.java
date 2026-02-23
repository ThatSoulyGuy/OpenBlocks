package com.openblocks.core.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Base container menu replacing OpenModsLib's ContainerBase.
 * Provides common player inventory slot layout and shift-click logic.
 */
public abstract class OpenBlocksContainerMenu extends AbstractContainerMenu {

    protected static final int PLAYER_INVENTORY_SIZE = 36;

    protected OpenBlocksContainerMenu(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    /**
     * Adds the standard 3x9 player inventory + 1x9 hotbar slots.
     * Call this after adding all custom slots.
     *
     * @param inventory Player inventory
     * @param xOffset  X offset for the top-left of the inventory grid
     * @param yOffset  Y offset for the top-left of the inventory grid
     */
    protected void addPlayerInventorySlots(Inventory inventory, int xOffset, int yOffset) {
        // Main inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
            }
        }
        // Hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, xOffset + col * 18, yOffset + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            int containerSlots = slots.size() - PLAYER_INVENTORY_SIZE;

            if (slotIndex < containerSlots) {
                // Move from container to player inventory
                if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to container
                if (!moveItemStackTo(stack, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }
}
