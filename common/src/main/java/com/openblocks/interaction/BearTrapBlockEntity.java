package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Bear trap block entity. Holds a reference to the captured entity
 * and prevents it from moving while trapped.
 */
public class BearTrapBlockEntity extends OpenBlocksBlockEntity {

    private WeakReference<LivingEntity> capturedEntity = new WeakReference<>(null);

    public BearTrapBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.BEAR_TRAP.get(), pos, state);
    }

    public void capture(LivingEntity entity) {
        capturedEntity = new WeakReference<>(entity);
    }

    public void release() {
        capturedEntity = new WeakReference<>(null);
    }

    public void serverTick() {
        if (level == null) return;

        LivingEntity entity = capturedEntity.get();
        if (entity == null || !entity.isAlive()) {
            // Entity died or despawned — re-open the trap
            if (getBlockState().getValue(BearTrapBlock.CLOSED)) {
                level.setBlock(worldPosition, getBlockState().setValue(BearTrapBlock.CLOSED, false), 3);
                capturedEntity = new WeakReference<>(null);
            }
            return;
        }

        // Keep entity pinned to trap center
        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY();
        double cz = worldPosition.getZ() + 0.5;

        entity.setPos(cx, cy, cz);
        entity.setDeltaMovement(0, 0, 0);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        LivingEntity entity = capturedEntity.get();
        info.add("Captured: " + (entity != null ? entity.getType().getDescription().getString() : "none"));
        info.add("Closed: " + getBlockState().getValue(BearTrapBlock.CLOSED));
        return info;
    }
}
