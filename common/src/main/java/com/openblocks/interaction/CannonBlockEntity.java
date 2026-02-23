package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.registry.OpenBlocksSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Cannon block entity. Stores pitch and speed parameters.
 * When fired, pulls an item from an adjacent inventory and launches it as a projectile.
 */
public class CannonBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Float> pitch = syncedFloat("pitch", 45.0f);
    private final SyncedValue<Float> speed = syncedFloat("speed", 1.0f);

    public CannonBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.CANNON.get(), pos, state);
    }

    public void serverTick() {
        // Cannon only fires on redstone pulse, no tick logic needed
    }

    public void fire() {
        if (level == null || level.isClientSide()) return;

        Direction facing = getBlockState().getValue(CannonBlock.FACING);

        // Try to find an item from adjacent inventory (below or behind the cannon)
        ItemStack projectile = findProjectile(facing);
        if (projectile.isEmpty()) return;

        // Calculate launch velocity
        double pitchRad = Math.toRadians(pitch.get());
        double yawRad = Math.toRadians(getYawFromFacing(facing));

        double vx = -Math.sin(yawRad) * Math.cos(pitchRad) * speed.get();
        double vy = Math.sin(pitchRad) * speed.get();
        double vz = Math.cos(yawRad) * Math.cos(pitchRad) * speed.get();

        // Spawn item entity as projectile
        Vec3 spawnPos = Vec3.atCenterOf(worldPosition).add(
                facing.getStepX() * 0.8,
                0.5,
                facing.getStepZ() * 0.8
        );

        ItemStack singleItem = projectile.split(1);
        ItemEntity itemEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, singleItem);
        itemEntity.setDeltaMovement(vx, vy, vz);
        itemEntity.setPickUpDelay(20); // Can't pick up immediately
        level.addFreshEntity(itemEntity);

        level.playSound(null, worldPosition, OpenBlocksSounds.CANNON_FIRE.get(),
                SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private ItemStack findProjectile(Direction cannonFacing) {
        // Check the block behind the cannon for an inventory
        BlockPos behindPos = worldPosition.relative(cannonFacing.getOpposite());
        BlockEntity behind = level.getBlockEntity(behindPos);

        if (behind instanceof net.minecraft.world.Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }

        // Also check below
        BlockPos belowPos = worldPosition.below();
        BlockEntity below = level.getBlockEntity(belowPos);
        if (below instanceof net.minecraft.world.Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static float getYawFromFacing(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
    }

    public float getPitch() { return pitch.get(); }
    public void setPitch(float value) { pitch.set(value); sync(); }
    public float getSpeed() { return speed.get(); }
    public void setSpeed(float value) { speed.set(value); sync(); }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Pitch: " + pitch.get());
        info.add("Speed: " + speed.get());
        info.add("Facing: " + getBlockState().getValue(CannonBlock.FACING));
        return info;
    }
}
