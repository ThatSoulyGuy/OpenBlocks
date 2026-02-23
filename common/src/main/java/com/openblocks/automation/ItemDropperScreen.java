package com.openblocks.automation;

import com.openblocks.core.gui.OpenBlocksContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemDropperScreen extends OpenBlocksContainerScreen<ItemDropperMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/gui/item_dropper.png");

    public ItemDropperScreen(ItemDropperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
