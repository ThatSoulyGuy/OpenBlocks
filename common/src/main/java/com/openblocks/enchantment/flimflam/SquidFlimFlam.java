package com.openblocks.enchantment.flimflam;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Squid;

import java.util.Random;

/**
 * Spawns a squid with a funny name at the player's location.
 */
public class SquidFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    private static final String[] NAMES = {
            "Fancy Hat",
            "Hello there!",
            "Look at my hat!",
            "My hat is amazing!",
            "Ceci n'est pas une pipe",
            "???"
    };

    @Override
    public boolean execute(ServerPlayer target) {
        if (target.isPassenger()) return false;

        ServerLevel level = target.serverLevel();
        Squid squid = EntityType.SQUID.create(level);
        if (squid == null) return false;

        squid.moveTo(target.getX(), target.getY(), target.getZ(), 0, 0);
        squid.setCustomName(Component.literal(NAMES[RANDOM.nextInt(NAMES.length)]));
        squid.setCustomNameVisible(true);
        level.addFreshEntity(squid);
        return true;
    }
}
