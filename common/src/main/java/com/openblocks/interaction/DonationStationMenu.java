package com.openblocks.interaction;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class DonationStationMenu extends OpenBlocksContainerMenu {

    private final Container container;

    public DonationStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1));
    }

    public DonationStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(OpenBlocksMenus.DONATION_STATION.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        // Single slot centered
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
