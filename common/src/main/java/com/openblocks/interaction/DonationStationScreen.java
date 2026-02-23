package com.openblocks.interaction;

import com.openblocks.core.gui.OpenBlocksContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DonationStationScreen extends OpenBlocksContainerScreen<DonationStationMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/gui/donation_station.png");

    public DonationStationScreen(DonationStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
