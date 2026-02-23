package com.openblocks.trophy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class SquidBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        Level level = tile.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos above = tile.getBlockPos().above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, Fluids.WATER.defaultFluidState().createLegacyBlock(), 3);
            }
        }
        return 10;
    }
}
