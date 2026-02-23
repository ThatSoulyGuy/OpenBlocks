package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;

import java.util.Random;

/**
 * Pelts the player with 200 snowballs from above.
 */
public class SnowballsFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        ServerLevel level = target.serverLevel();

        for (int i = 0; i < 200; i++) {
            Snowball snowball = EntityType.SNOWBALL.create(level);
            if (snowball == null) return false;

            snowball.setPos(target.getX(), target.getY() + 4, target.getZ());
            snowball.setDeltaMovement(
                    RANDOM.nextGaussian() * 0.05,
                    1.0,
                    RANDOM.nextGaussian() * 0.05
            );
            level.addFreshEntity(snowball);
        }
        return true;
    }
}
