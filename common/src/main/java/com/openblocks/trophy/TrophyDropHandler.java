package com.openblocks.trophy;

import com.openblocks.core.config.OpenBlocksConfig;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Handles trophy drops when mobs are killed by players.
 */
public final class TrophyDropHandler {

    public static void register() {
        EntityEvent.LIVING_DEATH.register(TrophyDropHandler::onLivingDeath);
    }

    private static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {
        Level level = entity.level();
        if (level.isClientSide()) return EventResult.pass();

        // Only drop if killed by a player
        Entity killer = source.getEntity();
        if (!(killer instanceof Player)) return EventResult.pass();

        // Check if mob loot is enabled
        if (!level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
            return EventResult.pass();
        }

        // Check if this entity type has a trophy
        TrophyType trophyType = TrophyType.fromEntityType(entity.getType());
        if (trophyType == null) return EventResult.pass();

        // Roll for drop
        double dropChance = OpenBlocksConfig.Trophy.dropChance;
        if (dropChance <= 0) return EventResult.pass();

        if (level.random.nextDouble() < dropChance) {
            ItemStack trophyStack = TrophyBlockItem.createTrophyItem(trophyType);
            ItemEntity drop = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), trophyStack);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }

        return EventResult.pass();
    }

    private TrophyDropHandler() {}
}
