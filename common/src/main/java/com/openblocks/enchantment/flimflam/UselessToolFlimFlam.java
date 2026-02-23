package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Random;

/**
 * Drops a completely broken tool/armor with random enchantments near the player.
 */
public class UselessToolFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    private static final Item[] ITEMS = {
            Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE,
            Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL,
            Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE,
            Items.SHEARS,
            Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS,
            Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS,
            Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE,
            Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET
    };

    @Override
    public boolean execute(ServerPlayer target) {
        Item item = ITEMS[RANDOM.nextInt(ITEMS.length)];
        ItemStack stack = new ItemStack(item);

        // Apply random enchantments
        stack = EnchantmentHelper.enchantItem(
                RandomSource.create(), stack, 30, target.level().registryAccess(),
                java.util.Optional.empty());

        // Set damage to maximum (completely broken)
        if (stack.isDamageableItem()) {
            stack.setDamageValue(stack.getMaxDamage() - 1);
        }

        // Drop at player position
        target.drop(stack, true, false);
        return true;
    }
}
