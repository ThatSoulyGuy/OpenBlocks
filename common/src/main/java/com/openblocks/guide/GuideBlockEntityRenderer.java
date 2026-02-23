package com.openblocks.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders translucent marker cubes at each position in the guide's shape.
 */
public class GuideBlockEntityRenderer implements BlockEntityRenderer<GuideBlockEntity> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("block/white_concrete");
    private static final float MARKER_SIZE = 0.4f;
    private static final float MARKER_MIN = 0.5f - MARKER_SIZE / 2;
    private static final float MARKER_MAX = 0.5f + MARKER_SIZE / 2;

    public GuideBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GuideBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!be.shouldRender()) return;

        List<BlockPos> coords = be.getShapeCoords();
        if (coords.isEmpty()) return;

        int color = be.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 0.4f;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(WHITE_TEXTURE);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        for (BlockPos offset : coords) {
            poseStack.pushPose();
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());

            Matrix4f matrix = poseStack.last().pose();

            // Render small translucent cube
            // Top
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MAX, MARKER_MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MAX, MARKER_MAX).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MAX, MARKER_MAX).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MAX, MARKER_MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);

            // Bottom
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MIN, MARKER_MAX).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MIN, MARKER_MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MIN, MARKER_MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MIN, MARKER_MAX).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);

            // North (z=min)
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MAX, MARKER_MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MAX, MARKER_MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MIN, MARKER_MIN).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MIN, MARKER_MIN).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

            // South (z=max)
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MAX, MARKER_MAX).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MAX, MARKER_MAX).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MIN, MARKER_MAX).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MIN, MARKER_MAX).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

            // West (x=min)
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MAX, MARKER_MAX).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MAX, MARKER_MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MIN, MARKER_MIN).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
            consumer.addVertex(matrix, MARKER_MIN, MARKER_MIN, MARKER_MAX).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);

            // East (x=max)
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MAX, MARKER_MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MAX, MARKER_MAX).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MIN, MARKER_MAX).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
            consumer.addVertex(matrix, MARKER_MAX, MARKER_MIN, MARKER_MIN).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);

            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(GuideBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
