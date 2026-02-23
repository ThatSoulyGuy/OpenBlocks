package com.openblocks.trophy;

import net.minecraft.world.entity.player.Player;

public class BlazeBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        player.igniteForSeconds(4);
        return 0;
    }
}
