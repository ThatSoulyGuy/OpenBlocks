package com.openblocks.trophy;

import net.minecraft.world.entity.player.Player;

/**
 * Defines behavior when a trophy is activated (right-clicked) or ticked.
 */
public interface ITrophyBehavior {

    /**
     * Execute when the trophy is activated by a player.
     * @return cooldown ticks before the behavior can be activated again
     */
    int executeActivateBehavior(TrophyBlockEntity tile, Player player);

    /**
     * Execute every server tick while the trophy is placed.
     */
    default void executeTickBehavior(TrophyBlockEntity tile) {}
}
