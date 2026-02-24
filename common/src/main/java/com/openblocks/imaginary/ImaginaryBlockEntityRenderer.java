package com.openblocks.imaginary;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders imaginary blocks as translucent tinted shapes with fade-in/fade-out.
 */
public class ImaginaryBlockEntityRenderer implements BlockEntityRenderer<ImaginaryBlockEntity> {

    private static final ResourceLocation PENCIL_TEXTURE = ResourceLocation.fromNamespaceAndPath("openblocks", "block/pencil_block");
    private static final ResourceLocation CRAYON_TEXTURE = ResourceLocation.fromNamespaceAndPath("openblocks", "block/crayon_block");

    private static final double PANEL_HEIGHT = 0.1;

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

        // Apply facing rotation for asymmetric shapes (stairs)
        Direction facing = be.getFacing();
        float rotationDeg = switch (facing) {
            case NORTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0; // SOUTH is default orientation
        };
        if (rotationDeg != 0) {
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationDeg));
            poseStack.translate(-0.5, 0, -0.5);
        }

        Matrix4f matrix = poseStack.last().pose();

        ImaginaryShape shape = be.getShape();
        switch (shape) {
            case BLOCK -> renderBox(consumer, matrix, sprite, r, g, b, a, packedLight, packedOverlay,
                    0, 0, 0, 1, 1, 1);
            case PANEL -> renderBox(consumer, matrix, sprite, r, g, b, a, packedLight, packedOverlay,
                    0, (float)(1 - PANEL_HEIGHT), 0, 1, 1, 1);
            case HALF_PANEL -> renderBox(consumer, matrix, sprite, r, g, b, a, packedLight, packedOverlay,
                    0, (float)(0.5 - PANEL_HEIGHT), 0, 1, 0.5f, 1);
            case STAIRS -> {
                // Lower step (front half: z=0..0.5)
                renderBox(consumer, matrix, sprite, r, g, b, a, packedLight, packedOverlay,
                        0, (float)(0.5 - PANEL_HEIGHT), 0, 1, 0.5f, 0.5f);
                // Upper step (back half: z=0.5..1)
                renderBox(consumer, matrix, sprite, r, g, b, a, packedLight, packedOverlay,
                        0, (float)(1 - PANEL_HEIGHT), 0.5f, 1, 1, 1);
            }
        }

        poseStack.popPose();
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, TextureAtlasSprite sprite,
                                  float r, float g, float b, float a, int packedLight, int packedOverlay,
                                  float x0, float y0, float z0, float x1, float y1, float z1) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face (y=y1)
        consumer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);

        // Bottom face (y=y0)
        consumer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);

        // North face (z=z0)
        consumer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

        // South face (z=z1)
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

        // West face (x=x0)
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);

        // East face (x=x1)
        consumer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
    }
}
