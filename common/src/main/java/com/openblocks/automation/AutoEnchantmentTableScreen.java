package com.openblocks.automation;

import com.openblocks.core.gui.OpenBlocksContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AutoEnchantmentTableScreen extends OpenBlocksContainerScreen<AutoEnchantmentTableMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/gui/auto_enchantment_table.png");

    public AutoEnchantmentTableScreen(AutoEnchantmentTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        super.renderBg(g, partialTick, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        // Arrow between lapis and output
        g.drawString(font, "\u2192", x + 106, y + 39, 0x404040, false);

        // XP tank gauge
        AutoAnvilScreen.renderXpGauge(g, x + 152, y + 18, menu.getStoredXp(), menu.getMaxXp());

        // Bookshelf power and enchant level info
        int power = menu.getBookshelfPower();
        int enchantLevel = Math.min(30, 1 + power * 2);
        g.drawString(font, Component.translatable("openblocks.gui.power", power), x + 50, y + 56, 0x404040, false);
        g.drawString(font, Component.translatable("openblocks.gui.enchant_level", enchantLevel), x + 50, y + 68, 0x404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);

        // XP label above gauge
        g.drawString(font, "XP", 154, 8, 0x404040, false);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
