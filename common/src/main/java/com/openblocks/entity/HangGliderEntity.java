package com.openblocks.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A hang glider entity that attaches to a player and provides gliding physics.
 * Not saved to disk — recreated when player uses the hang glider item.
 */
public class HangGliderEntity extends Entity {

    private static final double VSPEED_NORMAL = -0.052;
    private static final double VSPEED_FAST = -0.176;
    private static final double VSPEED_MIN = -0.32;

    private static final EntityDataAccessor<Boolean> DATA_DEPLOYED =
            SynchedEntityData.defineId(HangGliderEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_PLAYER_ID =
            SynchedEntityData.defineId(HangGliderEntity.class, EntityDataSerializers.INT);

    public static final Map<Player, HangGliderEntity> GLIDER_MAP = new WeakHashMap<>();

    private Player player;
    private double lastMotionY;

    public HangGliderEntity(EntityType<? extends HangGliderEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setPlayer(Player player) {
        this.player = player;
        entityData.set(DATA_PLAYER_ID, player.getId());
        GLIDER_MAP.put(player, this);
        syncPosition();
    }

    public Player getPlayer() {
        if (player == null && level().isClientSide()) {
            int id = entityData.get(DATA_PLAYER_ID);
            if (id != -1 && level().getEntity(id) instanceof Player p) {
                player = p;
                GLIDER_MAP.put(player, this);
            }
        }
        return player;
    }

    public boolean isDeployed() {
        return entityData.get(DATA_DEPLOYED);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DEPLOYED, false);
        builder.define(DATA_PLAYER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();

        // Resolve player on client from synced entity ID
        if (player == null && level().isClientSide()) {
            int id = entityData.get(DATA_PLAYER_ID);
            if (id != -1 && level().getEntity(id) instanceof Player p) {
                player = p;
                GLIDER_MAP.put(player, this);
            }
        }

        if (player == null || player.isRemoved() || player.isSpectator()) {
            if (!level().isClientSide()) {
                discard();
            }
            return;
        }

        syncPosition();

        boolean deployed = !player.onGround() && !player.isInWater() && !player.isSleeping();

        if (!level().isClientSide()) {
            entityData.set(DATA_DEPLOYED, deployed);
        }

        // Glider physics run on both sides — player movement is client-authoritative
        // in MC, so the client must apply the velocity changes for smooth gliding.
        // The server runs the same logic for authoritative position validation.
        if (deployed && player.getDeltaMovement().y < lastMotionY) {
            double verticalSpeed;
            double horizontalSpeed;

            if (player.isShiftKeyDown()) {
                verticalSpeed = Math.max(VSPEED_FAST, VSPEED_MIN);
                horizontalSpeed = 0.1;
            } else {
                verticalSpeed = Math.max(VSPEED_NORMAL, VSPEED_MIN);
                horizontalSpeed = 0.03;
            }

            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, verticalSpeed, motion.z);
            player.fallDistance = 0;

            // Apply forward impulse based on player look direction
            float yaw = player.getYHeadRot();
            double dx = -Math.sin(Math.toRadians(yaw)) * horizontalSpeed;
            double dz = Math.cos(Math.toRadians(yaw)) * horizontalSpeed;

            player.setDeltaMovement(player.getDeltaMovement().add(dx, 0, dz));

            // Reset fall distance
            player.resetFallDistance();
        }

        lastMotionY = player.getDeltaMovement().y;
    }

    private void syncPosition() {
        if (player != null) {
            setPos(player.getX(), player.getY() + 1.2, player.getZ());
            setYRot(player.getYRot());
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false; // Never save to disk
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void remove(RemovalReason reason) {
        if (player != null) {
            GLIDER_MAP.remove(player);
        }
        super.remove(reason);
    }

    /**
     * Check if a glider is valid and should continue to exist.
     */
    public boolean isGliderValid() {
        if (player == null || player.isRemoved()) return false;
        if (player.isSpectator()) return false;
        // Check if player still has the hang glider item
        return true; // Simplified — item checks handled by HangGliderItem
    }
}
