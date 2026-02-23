package com.openblocks.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Essential block manipulation helpers.
 * Ported from OpenModsLib's openmods.utils.BlockUtils.
 */
public final class BlockUtils {

    /**
     * Drops an item stack at the given position with random velocity.
     */
    public static void dropItemStack(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty() && !level.isClientSide()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
    }

    /**
     * Gets a block entity at the given position, cast to the expected type.
     * Returns null if the block entity doesn't exist or is the wrong type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T getBlockEntity(Level level, BlockPos pos, Class<T> type) {
        BlockEntity be = level.getBlockEntity(pos);
        if (type.isInstance(be)) {
            return (T) be;
        }
        return null;
    }

    private BlockUtils() {}
}
