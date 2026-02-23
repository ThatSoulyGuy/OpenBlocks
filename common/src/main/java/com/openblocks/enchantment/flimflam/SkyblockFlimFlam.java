package com.openblocks.enchantment.flimflam;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * Teleports the player high up and places them on a small ice platform.
 */
public class SkyblockFlimFlam implements IFlimFlamEffect {

    @Override
    public boolean execute(ServerPlayer target) {
        ServerLevel level = target.serverLevel();

        // Don't work in the nether (water evaporates)
        if (level.dimensionType().ultraWarm()) return false;

        double targetY = Math.min(target.getY() + 150, 250);
        BlockPos center = BlockPos.containing(target.getX(), targetY, target.getZ());

        // Check if positions are clear
        if (!level.getBlockState(center).isAir()) return false;

        // Create small ice platform: center + 4 cardinal directions
        BlockPos[] positions = {
                center.below(),
                center.below().east(),
                center.below().north(),
                center.below().south(),
                center.below().west()
        };

        for (BlockPos pos : positions) {
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        }

        // Teleport player
        target.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        target.fallDistance = 0;
        return true;
    }
}
