package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Forces the player to mount a random nearby mob.
 */
public class MountFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        AABB area = target.getBoundingBox().inflate(40);
        List<LivingEntity> entities = target.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> !(e instanceof Player) && !(e instanceof Creeper)
                        && e.getType() != EntityType.SQUID && e.isAlive());

        if (entities.isEmpty()) return false;

        LivingEntity mount = entities.get(RANDOM.nextInt(entities.size()));
        target.startRiding(mount, true);
        return true;
    }
}
