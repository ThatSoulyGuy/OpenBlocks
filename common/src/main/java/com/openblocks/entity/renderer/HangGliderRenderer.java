package com.openblocks.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openblocks.OpenBlocksConstants;
import com.openblocks.entity.HangGliderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

/**
 * Renders the Hang Glider entity exactly as in 1.12.2:
 * a flat textured plane (4.8 x 4.8 units) above the player.
 * Deployed: large canopy behind/above player. Packed: small, rotated 90 on X, scaled 0.4.
 * First-person deployed: canopy above head only.
 * Texture: textures/models/hang_glider.png
 */
public class HangGliderRenderer extends EntityRenderer<HangGliderEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "textures/models/hang_glider.png");

    private static final float QUAD_HALF_SIZE = 2.4f;
    private static final float ONGROUND_ROTATION = 90f;

    public HangGliderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HangGliderEntity glider, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Player owner = glider.getPlayer();
        if (owner == null) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isLocalPlayer = owner == mc.player;
        boolean isFpp = mc.options.getCameraType().isFirstPerson();
        boolean isDeployed = glider.isDeployed();

        // In first person + not deployed = don't render
        if (isLocalPlayer && isFpp && !isDeployed) return;

        float rotation = interpolateRotation(glider.yRotO, glider.getYRot(), partialTick);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - rotation));

        if (isLocalPlayer && isFpp) {
            // Move over head when flying in first person
            poseStack.translate(0, 0.7, 0);
        } else {
            if (!isDeployed) {
                // Packed: move up a little and forward
                poseStack.translate(0, 0.2, 0.3);
            } else {
                // Deployed: move closer to back and forward
                poseStack.translate(0, -0.5, -1.0);
            }
        }

        if (!isDeployed) {
            // Packed: rotate 90 degrees on X and scale down
            poseStack.mulPose(Axis.XP.rotationDegrees(ONGROUND_ROTATION));
            poseStack.scale(0.4f, 1f, 0.4f);
        }

        // Render the quad
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // Match 1.12.2 vertex order and UV mapping exactly
        consumer.addVertex(matrix, QUAD_HALF_SIZE, 0, QUAD_HALF_SIZE)
                .setColor(255, 255, 255, 255).setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, -QUAD_HALF_SIZE, 0, QUAD_HALF_SIZE)
                .setColor(255, 255, 255, 255).setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, -QUAD_HALF_SIZE, 0, -QUAD_HALF_SIZE)
                .setColor(255, 255, 255, 255).setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, QUAD_HALF_SIZE, 0, -QUAD_HALF_SIZE)
                .setColor(255, 255, 255, 255).setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);

        poseStack.popPose();
    }

    private static float interpolateRotation(float prevRotation, float nextRotation, float partialTick) {
        float rotation = nextRotation - prevRotation;

        while (rotation < -180.0f)
            rotation += 360.0f;
        while (rotation >= 180.0f)
            rotation -= 360.0f;

        return prevRotation + partialTick * rotation;
    }

    @Override
    public ResourceLocation getTextureLocation(HangGliderEntity entity) {
        return TEXTURE;
    }
}
