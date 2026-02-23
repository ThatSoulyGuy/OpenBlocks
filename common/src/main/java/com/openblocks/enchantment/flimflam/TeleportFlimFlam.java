package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

import java.util.Random;

/**
 * Throws an ender pearl from the player's position with random velocity,
 * teleporting them to wherever it lands.
 */
public class TeleportFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        ServerLevel level = target.serverLevel();
        ThrownEnderpearl pearl = EntityType.ENDER_PEARL.create(level);
        if (pearl == null) return false;

        pearl.setOwner(target);
        pearl.setPos(target.getX(), target.getY() + 1, target.getZ());
        pearl.setDeltaMovement(
                RANDOM.nextGaussian(),
                0.5,
                RANDOM.nextGaussian()
        );
        level.addFreshEntity(pearl);
        return true;
    }
}
