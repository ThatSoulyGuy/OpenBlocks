package com.openblocks.enchantment.flimflam;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Adds Bane of Arthropods V to a random unenchanted single-stack item in the player's inventory.
 */
public class BaneFlimFlam implements IFlimFlamEffect {

    private static final ResourceKey<Enchantment> BANE_OF_ARTHROPODS =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("bane_of_arthropods"));

    @Override
    public boolean execute(ServerPlayer target) {
        var registry = target.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var holderOpt = registry.getHolder(BANE_OF_ARTHROPODS);
        if (holderOpt.isEmpty()) return false;

        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getMaxStackSize() == 1 && !EnchantmentHelper.hasAnyEnchantments(stack)) {
                stack.enchant(holderOpt.get(), 5);
                return true;
            }
        }
        return false;
    }
}
