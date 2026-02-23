package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;

import java.util.Random;

/**
 * Spawns 15 creepers around the player. They have Slowness X so they can't chase,
 * making them mostly harmless but very scary.
 */
public class DummyCreepersFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        ServerLevel level = target.serverLevel();

        for (int i = 0; i < 15; i++) {
            Creeper creeper = EntityType.CREEPER.create(level);
            if (creeper == null) return false;

            double x = target.getX() + (RANDOM.nextDouble() - 0.5) * 40;
            double z = target.getZ() + (RANDOM.nextDouble() - 0.5) * 40;
            double y = target.getY() + 5 + RANDOM.nextDouble() * 5;

            creeper.moveTo(x, y, z, RANDOM.nextFloat() * 360, 0);

            // Make them very slow so they can't actually reach the player
            creeper.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6000, 9, false, false));

            level.addFreshEntity(creeper);
        }
        return true;
    }
}
