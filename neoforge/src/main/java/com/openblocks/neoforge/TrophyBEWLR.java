package com.openblocks.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.openblocks.trophy.TrophyItemRendererHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge BlockEntityWithoutLevelRenderer for trophy items.
 * Renders the pedestal block model + scaled entity on top.
 * Lazy singleton to avoid calling Minecraft.getInstance() too early.
 */
public class TrophyBEWLR extends BlockEntityWithoutLevelRenderer {

    private static TrophyBEWLR instance;

    public static TrophyBEWLR getInstance() {
        if (instance == null) {
            Minecraft mc = Minecraft.getInstance();
            instance = new TrophyBEWLR(mc);
        }
        return instance;
    }

    private TrophyBEWLR(Minecraft mc) {
        super(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        TrophyItemRendererHelper.renderTrophyItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
