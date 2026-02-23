package com.openblocks.projector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

/**
 * Renders a 3D holographic projection of a map above the projector block.
 * Samples map pixels and renders translucent colored columns.
 */
public class ProjectorBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {

    private static final int SAMPLE_SIZE = 64;
    private static final float PIXEL_SIZE = 1.0f / SAMPLE_SIZE;
    private static final float MAX_HEIGHT = 3.0f;
    private static final float BASE_Y = 0.55f; // Just above the half-slab
    private static final int BASE_ALPHA = 120;

    public ProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ProjectorBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (be.getMapId() < 0) return;

        MapItemSavedData mapData = be.getMapData();
        if (mapData == null) return;

        byte[] colors = mapData.colors;
        if (colors == null || colors.length != 128 * 128) return;

        int rotation = be.getRotation();

        poseStack.pushPose();
        // Center rotation
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90 * rotation));
        poseStack.translate(-0.5, 0, -0.5);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        for (int sx = 0; sx < SAMPLE_SIZE; sx++) {
            for (int sz = 0; sz < SAMPLE_SIZE; sz++) {
                // Sample from 128x128 map at every 2nd pixel
                int mx = sx * 2;
                int mz = sz * 2;
                int colorIndex = Byte.toUnsignedInt(colors[mx + mz * 128]);

                if (colorIndex < 4) continue; // Skip transparent/unused colors

                int rgb = mapColorToRgb(colorIndex);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Height based on brightness
                float brightness = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
                float height = 0.05f + brightness * MAX_HEIGHT;

                float x0 = sx * PIXEL_SIZE;
                float z0 = sz * PIXEL_SIZE;
                float x1 = x0 + PIXEL_SIZE;
                float z1 = z0 + PIXEL_SIZE;
                float y0 = BASE_Y;
                float y1 = BASE_Y + height;

                renderColumn(consumer, matrix, x0, y0, z0, x1, y1, z1,
                        r / 255.0f, g / 255.0f, b / 255.0f, BASE_ALPHA / 255.0f,
                        packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private void renderColumn(VertexConsumer consumer, Matrix4f matrix,
                               float x0, float y0, float z0, float x1, float y1, float z1,
                               float r, float g, float b, float a,
                               int light, int overlay) {
        // Top face
        consumer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    /**
     * Converts a Minecraft map color index to RGB.
     * Map colors are stored as base*4 + shade (shade 0-3 = dark to bright).
     */
    private static int mapColorToRgb(int colorIndex) {
        // Use MapColor to get the RGB value
        int baseId = colorIndex / 4;
        int shade = colorIndex % 4;

        // Shade multipliers: 0=180, 1=220, 2=255, 3=135
        int multiplier = switch (shade) {
            case 0 -> 180;
            case 1 -> 220;
            case 2 -> 255;
            case 3 -> 135;
            default -> 255;
        };

        net.minecraft.world.level.material.MapColor mapColor = net.minecraft.world.level.material.MapColor.byId(baseId);
        int baseRgb = mapColor.col;

        int r = ((baseRgb >> 16) & 0xFF) * multiplier / 255;
        int g = ((baseRgb >> 8) & 0xFF) * multiplier / 255;
        int b = (baseRgb & 0xFF) * multiplier / 255;

        return (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean shouldRenderOffScreen(ProjectorBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
