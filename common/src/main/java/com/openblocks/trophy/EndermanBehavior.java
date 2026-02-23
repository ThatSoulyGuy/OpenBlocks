package com.openblocks.trophy;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;

public class EndermanBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        Level level = tile.getLevel();
        if (level != null && !level.isClientSide()) {
            ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(pearl);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_PEARL_THROW,
                    SoundSource.NEUTRAL, 0.5f, 0.4f / (level.random.nextFloat() * 0.4f + 0.8f));
        }
        return 0;
    }
}
