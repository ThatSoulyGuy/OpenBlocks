package com.openblocks.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base container screen replacing OpenModsLib's SyncedGuiContainer.
 * Provides common rendering patterns for container-based GUIs.
 */
public abstract class OpenBlocksContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    protected OpenBlocksContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = getBackgroundTexture();
        if (texture != null) {
            guiGraphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Override to provide the background texture for this screen.
     * Return null for no background.
     */
    protected abstract ResourceLocation getBackgroundTexture();
}
