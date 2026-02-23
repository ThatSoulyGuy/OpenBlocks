package com.openblocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A throwable entity similar to Eye of Ender that points toward a target position.
 * Spawned by the Golden Eye item, floats toward the target and then drops.
 */
public class GoldenEyeEntity extends Entity {

    private static final int TTL = 60;

    private int timeToLive = TTL;
    private ItemStack spawningStack = ItemStack.EMPTY;
    private BlockPos target = BlockPos.ZERO;
    private Vec3 targetPos = Vec3.ZERO;

    public GoldenEyeEntity(EntityType<? extends GoldenEyeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setup(ItemStack stack, Entity owner, BlockPos target) {
        this.spawningStack = stack.copy();
        this.target = target;

        // Position offset from owner's eye
        double offsetX = -Math.sin(Math.toRadians(owner.getYRot())) * 0.75;
        double offsetZ = Math.cos(Math.toRadians(owner.getYRot())) * 0.75;
        this.setPos(owner.getX() + offsetX, owner.getEyeY(), owner.getZ() + offsetZ);

        // Calculate target position (6 blocks toward target)
        double dx = target.getX() + 0.5 - getX();
        double dz = target.getZ() + 0.5 - getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 16) {
            // Close - point down
            targetPos = new Vec3(getX(), getY() - 2, getZ());
        } else {
            // Project 6 blocks toward target
            double factor = 6.0 / dist;
            targetPos = new Vec3(getX() + dx * factor, getY() + 2, getZ() + dz * factor);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        // Move toward target position
        Vec3 currentPos = position();
        Vec3 diff = targetPos.subtract(currentPos);
        double dist = diff.length();

        if (dist > 0.1) {
            Vec3 move = diff.normalize().scale(Math.min(0.5, dist));
            setDeltaMovement(move);
            setPos(getX() + move.x, getY() + move.y, getZ() + move.z);
        }

        if (!level().isClientSide()) {
            timeToLive--;
            if (timeToLive < 0) {
                if (!spawningStack.isEmpty()) {
                    ItemEntity item = new ItemEntity(level(), getX(), getY(), getZ(), spawningStack);
                    level().addFreshEntity(item);
                }
                discard();
            }
        } else {
            // Client particles
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.PORTAL,
                        getX() + random.nextGaussian() * 0.2,
                        getY() + random.nextGaussian() * 0.2,
                        getZ() + random.nextGaussian() * 0.2,
                        random.nextGaussian() * 0.05,
                        -0.1,
                        random.nextGaussian() * 0.05);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("SpawningItem")) {
            spawningStack = ItemStack.parseOptional(level().registryAccess(), tag.getCompound("SpawningItem"));
        }
        timeToLive = tag.getInt("TimeToLive");
        target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        targetPos = new Vec3(target.getX() + 0.5, getY() + 2, target.getZ() + 0.5);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!spawningStack.isEmpty()) {
            tag.put("SpawningItem", spawningStack.save(level().registryAccess()));
        }
        tag.putInt("TimeToLive", timeToLive);
        tag.putInt("TargetX", target.getX());
        tag.putInt("TargetY", target.getY());
        tag.putInt("TargetZ", target.getZ());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
