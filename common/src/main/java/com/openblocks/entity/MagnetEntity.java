package com.openblocks.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * A magnet entity that hovers below its owner and can attract nearby items.
 * Simplified implementation of the original crane magnet system.
 */
public class MagnetEntity extends Entity {

    private UUID ownerUUID;
    private double magnetDistance = 3.0; // Distance below owner
    private boolean attracting = true;

    public MagnetEntity(EntityType<? extends MagnetEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(Player owner) {
        this.ownerUUID = owner.getUUID();
        setPos(owner.getX(), owner.getY() - magnetDistance, owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;

        Player owner = ownerUUID != null ? level().getPlayerByUUID(ownerUUID) : null;
        if (owner == null || owner.isRemoved()) {
            discard();
            return;
        }

        // Follow owner position
        double targetX = owner.getX();
        double targetY = owner.getY() - magnetDistance;
        double targetZ = owner.getZ();

        Vec3 current = position();
        Vec3 target = new Vec3(targetX, targetY, targetZ);
        Vec3 diff = target.subtract(current);
        double dist = diff.length();

        if (dist > 0.1) {
            Vec3 move = diff.normalize().scale(Math.min(0.5, dist));
            setPos(current.x + move.x, current.y + move.y, current.z + move.z);
        }

        // Attract nearby items
        if (attracting && tickCount % 5 == 0) {
            attractItems();
        }
    }

    private void attractItems() {
        AABB area = getBoundingBox().inflate(3);
        List<ItemEntity> items = level().getEntitiesOfClass(ItemEntity.class, area);

        for (ItemEntity item : items) {
            Vec3 diff = position().subtract(item.position());
            double dist = diff.length();
            if (dist > 0.5) {
                Vec3 pull = diff.normalize().scale(0.1);
                item.setDeltaMovement(item.getDeltaMovement().add(pull));
            }
        }
    }

    public void toggleAttracting() {
        attracting = !attracting;
    }

    public void adjustDistance(double delta) {
        magnetDistance = Math.max(1.0, Math.min(10.0, magnetDistance + delta));
    }

    @Override
    public boolean shouldBeSaved() {
        return false; // Recreated by crane backpack
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        magnetDistance = tag.getDouble("MagnetDistance");
        attracting = tag.getBoolean("Attracting");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        tag.putDouble("MagnetDistance", magnetDistance);
        tag.putBoolean("Attracting", attracting);
    }
}
