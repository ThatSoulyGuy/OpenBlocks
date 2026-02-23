package com.openblocks.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openblocks.OpenBlocksConstants;
import com.openblocks.entity.MagnetEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Magnet entity with the original 1.12.2 model:
 * 3-tier pyramid (6x1x6, 4x1x4, 2x1x2) at 1/8 scale.
 * Texture: textures/models/magnet.png (32x32)
 */
public class MagnetRenderer extends EntityRenderer<MagnetEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "textures/models/magnet.png");

    private final ModelPart magnetModel;

    public MagnetRenderer(EntityRendererProvider.Context context) {
        super(context);

        // Build model matching 1.12.2 EntityMagnetRenderer exactly
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 3-tier pyramid: bottom 6x1x6, middle 4x1x4, top 2x1x2
        root.addOrReplaceChild("magnet",
                CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 1.0f, 6.0f)
                        .texOffs(0, 7).addBox(-2.0f, 1.0f, -2.0f, 4.0f, 1.0f, 4.0f)
                        .texOffs(0, 12).addBox(-1.0f, 2.0f, -1.0f, 2.0f, 1.0f, 2.0f),
                PartPose.ZERO);

        ModelPart modelRoot = LayerDefinition.create(mesh, 32, 32).bakeRoot();
        this.magnetModel = modelRoot.getChild("magnet");
    }

    @Override
    public void render(MagnetEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        poseStack.translate(0, entity.getBbHeight() - 0.4f, 0);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        magnetModel.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MagnetEntity entity) {
        return TEXTURE;
    }
}
