package com.openblocks.enchantment;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Handles the Explosive enchantment: when a player with the enchantment on boots
 * takes damage from certain sources while wearing armor with this enchantment,
 * an explosion is created. Consumes gunpowder from inventory.
 */
public final class ExplosiveHandler {

    private static final float[] EXPLOSION_POWER = {1.0f, 2.0f, 4.0f};
    private static final int[] GUNPOWDER_COST = {1, 2, 4};

    public static void register() {
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player && !player.level().isClientSide()) {
                handleExplosiveDamage(player, amount);
            }
            return EventResult.pass();
        });
    }

    private static void handleExplosiveDamage(ServerPlayer player, float amount) {
        if (amount <= 0) return;

        // Check for explosive enchantment on any armor piece
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty()) continue;

            int level = OpenBlocksEnchantments.getItemLevel(player, OpenBlocksEnchantments.EXPLOSIVE, armor);
            if (level <= 0) continue;

            int idx = Math.min(level, EXPLOSION_POWER.length) - 1;
            int gunpowderNeeded = GUNPOWDER_COST[idx];

            // Find and consume gunpowder
            int gunpowderSlot = findGunpowder(player, gunpowderNeeded);
            if (gunpowderSlot < 0) continue;

            player.getInventory().getItem(gunpowderSlot).shrink(gunpowderNeeded);

            // Damage the armor piece
            if (!player.isCreative()) {
                armor.hurtAndBreak(1, player, slot);
            }

            // Create explosion
            float power = EXPLOSION_POWER[idx];
            Level level1 = player.level();
            level1.explode(player, player.getX(), player.getY(), player.getZ(),
                    power, Level.ExplosionInteraction.NONE);
            return; // Only trigger once per damage event
        }
    }

    private static int findGunpowder(Player player, int needed) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.GUNPOWDER) && stack.getCount() >= needed) {
                return i;
            }
        }
        return -1;
    }

    private ExplosiveHandler() {}
}
