package com.openblocks.imaginary;

import com.openblocks.core.container.OpenBlocksContainerMenu;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for the drawing table.
 */
public class DrawingTableMenu extends OpenBlocksContainerMenu {

    private final Container container;
    private final ContainerData data;

    // Client constructor
    public DrawingTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(1));
    }

    // Server constructor
    public DrawingTableMenu(int containerId, Inventory playerInventory, DrawingTableBlockEntity be) {
        this(containerId, playerInventory, be, be.getDataAccess());
    }

    private DrawingTableMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(OpenBlocksMenus.DRAWING_TABLE.get(), containerId);
        this.container = container;
        this.data = data;

        // Input slot (left)
        addSlot(new Slot(container, DrawingTableBlockEntity.SLOT_INPUT, 48, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof UnpreparedStencilItem;
            }
        });

        // Output slot (right) — take only
        addSlot(new Slot(container, DrawingTableBlockEntity.SLOT_OUTPUT, 112, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventorySlots(playerInventory, 8, 84);
        addDataSlots(data);
    }

    public int getSelectedPatternOrdinal() {
        return data.get(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
