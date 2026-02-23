package com.openblocks.trophy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CreeperBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        Level level = tile.getLevel();
        if (level != null) {
            BlockPos pos = tile.getBlockPos();
            level.explode(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    2.0f, Level.ExplosionInteraction.NONE);
        }
        return 0;
    }
}
