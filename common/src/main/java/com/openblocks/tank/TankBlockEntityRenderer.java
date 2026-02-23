package com.openblocks.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix4f;

/**
 * Renders the fluid volume inside a tank block as translucent quads.
 */
public class TankBlockEntityRenderer implements BlockEntityRenderer<TankBlockEntity> {

    private static final float INSET = 0.01f;
    private static final float MIN = INSET;
    private static final float MAX = 1.0f - INSET;

    public TankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TankBlockEntity tank, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (tank.isEmpty()) return;

        float fillRatio = tank.getFillRatio();
        if (fillRatio <= 0) return;

        Fluid fluid = tank.getFluidType();
        if (fluid == Fluids.EMPTY) return;

        // Get fluid texture
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        ResourceLocation stillTexture = getFluidStillTexture(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        int color = getFluidColor(fluid);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 0.8f;

        float height = MIN + fillRatio * (MAX - MIN);

        poseStack.pushPose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top face
        consumer.addVertex(matrix, MIN, height, MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, MIN, height, MAX).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, MAX, height, MAX).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, MAX, height, MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);

        // Bottom face
        consumer.addVertex(matrix, MIN, MIN, MAX).setColor(r, g, b, a).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, MIN, MIN, MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, MAX, MIN, MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, MAX, MIN, MAX).setColor(r, g, b, a).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);

        // Interpolate v for side height
        float vHeight = v0 + (v1 - v0) * fillRatio;

        // North face (z=MIN)
        consumer.addVertex(matrix, MIN, height, MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, MAX, height, MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, MAX, MIN, MIN).setColor(r, g, b, a).setUv(u1, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, MIN, MIN, MIN).setColor(r, g, b, a).setUv(u0, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);

        // South face (z=MAX)
        consumer.addVertex(matrix, MAX, height, MAX).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, MIN, height, MAX).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, MIN, MIN, MAX).setColor(r, g, b, a).setUv(u1, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, MAX, MIN, MAX).setColor(r, g, b, a).setUv(u0, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);

        // West face (x=MIN)
        consumer.addVertex(matrix, MIN, height, MAX).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, MIN, height, MIN).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, MIN, MIN, MIN).setColor(r, g, b, a).setUv(u1, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, MIN, MIN, MAX).setColor(r, g, b, a).setUv(u0, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);

        // East face (x=MAX)
        consumer.addVertex(matrix, MAX, height, MIN).setColor(r, g, b, a).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, MAX, height, MAX).setColor(r, g, b, a).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, MAX, MIN, MAX).setColor(r, g, b, a).setUv(u1, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, MAX, MIN, MIN).setColor(r, g, b, a).setUv(u0, vHeight).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);

        poseStack.popPose();
    }

    private static ResourceLocation getFluidStillTexture(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return ResourceLocation.withDefaultNamespace("block/water_still");
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return ResourceLocation.withDefaultNamespace("block/lava_still");
        }
        // Fallback for modded fluids
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        return ResourceLocation.fromNamespaceAndPath(fluidId.getNamespace(), "block/" + fluidId.getPath() + "_still");
    }

    private static int getFluidColor(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return 0x3F76E4; // Default water blue
        }
        return 0xFFFFFF; // No tint for lava and others
    }
}
