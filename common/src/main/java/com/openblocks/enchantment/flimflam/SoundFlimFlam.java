package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Random;

/**
 * Plays a random scary/annoying sound at the player's position.
 */
public class SoundFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    private static final SoundEvent[] SOUNDS = {
            SoundEvents.TNT_PRIMED,
            SoundEvents.GENERIC_EXPLODE.value(),
            SoundEvents.ELDER_GUARDIAN_CURSE,
            SoundEvents.BLAZE_SHOOT,
            SoundEvents.ENDERMAN_STARE,
            SoundEvents.GHAST_SCREAM,
            SoundEvents.GHAST_SHOOT,
            SoundEvents.CREEPER_PRIMED,
            SoundEvents.WITHER_SPAWN,
            SoundEvents.LIGHTNING_BOLT_THUNDER
    };

    @Override
    public boolean execute(ServerPlayer target) {
        SoundEvent sound = SOUNDS[RANDOM.nextInt(SOUNDS.length)];
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                sound, SoundSource.MASTER, 1.0f, 1.0f);
        return true;
    }
}
