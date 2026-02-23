package com.openblocks.trophy;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class GuardianBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
        return 0;
    }
}
