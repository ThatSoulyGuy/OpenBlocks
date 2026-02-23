package com.openblocks.enchantment;

import com.openblocks.enchantment.flimflam.FlimFlamRegistry;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Handles the Flim Flam enchantment: on PvP damage, accumulates "bad luck"
 * on the target. Periodically checks players and applies random prank effects.
 */
public final class FlimFlamHandler {

    public static final int LUCK_MARGIN = -30;
    public static final int EFFECT_DELAY = 20 * 15; // 15 seconds in ticks

    private static final Random RANDOM = new Random();

    // Per-player luck tracking (keyed by UUID, survives reconnects within session)
    private static final Map<UUID, LuckData> PLAYER_LUCK = new HashMap<>();

    private static class LuckData {
        int luck = 0;
        int cooldown = 0;
        boolean forceNext = false;
    }

    public static void register() {
        // On PvP damage: accumulate bad luck
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity instanceof Player targetPlayer && !entity.level().isClientSide()) {
                if (source != null) {
                    Entity sourceEntity = source.getEntity();
                    if (sourceEntity instanceof Player sourcePlayer && sourcePlayer != targetPlayer) {
                        handleFlimFlamDamage(sourcePlayer, targetPlayer);
                    }
                }
            }
            return EventResult.pass();
        });

        // Periodic tick: deliver karma
        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
                deliverKarma(serverPlayer);
            }
        });
    }

    private static void handleFlimFlamDamage(Player source, Player target) {
        int sourceFlimFlam = OpenBlocksEnchantments.getItemLevel(
                source, OpenBlocksEnchantments.FLIM_FLAM, source.getMainHandItem());
        int targetFlimFlam = OpenBlocksEnchantments.getArmorLevel(
                target, OpenBlocksEnchantments.FLIM_FLAM);

        // Armor is less effective since more slots contribute
        int diff = targetFlimFlam / 3 - sourceFlimFlam;
        if (diff == 0) return;

        Player victim;
        int flimFlamsToApply;
        if (diff > 0) {
            // Target is better protected, source gets flim-flammed
            victim = source;
            flimFlamsToApply = diff;
        } else {
            victim = target;
            flimFlamsToApply = -diff;
        }

        LuckData luck = getOrCreateLuck(victim.getUUID());
        for (int i = 0; i < flimFlamsToApply; i++) {
            int roll = RANDOM.nextInt(20) + 1;
            if (roll == 20) luck.forceNext = true;
            luck.luck -= roll;
        }
        if (luck.luck < LUCK_MARGIN) luck.forceNext = true;
    }

    private static void deliverKarma(ServerPlayer player) {
        if (player.isDeadOrDying()) return;

        LuckData luck = PLAYER_LUCK.get(player.getUUID());
        if (luck == null || !canFlimFlam(luck)) return;

        FlimFlamRegistry.executeRandomEffect(player, luck.luck);
    }

    private static boolean canFlimFlam(LuckData luck) {
        if (luck.forceNext) {
            luck.forceNext = false;
            luck.cooldown = EFFECT_DELAY;
            return true;
        }

        if (luck.luck > -LUCK_MARGIN || luck.cooldown-- > 0) return false;
        luck.cooldown = EFFECT_DELAY;
        double probability = 0.75 * 2.0 * Math.abs(Math.atan(luck.luck / 250.0) / Math.PI);
        return RANDOM.nextDouble() < probability;
    }

    public static int getLuck(Player player) {
        LuckData data = PLAYER_LUCK.get(player.getUUID());
        return data != null ? data.luck : 0;
    }

    public static int modifyLuck(Player player, int amount) {
        LuckData data = getOrCreateLuck(player.getUUID());
        data.luck += amount;
        return data.luck;
    }

    private static LuckData getOrCreateLuck(UUID uuid) {
        return PLAYER_LUCK.computeIfAbsent(uuid, k -> new LuckData());
    }

    private FlimFlamHandler() {}
}
