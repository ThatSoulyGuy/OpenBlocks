package com.openblocks.automation;

import com.openblocks.core.gui.OpenBlocksContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlockPlacerScreen extends OpenBlocksContainerScreen<BlockPlacerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/gui/block_placer.png");

    public BlockPlacerScreen(BlockPlacerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
