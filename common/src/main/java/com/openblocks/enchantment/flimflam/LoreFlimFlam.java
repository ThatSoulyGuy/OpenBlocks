package com.openblocks.enchantment.flimflam;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Adds silly lore text to a random equipped item.
 */
public class LoreFlimFlam implements IFlimFlamEffect {

    private static final Random RANDOM = new Random();

    private static final String[] ADJECTIVES = {
            "Mighty", "Enchanted", "Legendary", "Cursed", "Ancient", "Mystical",
            "Forgotten", "Heroic", "Terrible", "Wonderful", "Suspicious", "Fabulous"
    };

    private static final String[] NOUNS = {
            "Destiny", "Doom", "Wonder", "Mystery", "Power", "Chaos",
            "Confusion", "Enlightenment", "Silliness", "Nonsense", "Pudding"
    };

    private static final String[] LORE_TEMPLATES = {
            "Forged in the fires of %s",
            "Once wielded by the great %s",
            "Smells faintly of %s",
            "Contains the essence of %s",
            "Whispers of %s in the moonlight",
            "Guaranteed to cause %s",
            "Now with 50%% more %s",
            "Warning: may contain traces of %s"
    };

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    @Override
    public boolean execute(ServerPlayer target) {
        // Try random equipment slots
        List<EquipmentSlot> slots = new ArrayList<>(List.of(SLOTS));
        java.util.Collections.shuffle(slots);

        for (EquipmentSlot slot : slots) {
            ItemStack stack = target.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                addSillyLore(stack);
                return true;
            }
        }
        return false;
    }

    private void addSillyLore(ItemStack stack) {
        String adj = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        String template = LORE_TEMPLATES[RANDOM.nextInt(LORE_TEMPLATES.length)];

        String loreText = String.format(template, adj + " " + noun);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(loreText).withStyle(style ->
                style.withColor(net.minecraft.ChatFormatting.GREEN).withItalic(true)));

        stack.set(net.minecraft.core.component.DataComponents.LORE, new ItemLore(lines));
    }
}
