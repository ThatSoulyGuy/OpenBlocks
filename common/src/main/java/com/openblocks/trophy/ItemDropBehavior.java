package com.openblocks.trophy;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Generic trophy behavior that drops an item when activated.
 */
public class ItemDropBehavior implements ITrophyBehavior {

    private final int cooldownTicks;
    private final ItemStack drop;
    private final SoundEvent sound;

    public ItemDropBehavior(int cooldownTicks, ItemStack drop) {
        this(cooldownTicks, drop, null);
    }

    public ItemDropBehavior(int cooldownTicks, ItemStack drop, SoundEvent sound) {
        this.cooldownTicks = cooldownTicks;
        this.drop = drop.copy();
        this.sound = sound;
    }

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        Level level = tile.getLevel();
        if (level != null && !level.isClientSide()) {
            if (sound != null) {
                level.playSound(null, player.blockPosition(), sound, SoundSource.NEUTRAL,
                        1.0f, (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1.0f);
            }
            ItemStack dropCopy = drop.copy();
            if (!player.getInventory().add(dropCopy)) {
                player.drop(dropCopy, false);
            }
        }
        return cooldownTicks;
    }
}
