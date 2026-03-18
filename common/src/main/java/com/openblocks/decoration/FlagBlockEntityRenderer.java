package com.openblocks.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openblocks.core.util.ColorMeta;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders the flag cloth attached to the flag pole, rotated by the stored angle
 * and tinted by the stored color.
 */
public class FlagBlockEntityRenderer implements BlockEntityRenderer<FlagBlockEntity> {

    private static final ResourceLocation FLAG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/block/flag.png");

    // Flag cloth dimensions
    private static final float FLAG_WIDTH = 10.0f / 16.0f;
    private static final float FLAG_HEIGHT = 8.0f / 16.0f;

    // Pole position (offset from block center)
    private static final float POLE_OFFSET = 0.5f / 16.0f;

    public FlagBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FlagBlockEntity flag, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float angle = flag.getAngle();
        ColorMeta color = flag.getColor();

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;

        poseStack.pushPose();

        // Move to center of block
        poseStack.translate(0.5, 0.0, 0.5);

        // Apply the stored rotation angle around Y axis
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(FLAG_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // UVs map to the full texture (entity render types use 0-1 range)
        float u0 = 0.0f;
        float u1 = 1.0f;
        float v0 = 0.0f;
        float v1 = 1.0f;

        // Flag cloth hangs from the top of the pole, extends to one side
        float x0 = POLE_OFFSET;
        float x1 = POLE_OFFSET + FLAG_WIDTH;
        float y0 = 0.5f;                    // Mid-height of block
        float y1 = y0 + FLAG_HEIGHT;        // Top of flag

        // Single quad — entityCutoutNoCull renders both sides
        consumer.addVertex(matrix, x0, y1, 0).setColor(r, g, b, 1.0f).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, x0, y0, 0).setColor(r, g, b, 1.0f).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, x1, y0, 0).setColor(r, g, b, 1.0f).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, 1.0f).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

        poseStack.popPose();
    }
}
