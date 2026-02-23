package com.openblocks.entity.renderer;

import com.openblocks.core.registry.OpenBlocksItems;
import com.openblocks.entity.GoldenEyeEntity;
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

/**
 * Renders the Golden Eye entity as a spinning item sprite.
 */
public class GoldenEyeRenderer extends EntityRenderer<GoldenEyeEntity> {

    private final ItemRenderer itemRenderer;
    private final ItemStack displayStack;

    public GoldenEyeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.displayStack = new ItemStack(OpenBlocksItems.GOLDEN_EYE.get());
    }

    @Override
    public void render(GoldenEyeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 8.0f));
        poseStack.scale(0.75f, 0.75f, 0.75f);
        itemRenderer.renderStatic(displayStack, ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenEyeEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
