package com.openblocks.imaginary;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders imaginary blocks as translucent tinted cubes with fade-in/fade-out.
 */
public class ImaginaryBlockEntityRenderer implements BlockEntityRenderer<ImaginaryBlockEntity> {

    private static final ResourceLocation PENCIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("openblocks", "block/pencil_block");
    private static final ResourceLocation CRAYON_TEXTURE = ResourceLocation.fromNamespaceAndPath("openblocks", "block/crayon_block");

    public ImaginaryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ImaginaryBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean visible = be.isVisibleFor(player);
        float currentVisibility = be.getVisibility();
        float fadingSpeed = OpenBlocksConfig.Imaginary.fadingSpeed;

        // Update fade
        if (visible && currentVisibility < 1.0f) {
            currentVisibility = Math.min(1.0f, currentVisibility + fadingSpeed);
            be.setVisibility(currentVisibility);
        } else if (!visible && currentVisibility > 0.0f) {
            currentVisibility = Math.max(0.0f, currentVisibility - fadingSpeed);
            be.setVisibility(currentVisibility);
        }

        if (currentVisibility <= 0.0f) return;

        // Determine color
        float r, g, b;
        Integer color = be.getColor();
        if (color != null) {
            r = ((color >> 16) & 0xFF) / 255.0f;
            g = ((color >> 8) & 0xFF) / 255.0f;
            b = (color & 0xFF) / 255.0f;
        } else {
            // Pencil: light gray
            r = 0.8f;
            g = 0.8f;
            b = 0.8f;
        }
        float a = currentVisibility * 0.7f;

        ResourceLocation textureLoc = be.getColor() != null ? CRAYON_TEXTURE : PENCIL_TEXTURE;
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(textureLoc);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        poseStack.pushPose();

        Matrix4f matrix = poseStack.last().pose();
        float min = 0.0f;
        float max = 1.0f;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face
        consumer.addVertex(matrix, min, max, min).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, min, max, max).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, max, max, max).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, max, max, min).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);

        // Bottom face
        consumer.addVertex(matrix, min, min, max).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, min, min, min).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, max, min, min).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, max, min, max).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);

        // North face (z=min)
        consumer.addVertex(matrix, min, max, min).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, max, max, min).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, max, min, min).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, min, min, min).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

        // South face (z=max)
        consumer.addVertex(matrix, max, max, max).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, min, max, max).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, min, min, max).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, max, min, max).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

        // West face (x=min)
        consumer.addVertex(matrix, min, max, max).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, min, max, min).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, min, min, min).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, min, min, max).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);

        // East face (x=max)
        consumer.addVertex(matrix, max, max, min).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, max, max, max).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, max, min, max).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, max, min, min).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);

        poseStack.popPose();
    }
}
