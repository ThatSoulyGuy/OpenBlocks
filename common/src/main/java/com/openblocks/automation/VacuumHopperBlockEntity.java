package com.openblocks.automation;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.FluidXpUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Vacuum Hopper block entity. Attracts nearby items and XP orbs,
 * storing items in a 10-slot inventory and XP as fluid.
 */
public class VacuumHopperBlockEntity extends OpenBlocksBlockEntity implements Container, MenuProvider {

    private static final double VACUUM_RANGE = 3.0;
    private static final double ABSORB_RANGE = 1.1;
    private static final double SUCTION_FORCE = 0.05;
    private static final int INVENTORY_SIZE = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int storedXpPoints = 0;
    private boolean vacuumEnabled = true;

    public VacuumHopperBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.VACUUM_HOPPER.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VacuumHopperBlockEntity be) {
        if (level.isClientSide()) return;
        if (!be.vacuumEnabled) return;

        Vec3 center = Vec3.atCenterOf(pos);
        AABB scanBox = new AABB(pos).inflate(VACUUM_RANGE);

        // Attract items
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, scanBox);
        for (ItemEntity itemEntity : nearbyItems) {
            if (itemEntity.isRemoved()) continue;

            double dist = itemEntity.position().distanceTo(center);

            if (dist < ABSORB_RANGE) {
                // Try to absorb
                ItemStack remaining = be.tryInsert(itemEntity.getItem());
                if (remaining.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(remaining);
                }
            } else {
                // Apply suction
                Vec3 dir = center.subtract(itemEntity.position()).normalize();
                itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(dir.scale(SUCTION_FORCE)));
            }
        }

        // Attract XP orbs
        List<ExperienceOrb> nearbyOrbs = level.getEntitiesOfClass(ExperienceOrb.class, scanBox);
        for (ExperienceOrb orb : nearbyOrbs) {
            if (orb.isRemoved()) continue;

            double dist = orb.position().distanceTo(center);

            if (dist < ABSORB_RANGE) {
                be.storedXpPoints += orb.getValue();
                orb.discard();
                be.setChanged();
            } else {
                Vec3 dir = center.subtract(orb.position()).normalize();
                orb.setDeltaMovement(orb.getDeltaMovement().add(dir.scale(SUCTION_FORCE)));
            }
        }
    }

    private ItemStack tryInsert(ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < items.size(); i++) {
            ItemStack slot = items.get(i);

            if (slot.isEmpty()) {
                items.set(i, remaining);
                setChanged();
                return ItemStack.EMPTY;
            }

            if (ItemStack.isSameItemSameComponents(slot, remaining)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, remaining.getCount());
                    slot.grow(transfer);
                    remaining.shrink(transfer);
                    setChanged();
                    if (remaining.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }

        return remaining;
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
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
        return Component.translatable("block.openblocks.vacuum_hopper");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new VacuumHopperMenu(containerId, playerInventory, this);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("StoredXP", storedXpPoints);
        tag.putBoolean("VacuumEnabled", vacuumEnabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        storedXpPoints = tag.getInt("StoredXP");
        vacuumEnabled = !tag.contains("VacuumEnabled") || tag.getBoolean("VacuumEnabled");
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        int nonEmpty = (int) items.stream().filter(s -> !s.isEmpty()).count();
        info.add("Items: " + nonEmpty + "/" + INVENTORY_SIZE);
        info.add("Stored XP: " + storedXpPoints);
        info.add("Vacuum: " + (vacuumEnabled ? "ON" : "OFF"));
        return info;
    }
}
