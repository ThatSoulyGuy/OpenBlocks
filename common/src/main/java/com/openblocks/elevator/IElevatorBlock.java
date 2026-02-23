package com.openblocks.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Interface for blocks that participate in the elevator system.
 * Implement on any block that should be recognized as an elevator destination.
 */
public interface IElevatorBlock {

    /**
     * Returns the elevator color for color-matching purposes.
     * Elevators only teleport to matching colors.
     */
    DyeColor getElevatorColor(Level level, BlockPos pos, BlockState state);

    /**
     * Returns the player rotation to apply after teleporting.
     * {@link PlayerRotation#NONE} means the player keeps their current facing.
     */
    default PlayerRotation getElevatorRotation(Level level, BlockPos pos, BlockState state) {
        return PlayerRotation.NONE;
    }
}
