package com.openblocks.interaction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openblocks.OpenBlocksConstants;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the cannon block entity with the original 1.12.2 model:
 * body (6x6x6), shooter barrel (4x4x6), base plate (12x1x12), and wheels (1x6x6).
 * Texture: textures/models/cannon.png (64x32)
 */
public class CannonBlockEntityRenderer implements BlockEntityRenderer<CannonBlockEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "textures/models/cannon.png");

    private static final float DEG30 = (float) Math.toRadians(30);

    private final ModelPart body;
    private final ModelPart shooter;
    private final ModelPart base;
    private final ModelPart wheel;

    public CannonBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Build model matching 1.12.2 ModelCannon exactly
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body: 6x6x6 cube, pivot at (0, 11, 3), initial pitch ~20 degrees
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-3.0f, -5.0f, -3.0f, 6.0f, 6.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, 11.0f, 3.0f, 0.3490659f, 0.0f, 0.0f));

        // Shooter barrel: 4x4x6 extending forward from the body
        root.addOrReplaceChild("shooter",
                CubeListBuilder.create().texOffs(34, 0).mirror()
                        .addBox(-2.0f, -4.0f, 2.0f, 4.0f, 4.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, 11.0f, 3.0f, 0.3490659f, 0.0f, 0.0f));

        // Base plate: 12x1x12 flat
        root.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(14, 19).mirror()
                        .addBox(-6.0f, 0.0f, -6.0f, 12.0f, 1.0f, 12.0f),
                PartPose.offset(0.0f, 15.0f, 0.0f));

        // Wheel: 1x6x6 — rendered multiple times at different angles
        root.addOrReplaceChild("wheel",
                CubeListBuilder.create().texOffs(0, 20).mirror()
                        .addBox(3.0f, -3.0f, -3.0f, 1.0f, 6.0f, 6.0f),
                PartPose.offset(0.0f, 11.0f, 3.0f));

        ModelPart modelRoot = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        this.body = modelRoot.getChild("body");
        this.shooter = modelRoot.getChild("shooter");
        this.base = modelRoot.getChild("base");
        this.wheel = modelRoot.getChild("wheel");
    }

    @Override
    public void render(CannonBlockEntity cannon, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = cannon.getBlockState().getValue(CannonBlock.FACING);
        float pitch = cannon.getPitch();

        poseStack.pushPose();

        // Position at center of block, flipped Y (model space has Y=0 at top)
        poseStack.translate(0.5f, 1.0f, 0.5f);

        // Rotate to face the correct direction
        float yaw = switch (facing) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));

        // Set pitch on shooter and body
        float pitchRad = (float) Math.toRadians(pitch);
        shooter.xRot = pitchRad;
        body.xRot = pitchRad;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));

        body.render(poseStack, consumer, packedLight, packedOverlay);
        shooter.render(poseStack, consumer, packedLight, packedOverlay);
        base.render(poseStack, consumer, packedLight, packedOverlay);

        // Render 6 wheel spokes (3 on each side)
        wheel.zRot = 0;
        wheel.xRot = 0;
        wheel.x = 0;
        wheel.render(poseStack, consumer, packedLight, packedOverlay);
        wheel.xRot = DEG30;
        wheel.x = -0.01f;
        wheel.render(poseStack, consumer, packedLight, packedOverlay);
        wheel.xRot = DEG30 * 2;
        wheel.x = -0.02f;
        wheel.render(poseStack, consumer, packedLight, packedOverlay);

        wheel.zRot = (float) Math.PI;
        wheel.xRot = 0;
        wheel.x = 0;
        wheel.render(poseStack, consumer, packedLight, packedOverlay);
        wheel.xRot = DEG30;
        wheel.x = -0.01f;
        wheel.render(poseStack, consumer, packedLight, packedOverlay);
        wheel.xRot = DEG30 * 2;
        wheel.x = -0.02f;
        wheel.render(poseStack, consumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
