package com.openblocks.glyph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GlyphEntityRenderer extends EntityRenderer<GlyphEntity> {

    private final ItemRenderer itemRenderer;

    public GlyphEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GlyphEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Rotate to face outward from wall
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entity.getYRot()));

        // Scale to 0.5 (8x8 pixels = half a block)
        poseStack.scale(0.5f, 0.5f, 0.5f);

        ItemStack stack = GlyphItem.createGlyph(entity.getCharIndex());
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                entity.level(), entity.getId());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GlyphEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
