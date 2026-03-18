package com.openblocks.automation;

import com.openblocks.core.gui.OpenBlocksContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AutoAnvilScreen extends OpenBlocksContainerScreen<AutoAnvilMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/gui/auto_anvil.png");

    public AutoAnvilScreen(AutoAnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        super.renderBg(g, partialTick, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        // "+" between tool and modifier slots
        g.drawString(font, "+", x + 53, y + 39, 0x404040, false);
        // Arrow between modifier and output slots
        g.drawString(font, "\u2192", x + 106, y + 39, 0x404040, false);

        // XP tank gauge
        renderXpGauge(g, x + 152, y + 18, menu.getStoredXp(), menu.getMaxXp());
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);

        // XP label above gauge
        g.drawString(font, "XP", 154, 8, 0x404040, false);
    }

    static void renderXpGauge(GuiGraphics g, int x, int y, int stored, int max) {
        int w = 14;
        int h = 52;

        // Border
        g.fill(x, y, x + w, y + h, 0xFF373737);
        // Inner background
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1E1E1E);

        // Fill
        if (max > 0 && stored > 0) {
            int fillH = Math.min(h - 2, (int) ((h - 2) * (long) stored / max));
            if (fillH > 0) {
                g.fill(x + 1, y + h - 1 - fillH, x + w - 1, y + h - 1, 0xFF80FF00);
            }
        }
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
