package com.openblocks.trophy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class WitchBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 1));
        player.displayClientMessage(Component.translatable("openblocks.misc.get_witched"), true);
        return 0;
    }
}
