package com.openblocks.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.openblocks.OpenBlocksConstants;
import com.openblocks.entity.CartographerEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Renders the Cartographer entity with the original 1.12.2 model:
 * body (5x2x5), base (3x1x3 + 2 legs at 1x4x1 each), eye of ender,
 * and coordinate text billboard.
 * Texture: textures/models/cartographer.png (32x32)
 */
public class CartographerRenderer extends EntityRenderer<CartographerEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "textures/models/cartographer.png");

    private final ModelPart body;
    private final ModelPart base;
    private final ItemRenderer itemRenderer;
    private final Font font;
    private final ItemStack enderEyeStack = new ItemStack(Items.ENDER_EYE);

    public CartographerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.font = context.getFont();

        // Build model matching 1.12.2 ModelCartographer
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body: 5x2x5 centered
        PartDefinition bodyDef = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5f, -1.5f, -2.5f, 5.0f, 2.0f, 5.0f),
                PartPose.ZERO);

        // Base (child of body): 3x1x3 platform + two legs (1x4x1 each)
        bodyDef.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 7).addBox(-1.5f, 0.5f, -1.5f, 3.0f, 1.0f, 3.0f)
                        .texOffs(0, 11).addBox(-0.5f, 0.5f, -2.5f, 1.0f, 4.0f, 1.0f)
                        .texOffs(4, 11).addBox(-0.5f, 0.5f, 1.5f, 1.0f, 4.0f, 1.0f),
                PartPose.ZERO);

        ModelPart modelRoot = LayerDefinition.create(mesh, 32, 32).bakeRoot();
        this.body = modelRoot.getChild("body");
        this.base = this.body.getChild("base");
    }

    @Override
    public void render(CartographerEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Update eye animation every frame (matching 1.12.2: called from doRender)
        entity.updateEye();

        poseStack.pushPose();

        // Gentle bobbing animation
        float bob = (float) Math.sin((entity.tickCount + partialTick) * 0.15) * 0.1f;
        poseStack.translate(0, bob + 0.5, 0);

        // In 1.12.2, the body itself doesn't rotate — only the base child part
        // rotates via base.rotateAngleY = eyeYaw. No overall entity spin.
        base.yRot = entity.eyeYaw;

        // Render body model (no Y-flip needed — model coordinates map directly to world Y-up)
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        body.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        // Render eye of ender (matching 1.12.2 ModelCartographer.renderEye)
        // translate up 0.25 blocks, rotate by eyeYaw+90 on Y and eyePitch on X, scale 3/16
        poseStack.pushPose();
        poseStack.translate(0, 0.25, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(entity.eyeYaw) + 90));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(entity.eyePitch)));
        float eyeScale = 3.0f / 16.0f;
        poseStack.scale(eyeScale, eyeScale, eyeScale);
        itemRenderer.renderStatic(enderEyeStack, ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());
        poseStack.popPose();

        poseStack.popPose();

        // Render coordinate text as billboard below entity
        renderCoordinateText(entity, poseStack, bufferSource, packedLight);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderCoordinateText(CartographerEntity entity, PoseStack poseStack,
                                      MultiBufferSource bufferSource, int packedLight) {
        String coords = String.format("%d, %d", entity.getNewMapCenterX(), entity.getNewMapCenterZ());

        poseStack.pushPose();
        // Position below the entity body
        poseStack.translate(0, -0.25, 0);
        // Billboard: face the camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        // Scale to small text (similar to name tag but smaller)
        float textScale = 0.015f;
        poseStack.scale(textScale, -textScale, textScale);

        int textWidth = font.width(coords);
        float x = -textWidth / 2.0f;
        font.drawInBatch(coords, x, 0, 0xFFFFFFFF, false, poseStack.last().pose(),
                bufferSource, Font.DisplayMode.NORMAL, 0x40000000, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(CartographerEntity entity) {
        return TEXTURE;
    }
}
