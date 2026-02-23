package com.openblocks.automation;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.container.RestrictedSlot;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class AutoAnvilMenu extends OpenBlocksContainerMenu {

    private final Container container;

    public AutoAnvilMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(3));
    }

    public AutoAnvilMenu(int containerId, Inventory playerInventory, Container container) {
        super(OpenBlocksMenus.AUTO_ANVIL.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, 0, 27, 35));           // Tool
        addSlot(new Slot(container, 1, 76, 35));           // Modifier
        addSlot(new RestrictedSlot(container, 2, 134, 35, stack -> false)); // Output

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
