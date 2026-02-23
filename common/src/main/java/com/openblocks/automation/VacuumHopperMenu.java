package com.openblocks.automation;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class VacuumHopperMenu extends OpenBlocksContainerMenu {

    private final Container container;

    public VacuumHopperMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(10));
    }

    public VacuumHopperMenu(int containerId, Inventory playerInventory, Container container) {
        super(OpenBlocksMenus.VACUUM_HOPPER.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        // 2 rows x 5 cols
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                addSlot(new Slot(container, col + row * 5, 44 + col * 18, 18 + row * 18));
            }
        }

        addPlayerInventorySlots(playerInventory, 8, 66);
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
