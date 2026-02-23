package com.openblocks.automation;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Item Dropper block entity. 9-slot inventory, drops items in the facing
 * direction with configurable speed when powered.
 */
public class ItemDropperBlockEntity extends OpenBlocksBlockEntity implements Container, MenuProvider {

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final SyncedValue<Double> dropSpeed = syncedDouble("dropSpeed", 1.0);
    private boolean wasPowered = false;

    public ItemDropperBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.ITEM_DROPPER.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public void onRedstoneChanged(boolean powered) {
        if (powered && !wasPowered) {
            dropItem();
        }
        wasPowered = powered;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ItemDropperBlockEntity be) {
        // Nothing to tick - dropping is event-driven via redstone
    }

    private void dropItem() {
        if (level == null || level.isClientSide()) return;

        Direction facing = getBlockState().getValue(ItemDropperBlock.FACING);

        // Find first non-empty slot
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            ItemStack toDrop = stack.split(1);
            setChanged();

            // Calculate spawn position and velocity
            Vec3 spawnPos = Vec3.atCenterOf(worldPosition)
                    .add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.7));

            double speed = Math.min(dropSpeed.get(), OpenBlocksConfig.ItemDropper.maxDropSpeed);
            Vec3 velocity = Vec3.atLowerCornerOf(facing.getNormal()).scale(speed * 0.2);

            ItemEntity entity = new ItemEntity(level,
                    spawnPos.x, spawnPos.y, spawnPos.z, toDrop);
            entity.setDeltaMovement(velocity);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);

            return;
        }
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return 9;
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
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.openblocks.item_dropper");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ItemDropperMenu(containerId, playerInventory, this);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        int nonEmpty = (int) items.stream().filter(s -> !s.isEmpty()).count();
        info.add("Items: " + nonEmpty + "/9");
        info.add("Speed: " + dropSpeed.get());
        return info;
    }
}
