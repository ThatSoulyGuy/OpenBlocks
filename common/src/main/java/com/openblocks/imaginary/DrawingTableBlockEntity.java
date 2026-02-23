package com.openblocks.imaginary;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.registry.OpenBlocksMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the drawing table. Has 2 slots:
 * - Slot 0: input (unprepared stencils)
 * - Slot 1: output (patterned stencils, auto-generated)
 */
public class DrawingTableBlockEntity extends OpenBlocksBlockEntity implements Container, MenuProvider {

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final SyncedValue<StencilPattern> selectedPattern = syncedEnum("pattern", StencilPattern.CREEPER_FACE);

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) return selectedPattern.get().ordinal();
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                StencilPattern[] values = StencilPattern.values();
                if (value >= 0 && value < values.length) {
                    selectedPattern.set(values[value]);
                }
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public DrawingTableBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.DRAWING_TABLE.get(), pos, state);
    }

    public StencilPattern getSelectedPattern() {
        return selectedPattern.get();
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public void cyclePatternUp() {
        selectedPattern.set(selectedPattern.get().next());
        updateOutput();
        sync();
    }

    public void cyclePatternDown() {
        selectedPattern.set(selectedPattern.get().prev());
        updateOutput();
        sync();
    }

    private void updateOutput() {
        ItemStack input = items.get(SLOT_INPUT);
        if (!input.isEmpty() && input.getItem() instanceof UnpreparedStencilItem) {
            items.set(SLOT_OUTPUT, StencilItem.createStencil(selectedPattern.get()));
        } else {
            items.set(SLOT_OUTPUT, ItemStack.EMPTY);
        }
        setChanged();
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (slot == SLOT_OUTPUT && !result.isEmpty()) {
            // Consume input when output is taken
            ItemStack input = items.get(SLOT_INPUT);
            if (!input.isEmpty()) {
                input.shrink(1);
            }
            updateOutput();
        }
        if (slot == SLOT_INPUT) {
            updateOutput();
        }
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) return; // Output is read-only
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        updateOutput();
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) return false;
        return stack.getItem() instanceof UnpreparedStencilItem;
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.openblocks.drawing_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DrawingTableMenu(containerId, playerInventory, this);
    }

    public BlockPos getBlockPos() {
        return worldPosition;
    }

    // --- Persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        updateOutput();
    }
}
