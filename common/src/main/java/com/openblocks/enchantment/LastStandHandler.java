package com.openblocks.enchantment;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles the Last Stand enchantment: prevents lethal damage by consuming XP.
 * The XP cost is based on how much overkill damage would occur.
 */
public final class LastStandHandler {

    public static void register() {
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player && !player.level().isClientSide()) {
                if (handleLastStand(player, amount)) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });
    }

    private static boolean handleLastStand(ServerPlayer player, float damage) {
        int totalLevel = OpenBlocksEnchantments.getArmorLevel(player, OpenBlocksEnchantments.LAST_STAND);
        if (totalLevel <= 0) return false;

        float health = player.getHealth();
        float remaining = health - damage;

        // Only trigger if damage would be lethal
        if (remaining >= 1.0f) return false;

        // Calculate XP cost: (damage - health + 1) * 50 / totalLevel
        float xpNeeded = (damage - health + 1.0f) * 50.0f / totalLevel;
        xpNeeded = Math.max(1, xpNeeded);

        int playerXp = getPlayerTotalXp(player);
        if (playerXp < (int) xpNeeded) return false;

        // Consume XP and save the player
        addPlayerXp(player, -(int) xpNeeded);
        player.setHealth(1.0f);
        return true;
    }

    /**
     * Get total XP points for a player (levels + progress).
     */
    private static int getPlayerTotalXp(ServerPlayer player) {
        int level = player.experienceLevel;
        int total = 0;

        // Sum XP for all completed levels
        for (int i = 0; i < level; i++) {
            total += xpForLevel(i);
        }

        // Add partial progress on current level
        total += (int) (player.experienceProgress * xpForLevel(level));
        return total;
    }

    /**
     * Add (or subtract) XP points from a player.
     */
    private static void addPlayerXp(ServerPlayer player, int amount) {
        player.giveExperiencePoints(amount);
    }

    /**
     * XP required to go from the given level to the next.
     */
    private static int xpForLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        if (level >= 15) return 37 + (level - 15) * 5;
        return 7 + level * 2;
    }

    private LastStandHandler() {}
}
