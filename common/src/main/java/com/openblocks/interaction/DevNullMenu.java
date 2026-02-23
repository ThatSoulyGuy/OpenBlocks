package com.openblocks.interaction;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

/**
 * Menu for the /dev/null item. Provides a single slot for setting
 * the filter item that will be auto-consumed on pickup.
 */
public class DevNullMenu extends OpenBlocksContainerMenu {

    private final Container container;

    public DevNullMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1));
    }

    public DevNullMenu(int containerId, Inventory playerInventory, Container container) {
        super(OpenBlocksMenus.DEV_NULL.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        // Single filter slot centered
        addSlot(new Slot(container, 0, 80, 35));

        addPlayerInventorySlots(playerInventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
