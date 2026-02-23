package com.openblocks.goldenegg;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders the golden egg with rotation and vertical offset based on state.
 */
public class GoldenEggBlockEntityRenderer implements BlockEntityRenderer<GoldenEggBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public GoldenEggBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(GoldenEggBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        GoldenEggState state = be.getState();
        if (state == GoldenEggState.EXPLODING) return;

        float rotation = be.getRotation(partialTick);
        float offset = be.getOffset(partialTick);

        poseStack.pushPose();

        // Translate to center, apply rotation, translate back
        poseStack.translate(0.5, offset, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5, 0.0, -0.5);

        // Render the block model directly — renderSingleBlock() skips ENTITYBLOCK_ANIMATED blocks
        BlockState blockState = be.getBlockState();
        BakedModel model = blockRenderer.getBlockModel(blockState);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), consumer, blockState, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(GoldenEggBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
