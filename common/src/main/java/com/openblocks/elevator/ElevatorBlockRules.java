package com.openblocks.elevator;

import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Determines how the elevator column search treats each block it encounters.
 */
public final class ElevatorBlockRules {

    public enum Action {
        /** Block is treated as empty — search continues without penalty. */
        IGNORE,
        /** Block counts toward the pass-through limit. */
        INCREMENT,
        /** Block completely stops the elevator search. */
        ABORT
    }

    /**
     * Evaluates the action for a block encountered during elevator column search.
     */
    public static Action getAction(Level level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return Action.IGNORE;
        }

        // Elevator blocks themselves are skipped (they are destinations, not obstacles)
        if (state.getBlock() instanceof IElevatorBlock) {
            return Action.IGNORE;
        }

        // If configured to ignore all blocks, everything is passable
        if (OpenBlocksConfig.Elevator.ignoreAllBlocks) {
            return Action.IGNORE;
        }

        // Check if block has no collision (fluids, tall grass, etc.)
        VoxelShape collisionShape = state.getCollisionShape(level, pos, CollisionContext.empty());
        if (collisionShape.isEmpty()) {
            return Action.IGNORE;
        }

        // Check for half slabs if configured to ignore them
        if (OpenBlocksConfig.Elevator.ignoreHalfBlocks) {
            if (isHalfBlock(collisionShape)) {
                return Action.IGNORE;
            }
        }

        // Check for irregular (non-full-cube) collision boxes
        if (OpenBlocksConfig.Elevator.irregularBlocksArePassable) {
            if (!isFullBlock(collisionShape)) {
                return Action.IGNORE;
            }
        }

        // Solid full block — counts toward pass-through limit
        return Action.INCREMENT;
    }

    private static boolean isHalfBlock(VoxelShape shape) {
        if (shape.isEmpty()) return false;
        double height = shape.max(net.minecraft.core.Direction.Axis.Y) - shape.min(net.minecraft.core.Direction.Axis.Y);
        return height > 0.0 && height <= 0.5001;
    }

    private static boolean isFullBlock(VoxelShape shape) {
        return Shapes.joinIsNotEmpty(Shapes.block(), shape, net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME)
                ? false : true;
    }

    private ElevatorBlockRules() {}
}
