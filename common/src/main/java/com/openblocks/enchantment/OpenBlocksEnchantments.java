package com.openblocks.enchantment;

import com.openblocks.OpenBlocksConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Optional;

/**
 * ResourceKey references for OpenBlocks enchantments.
 * Enchantments are data-driven in 1.21.1 — defined via JSON, not Java.
 */
public final class OpenBlocksEnchantments {

    public static final ResourceKey<Enchantment> EXPLOSIVE =
            ResourceKey.create(Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "explosive"));

    public static final ResourceKey<Enchantment> LAST_STAND =
            ResourceKey.create(Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "last_stand"));

    public static final ResourceKey<Enchantment> FLIM_FLAM =
            ResourceKey.create(Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "flim_flam"));

    /**
     * Get the total enchantment level across all armor pieces for a living entity.
     */
    public static int getArmorLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        Registry<Enchantment> registry = entity.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(key);
        if (holderOpt.isEmpty()) return 0;

        Holder<Enchantment> holder = holderOpt.get();
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                total += EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getItemBySlot(slot));
            }
        }
        return total;
    }

    /**
     * Get the enchantment level on a specific item stack.
     */
    public static int getItemLevel(LivingEntity entity, ResourceKey<Enchantment> key, ItemStack stack) {
        Registry<Enchantment> registry = entity.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(key);
        if (holderOpt.isEmpty()) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(holderOpt.get(), stack);
    }

    private OpenBlocksEnchantments() {}
}
