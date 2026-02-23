package com.openblocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * A flying drone entity that maps the surrounding area, filling in map items.
 * Simplified implementation — hovers above the owner and fills in a map over time.
 */
public class CartographerEntity extends Entity {

    private static final EntityDataAccessor<Boolean> DATA_MAPPING =
            SynchedEntityData.defineId(CartographerEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUUID;
    private ItemStack mapItem = ItemStack.EMPTY;
    private int mappingTick;
    private float wanderAngle;
    private int wanderTimer;
    private final float angleOffset;

    public CartographerEntity(EntityType<? extends CartographerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        // Unique per-entity offset so multiple cartographers spread around the player
        this.angleOffset = random.nextFloat() * 2 * (float) Math.PI;
    }

    public void setOwner(Player owner) {
        this.ownerUUID = owner.getUUID();
        setPos(owner.getX() + 1, owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_MAPPING, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;

        Player owner = ownerUUID != null ? level().getPlayerByUUID(ownerUUID) : null;
        if (owner == null || owner.isRemoved()) {
            dropMapAndDiscard();
            return;
        }

        // Calculate target offset direction (matching 1.12.2 EntityCartographer)
        float yaw;
        if (isMapping()) {
            // Random wander when mapping — new random angle every 35 ticks
            wanderTimer--;
            if (wanderTimer <= 0) {
                wanderTimer = 35;
                wanderAngle = (float) (random.nextFloat() * 2 * Math.PI);
            }
            yaw = wanderAngle;
        } else {
            // Follow owner's look direction when idle, offset by per-entity angle
            yaw = (float) Math.toRadians(owner.getYRot()) + angleOffset;
        }

        // Target: 1 block offset at owner's eye level (matching 1.12.2 EntityAssistant)
        double targetX = owner.getX() + Math.sin(-yaw);
        double targetY = owner.getEyeY();
        double targetZ = owner.getZ() + Math.cos(-yaw);

        // Smooth damped movement (matching 1.12.2 MoveSmoother: damp=0.5, cutoff=5.0, panic=128)
        Vec3 current = position();
        double dx = targetX - current.x;
        double dy = targetY - current.y;
        double dz = targetZ - current.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > 128.0) {
            // Panic teleport
            setPos(targetX, targetY, targetZ);
        } else if (dist > 0.01) {
            // Clamp delta to cutoff, then apply 50% damping
            if (dist > 5.0) {
                double scale = 5.0 / dist;
                dx *= scale;
                dy *= scale;
                dz *= scale;
            }
            setPos(current.x + dx * 0.5, current.y + dy * 0.5, current.z + dz * 0.5);
        }

        // Mapping logic
        if (isMapping() && !mapItem.isEmpty()) {
            mappingTick++;
            if (mappingTick % 5 == 0) {
                // Update map data by visiting the cartographer's position
                MapId mapId = mapItem.get(net.minecraft.core.component.DataComponents.MAP_ID);
                if (mapId != null) {
                    MapItemSavedData mapData = level().getMapData(mapId);
                    if (mapData != null) {
                        ((MapItem) Items.FILLED_MAP).update(level(), owner, mapData);
                    }
                }
            }
        }
    }

    public boolean isMapping() {
        return entityData.get(DATA_MAPPING);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;

        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        // Sneak + interact: exchange maps or retrieve
        ItemStack heldItem = player.getItemInHand(hand);

        if (!mapItem.isEmpty() && heldItem.isEmpty()) {
            // Give map back
            player.setItemInHand(hand, mapItem.copy());
            mapItem = ItemStack.EMPTY;
            entityData.set(DATA_MAPPING, false);
            return InteractionResult.SUCCESS;
        }

        if (mapItem.isEmpty() && heldItem.is(Items.FILLED_MAP)) {
            // Take map from player
            mapItem = heldItem.split(1);
            entityData.set(DATA_MAPPING, true);
            mappingTick = 0;
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) return false;
        dropMapAndDiscard();
        // Drop as item
        ItemStack itemStack = new ItemStack(com.openblocks.core.registry.OpenBlocksItems.CARTOGRAPHER.get());
        ItemEntity drop = new ItemEntity(level(), getX(), getY(), getZ(), itemStack);
        level().addFreshEntity(drop);
        return true;
    }

    private void dropMapAndDiscard() {
        if (!mapItem.isEmpty() && !level().isClientSide()) {
            ItemEntity drop = new ItemEntity(level(), getX(), getY(), getZ(), mapItem);
            level().addFreshEntity(drop);
            mapItem = ItemStack.EMPTY;
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("MapItem")) {
            mapItem = ItemStack.parseOptional(level().registryAccess(), tag.getCompound("MapItem"));
        }
        entityData.set(DATA_MAPPING, tag.getBoolean("Mapping"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (!mapItem.isEmpty()) {
            tag.put("MapItem", mapItem.save(level().registryAccess()));
        }
        tag.putBoolean("Mapping", isMapping());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096;
    }

    // --- Client-side eye animation (matching 1.12.2 EntityCartographer) ---

    public float eyeYaw, eyePitch;
    private float targetYaw, targetPitch;

    /**
     * Called from the renderer every frame to animate the eye of ender.
     * Slowly chases random target angles, picking new ones when close enough.
     * Matches 1.12.2 EntityCartographer.updateEye() exactly.
     */
    public void updateEye() {
        float diffYaw = (targetYaw - eyeYaw) % (float) Math.PI;
        float diffPitch = (targetPitch - eyePitch) % (float) Math.PI;

        if (Math.abs(diffYaw) + Math.abs(diffPitch) < 0.0001f) {
            targetPitch = random.nextFloat() * 2 * (float) Math.PI;
            targetYaw = random.nextFloat() * 2 * (float) Math.PI;
        } else {
            eyeYaw -= diffYaw / 50.0f;
            eyePitch -= diffPitch / 50.0f;
        }
    }

    /** Chunk-aligned X coordinate for map center display. */
    public int getNewMapCenterX() {
        return ((int) getX()) & ~0x0F;
    }

    /** Chunk-aligned Z coordinate for map center display. */
    public int getNewMapCenterZ() {
        return ((int) getZ()) & ~0x0F;
    }
}
