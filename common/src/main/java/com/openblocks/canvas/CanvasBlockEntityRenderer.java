package com.openblocks.canvas;

import com.mojang.blaze3d.vertex.PoseStack;
import com.openblocks.imaginary.StencilPattern;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * Renders per-face paint colors and stencil pattern overlays on canvas blocks.
 * Delegates to PaintOverlayRenderer for the actual quad rendering.
 */
public class CanvasBlockEntityRenderer implements BlockEntityRenderer<CanvasBlockEntity> {

    public CanvasBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        PaintOverlayRenderer.ensureStencilTextures();
    }

    @Override
    public void render(CanvasBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        for (Direction face : Direction.values()) {
            int color = be.getColor(face);
            StencilPattern pattern = be.getStencil(face);
            boolean hasCover = be.hasStencilCover(face);

            if (color != 0 || hasCover) {
                PaintOverlayRenderer.renderFaceOverlay(face, color, pattern, hasCover,
                        poseStack, bufferSource);
            }
        }
    }
}
