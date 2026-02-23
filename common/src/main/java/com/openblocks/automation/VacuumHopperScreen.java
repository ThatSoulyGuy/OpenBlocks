package com.openblocks.automation;

import com.openblocks.core.gui.OpenBlocksContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class VacuumHopperScreen extends OpenBlocksContainerScreen<VacuumHopperMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/gui/vacuum_hopper.png");

    public VacuumHopperScreen(VacuumHopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 148;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}
