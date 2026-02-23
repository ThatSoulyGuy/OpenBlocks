package com.openblocks.enchantment.flimflam;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Gives a random nearby mob a silly custom name.
 */
public class RenameFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    private static final String[] FIRST_NAMES = {
            "Sir", "Lord", "Captain", "Professor", "Dr.", "King", "Queen",
            "Duke", "Baron", "General", "Admiral", "Saint", "Master"
    };

    private static final String[] LAST_NAMES = {
            "Fluffybottom", "McSquidface", "Thunderpants", "Noodlearms",
            "Wobblechops", "Bumblebee", "Snickerdoodle", "Wigglesnort",
            "Dingleberry", "Fizzlepop", "Bumblesnatch", "Wafflecrunch"
    };

    @Override
    public boolean execute(ServerPlayer target) {
        AABB area = target.getBoundingBox().inflate(20);
        List<LivingEntity> entities = target.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> !(e instanceof Player) && !e.hasCustomName());

        if (entities.isEmpty()) return false;

        LivingEntity mob = entities.get(RANDOM.nextInt(entities.size()));
        String name = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)] + " "
                + LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
        mob.setCustomName(Component.literal(name));
        mob.setCustomNameVisible(true);
        return true;
    }
}
