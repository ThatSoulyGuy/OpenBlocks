package com.openblocks.enchantment.flimflam;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * Surrounds the player in a 3x3x4 dirt cage with a torch inside.
 */
public class EncaseFlimFlam implements IFlimFlamEffect {

    @Override
    public boolean execute(ServerPlayer target) {
        ServerLevel level = target.serverLevel();
        BlockPos center = target.blockPosition();

        // Build a hollow dirt cage around the player
        for (int y = 0; y < 4; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    // Only place walls (hollow inside)
                    if (x == 0 && z == 0 && y < 3) continue; // Leave center column open (except roof)
                    if (Math.abs(x) < 1 && Math.abs(z) < 1 && y < 3) continue;

                    BlockPos pos = center.offset(x, y, z);
                    if (level.getBlockState(pos).isAir() || level.getBlockState(pos).canBeReplaced()) {
                        level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                    }
                }
            }
        }

        // Place a torch inside
        BlockPos torchPos = center.above();
        if (level.getBlockState(torchPos).isAir()) {
            level.setBlockAndUpdate(torchPos, Blocks.TORCH.defaultBlockState());
        }

        return true;
    }
}
