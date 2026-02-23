package com.openblocks.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A tamed chest mob that follows its owner and picks up nearby items.
 */
public class LuggageEntity extends PathfinderMob implements MenuProvider {

    private final SimpleContainer inventory = new SimpleContainer(27);
    private UUID ownerUUID;
    private int lastSoundTick;

    public LuggageEntity(EntityType<? extends LuggageEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.7);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal());
        this.goalSelector.addGoal(3, new CollectItemGoal());
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    @Nullable
    public Player getOwner() {
        if (ownerUUID == null || level().isClientSide()) return null;
        return level().getPlayerByUUID(ownerUUID);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            // Convert back to item
            ItemStack stack = new ItemStack(com.openblocks.core.registry.OpenBlocksItems.LUGGAGE.get());
            saveInventoryToItem(stack);
            if (hasCustomName()) {
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, getCustomName());
            }
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            discard();
            return InteractionResult.SUCCESS;
        }

        // Open inventory
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : Component.translatable("entity.openblocks.luggage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return ChestMenu.threeRows(containerId, playerInv, inventory);
    }

    public void restoreFromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (tag.contains("Inventory")) {
            loadInventoryFromTag(tag.getList("Inventory", Tag.TAG_COMPOUND));
        }
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            setCustomName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
        }
    }

    private void saveInventoryToItem(ItemStack stack) {
        ListTag list = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                list.add(item.save(level().registryAccess(), slotTag));
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Inventory", list);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
    }

    private void loadInventoryFromTag(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slotTag = list.getCompound(i);
            int slot = slotTag.getByte("Slot") & 0xFF;
            if (slot < inventory.getContainerSize()) {
                inventory.setItem(slot, ItemStack.parseOptional(level().registryAccess(), slotTag));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag list = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                list.add(item.save(level().registryAccess(), slotTag));
            }
        }
        tag.put("Inventory", list);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Inventory")) {
            loadInventoryFromTag(tag.getList("Inventory", Tag.TAG_COMPOUND));
        }
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                spawnAtLocation(item);
            }
        }
        inventory.clearContent();
    }

    /**
     * Follow owner goal — similar to FollowOwnerGoal from TamableAnimal but
     * works with our custom owner system.
     */
    private class FollowOwnerGoal extends Goal {
        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || owner.isSpectator()) return false;
            return distanceToSqr(owner) > 16.0;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = getOwner();
            if (owner == null) return false;
            return distanceToSqr(owner) > 4.0;
        }

        @Override
        public void tick() {
            Player owner = getOwner();
            if (owner == null) return;

            getNavigation().moveTo(owner, 0.7);

            // Teleport if too far
            if (distanceToSqr(owner) > 144.0) {
                teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
        }
    }

    /**
     * Collect nearby items on the ground.
     */
    private class CollectItemGoal extends Goal {
        private ItemEntity targetItem;

        @Override
        public boolean canUse() {
            if (!getNavigation().isDone()) return false;

            AABB area = getBoundingBox().inflate(10);
            List<ItemEntity> items = level().getEntitiesOfClass(ItemEntity.class, area,
                    item -> item.isAlive() && !item.hasPickUpDelay() && canFitInInventory(item.getItem()));

            if (items.isEmpty()) return false;

            targetItem = items.getFirst();
            double closest = Double.MAX_VALUE;
            for (ItemEntity item : items) {
                double dist = distanceToSqr(item);
                if (dist < closest) {
                    closest = dist;
                    targetItem = item;
                }
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return targetItem != null && targetItem.isAlive() && distanceToSqr(targetItem) > 1.0;
        }

        @Override
        public void start() {
            if (targetItem != null) {
                getNavigation().moveTo(targetItem, 0.7);
            }
        }

        @Override
        public void tick() {
            if (targetItem == null || !targetItem.isAlive()) return;

            if (distanceToSqr(targetItem) < 1.5) {
                ItemStack remaining = addToInventory(targetItem.getItem());
                if (remaining.isEmpty()) {
                    targetItem.discard();
                } else {
                    targetItem.setItem(remaining);
                }
                if (lastSoundTick + 15 < tickCount) {
                    playSound(SoundEvents.GENERIC_EAT, 0.5f, 1.0f);
                    lastSoundTick = tickCount;
                }
                targetItem = null;
            } else {
                getNavigation().moveTo(targetItem, 0.7);
            }
        }

        @Override
        public void stop() {
            targetItem = null;
        }
    }

    private boolean canFitInInventory(ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack existing = inventory.getItem(i);
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() < existing.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private ItemStack addToInventory(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < inventory.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack existing = inventory.getItem(i);
            if (existing.isEmpty()) {
                inventory.setItem(i, remaining.copy());
                remaining = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, remaining.getCount());
                    existing.grow(transfer);
                    remaining.shrink(transfer);
                }
            }
        }
        return remaining;
    }
}
