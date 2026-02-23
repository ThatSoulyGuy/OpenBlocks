package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Makes nearby mobs invisible with a 30% chance per mob.
 */
public class InvisibleMobsFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        AABB area = target.getBoundingBox().inflate(20);
        List<LivingEntity> entities = target.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> !(e instanceof Player));

        if (entities.isEmpty()) return false;

        boolean applied = false;
        for (LivingEntity entity : entities) {
            if (RANDOM.nextFloat() < 0.3f) {
                entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 12000, 0, false, false));
                applied = true;
            }
        }
        return applied;
    }
}
