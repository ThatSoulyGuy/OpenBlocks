package com.openblocks.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * A mini copy of a player. Follows the player around looking cute.
 * Renders with the owner's skin on the client side.
 */
public class MiniMeEntity extends PathfinderMob {

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(MiniMeEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public MiniMeEntity(EntityType<? extends MiniMeEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    @Nullable
    public Player getOwner() {
        UUID uuid = getOwnerUUID();
        if (uuid == null) return null;
        return level().getPlayerByUUID(uuid);
    }

    @Override
    public boolean isBaby() {
        return true; // Always renders at half size
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID owner = getOwnerUUID();
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            setOwnerUUID(tag.getUUID("Owner"));
        }
    }

    /**
     * Follow owner goal.
     */
    private class FollowOwnerGoal extends Goal {
        @Override
        public boolean canUse() {
            Player owner = getOwner();
            if (owner == null || owner.isSpectator()) return false;
            return distanceToSqr(owner) > 9.0;
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

            getNavigation().moveTo(owner, 1.0);

            // Teleport if too far
            if (distanceToSqr(owner) > 144.0) {
                teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
        }
    }
}
