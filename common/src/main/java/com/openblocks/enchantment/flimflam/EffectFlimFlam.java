package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Applies 2 random potion effects to the target player.
 */
public class EffectFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    @Override
    public boolean execute(ServerPlayer target) {
        List<MobEffectInstance> effects = new ArrayList<>();

        // Negative effects (15-60 seconds)
        effects.add(new MobEffectInstance(MobEffects.BLINDNESS, (15 + RANDOM.nextInt(45)) * 20, 0));
        effects.add(new MobEffectInstance(MobEffects.CONFUSION, (15 + RANDOM.nextInt(45)) * 20, 0));
        effects.add(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, (15 + RANDOM.nextInt(45)) * 20, RANDOM.nextInt(3)));

        // Moderate effects (5-15 seconds)
        effects.add(new MobEffectInstance(MobEffects.JUMP, (5 + RANDOM.nextInt(10)) * 20, 2 + RANDOM.nextInt(3)));
        effects.add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (5 + RANDOM.nextInt(10)) * 20, 2 + RANDOM.nextInt(3)));
        effects.add(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (5 + RANDOM.nextInt(10)) * 20, RANDOM.nextInt(2)));

        // Special
        effects.add(new MobEffectInstance(MobEffects.LEVITATION, (5 + RANDOM.nextInt(10)) * 20, 0));

        Collections.shuffle(effects);

        // Apply first 2
        for (int i = 0; i < Math.min(2, effects.size()); i++) {
            target.addEffect(effects.get(i));
        }
        return true;
    }
}
