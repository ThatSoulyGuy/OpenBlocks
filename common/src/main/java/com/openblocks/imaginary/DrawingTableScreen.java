package com.openblocks.imaginary;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.core.gui.OpenBlocksContainerScreen;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI screen for the drawing table with up/down buttons to cycle stencil patterns.
 */
public class DrawingTableScreen extends OpenBlocksContainerScreen<DrawingTableMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            OpenBlocksConstants.MOD_ID, "textures/gui/drawing_table.png");

    private final BlockPos blockPos;

    public DrawingTableScreen(DrawingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        // Try to get block position from the container's BE
        this.blockPos = findBlockPos(menu);
    }

    private static BlockPos findBlockPos(DrawingTableMenu menu) {
        if (menu.slots.size() > 0) {
            var slot = menu.getSlot(0);
            if (slot.container instanceof DrawingTableBlockEntity be) {
                return be.getBlockPos();
            }
        }
        return BlockPos.ZERO;
    }

    @Override
    protected void init() {
        super.init();

        int buttonX = leftPos + 76;
        int buttonY = topPos + 20;

        // Up button (previous pattern)
        addRenderableWidget(Button.builder(Component.literal("\u25B2"), b -> {
            NetworkManager.sendToServer(new DrawingTableActionPacket(blockPos, DrawingTableActionPacket.ACTION_PREV));
        }).bounds(buttonX, buttonY, 20, 14).build());

        // Down button (next pattern)
        addRenderableWidget(Button.builder(Component.literal("\u25BC"), b -> {
            NetworkManager.sendToServer(new DrawingTableActionPacket(blockPos, DrawingTableActionPacket.ACTION_NEXT));
        }).bounds(buttonX, buttonY + 28, 20, 14).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // Draw the selected pattern name between the buttons
        int patternOrdinal = menu.getSelectedPatternOrdinal();
        StencilPattern[] patterns = StencilPattern.values();
        if (patternOrdinal >= 0 && patternOrdinal < patterns.length) {
            StencilPattern pattern = patterns[patternOrdinal];
            Component patternName = Component.translatable("openblocks.stencil." + pattern.getSerializedName());
            int textX = leftPos + 76;
            int textY = topPos + 38;
            guiGraphics.drawString(font, patternName, textX, textY, 0x404040, false);
        }
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
