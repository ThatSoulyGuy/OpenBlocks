package com.openblocks.automation;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class BlockPlacerMenu extends OpenBlocksContainerMenu {

    private final Container container;

    public BlockPlacerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(9));
    }

    public BlockPlacerMenu(int containerId, Inventory playerInventory, Container container) {
        super(OpenBlocksMenus.BLOCK_PLACER.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        // 3x3 grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(container, col + row * 3, 62 + col * 18, 17 + row * 18));
            }
        }

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
