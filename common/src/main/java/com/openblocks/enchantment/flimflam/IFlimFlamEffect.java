package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;

/**
 * A flim flam effect that can be applied to a player as a prank.
 */
@FunctionalInterface
public interface IFlimFlamEffect {
    /**
     * Execute this flim flam effect on the target player.
     * @param target the player to prank
     * @return true if the effect was successfully applied, false if it couldn't be applied
     */
    boolean execute(ServerPlayer target);
}
