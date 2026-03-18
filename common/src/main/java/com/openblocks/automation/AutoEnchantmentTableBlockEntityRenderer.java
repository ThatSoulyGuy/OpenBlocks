package com.openblocks.automation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.util.Mth;

public class AutoEnchantmentTableBlockEntityRenderer implements BlockEntityRenderer<AutoEnchantmentTableBlockEntity> {

    private final BookModel bookModel;

    public AutoEnchantmentTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(AutoEnchantmentTableBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75f, 0.5f);

        float time = be.time + partialTick;
        poseStack.translate(0.0f, 0.1f + Mth.sin(time * 0.1f) * 0.01f, 0.0f);

        float rotDelta = be.rot - be.oRot;
        while (rotDelta >= (float) Math.PI) rotDelta -= (float) Math.PI * 2f;
        while (rotDelta < -(float) Math.PI) rotDelta += (float) Math.PI * 2f;
        float rotation = be.oRot + rotDelta * partialTick;

        poseStack.mulPose(Axis.YP.rotation(-rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0f));

        float flip = Mth.lerp(partialTick, be.oFlip, be.flip);
        float leftPage = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
        float rightPage = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;
        float openness = Mth.lerp(partialTick, be.oOpen, be.open);

        bookModel.setupAnim(time, Mth.clamp(leftPage, 0.0f, 1.0f), Mth.clamp(rightPage, 0.0f, 1.0f), openness);

        VertexConsumer buffer = EnchantTableRenderer.BOOK_LOCATION.buffer(bufferSource, net.minecraft.client.renderer.RenderType::entitySolid);
        bookModel.render(poseStack, buffer, packedLight, packedOverlay, -1);

        poseStack.popPose();
    }
}
