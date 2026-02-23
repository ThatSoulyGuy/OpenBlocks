package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Fan block entity. Pushes entities in its facing direction with force
 * proportional to redstone signal and inversely proportional to distance.
 */
public class FanBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Integer> redstonePower = syncedInt("power", 0);

    public FanBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.FAN.get(), pos, state);
    }

    public void updateRedstone(int power) {
        if (redstonePower.get() != power) {
            redstonePower.set(power);
            sync();
        }
    }

    public void serverTick() {
        int power = redstonePower.get();
        if (power <= 0 || level == null) return;

        Direction facing = getBlockState().getValue(FanBlock.FACING);
        double maxForce = OpenBlocksConfig.Fan.force;
        double maxRange = OpenBlocksConfig.Fan.range;

        // Scale force by redstone signal (0-15)
        double scaledForce = maxForce * (power / 15.0);
        double scaledRange = maxRange * (power / 15.0);

        // Build cone AABB in front of the fan
        BlockPos pos = worldPosition;
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal());

        double x1 = pos.getX() + 0.5 + Math.min(0, dir.x * scaledRange);
        double y1 = pos.getY() + 0.5 + Math.min(0, dir.y * scaledRange);
        double z1 = pos.getZ() + 0.5 + Math.min(0, dir.z * scaledRange);
        double x2 = pos.getX() + 0.5 + Math.max(0, dir.x * scaledRange);
        double y2 = pos.getY() + 0.5 + Math.max(0, dir.y * scaledRange);
        double z2 = pos.getZ() + 0.5 + Math.max(0, dir.z * scaledRange);

        // Expand perpendicular axes for a cone-like area
        double perpExpand = scaledRange * 0.5;
        if (facing.getAxis() != Direction.Axis.X) { x1 -= perpExpand; x2 += perpExpand; }
        if (facing.getAxis() != Direction.Axis.Y) { y1 -= perpExpand; y2 += perpExpand; }
        if (facing.getAxis() != Direction.Axis.Z) { z1 -= perpExpand; z2 += perpExpand; }

        AABB area = new AABB(x1, y1, z1, x2, y2, z2);
        List<Entity> entities = level.getEntities(null, area);

        for (Entity entity : entities) {
            double distance = entity.position().distanceTo(Vec3.atCenterOf(pos));
            if (distance < 0.5) distance = 0.5;
            if (distance > scaledRange) continue;

            // Force decays linearly with distance
            double factor = scaledForce * (1.0 - distance / scaledRange);
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    dir.x * factor,
                    dir.y * factor,
                    dir.z * factor
            ));
            entity.hurtMarked = true;
        }
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Redstone: " + redstonePower.get());
        info.add("Facing: " + getBlockState().getValue(FanBlock.FACING));
        return info;
    }
}
