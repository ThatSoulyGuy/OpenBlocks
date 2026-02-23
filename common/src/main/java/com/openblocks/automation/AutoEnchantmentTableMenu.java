package com.openblocks.automation;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.container.RestrictedSlot;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

public class AutoEnchantmentTableMenu extends OpenBlocksContainerMenu {

    private final Container container;

    public AutoEnchantmentTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(3));
    }

    public AutoEnchantmentTableMenu(int containerId, Inventory playerInventory, Container container) {
        super(OpenBlocksMenus.AUTO_ENCHANTMENT_TABLE.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, 0, 27, 35));           // Item to enchant
        addSlot(new RestrictedSlot(container, 1, 76, 35, stack -> stack.is(Items.LAPIS_LAZULI))); // Lapis
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
