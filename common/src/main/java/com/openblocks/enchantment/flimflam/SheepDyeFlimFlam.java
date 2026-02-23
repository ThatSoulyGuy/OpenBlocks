package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Dyes a random nearby sheep a random color.
 */
public class SheepDyeFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        AABB area = target.getBoundingBox().inflate(20);
        List<Sheep> sheep = target.level().getEntitiesOfClass(Sheep.class, area);

        if (sheep.isEmpty()) return false;

        Sheep chosen = sheep.get(RANDOM.nextInt(sheep.size()));
        DyeColor[] colors = DyeColor.values();
        chosen.setColor(colors[RANDOM.nextInt(colors.length)]);
        return true;
    }
}
