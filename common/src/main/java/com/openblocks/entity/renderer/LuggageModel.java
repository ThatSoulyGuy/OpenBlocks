package com.openblocks.entity.renderer;

import com.openblocks.entity.LuggageEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Luggage entity model matching the original 1.12.2 OpenBlocks model exactly.
 * 128x64 texture, body (16x7x8), lid (16x2x8) pivoting at back,
 * and a single leg (1x4x1) rendered 21 times in a 7x3 grid with walk animation.
 */
public class LuggageModel extends EntityModel<LuggageEntity> {

    private final ModelPart body;
    private final ModelPart lid;
    private final ModelPart leg;

    // Stored animation parameters for use in renderToBuffer
    private float limbSwing;
    private float limbSwingAmount;

    public LuggageModel(ModelPart root) {
        this.body = root.getChild("body");
        this.lid = root.getChild("lid");
        this.leg = root.getChild("leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body: 16 wide, 7 tall, 8 deep, pivot at (0, 13, 0)
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-8.0f, 0.0f, -4.0f, 16.0f, 7.0f, 8.0f),
                PartPose.offset(0.0f, 13.0f, 0.0f));

        // Lid: 16 wide, 2 tall, 8 deep, pivot at (0, 13, 4) — hinges at back of body
        root.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 23).mirror()
                        .addBox(-8.0f, -2.0f, -8.0f, 16.0f, 2.0f, 8.0f),
                PartPose.offset(0.0f, 13.0f, 4.0f));

        // Single leg template: 1 wide, 4 tall, 1 deep — rendered 21 times dynamically
        root.addOrReplaceChild("leg",
                CubeListBuilder.create().texOffs(0, 41).mirror()
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LuggageEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Store for use in renderToBuffer (needed for dynamic leg rendering)
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;

        // Lid animation: opens slightly when walking (negative X rotation = opens toward back)
        lid.xRot = Math.min(0, Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount);
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                com.mojang.blaze3d.vertex.VertexConsumer buffer,
                                int packedLight, int packedOverlay,
                                int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        lid.render(poseStack, buffer, packedLight, packedOverlay, color);

        // Render 21 legs in a 7x3 grid, matching 1.12.2 exactly
        for (int x = -3; x <= 3; x++) {
            for (int z = -1; z <= 1; z++) {
                leg.x = x * 2.0f;
                leg.y = 20.0f;
                leg.z = z * 2.0f;
                leg.xRot = Mth.cos(limbSwing + (x * z) * 0.6662f) * 1.4f * limbSwingAmount;
                leg.render(poseStack, buffer, packedLight, packedOverlay, color);
            }
        }
    }
}
