package com.openblocks.grave;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * Renders the player name and "R.I.P." text on the front face of a grave block.
 */
public class GraveBlockEntityRenderer implements BlockEntityRenderer<GraveBlockEntity> {

    private final Font font;

    public GraveBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(GraveBlockEntity grave, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String playerName = grave.getPlayerName();
        if (playerName == null || playerName.isEmpty()) return;

        Direction facing = grave.getBlockState().getValue(GraveBlock.FACING);

        poseStack.pushPose();

        // Move to center of block
        poseStack.translate(0.5, 0.0, 0.5);

        // Rotate to face the correct direction
        float yaw = switch (facing) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        // Position text on the front face of the headstone
        // Grave shape is 16x6x16, headstone area is the upper portion
        poseStack.translate(0.0, 0.35, -0.501);

        // Scale down text to fit on the block face
        float scale = 0.01f;
        poseStack.scale(scale, -scale, scale);

        // Render "R.I.P." header
        String header = "R.I.P.";
        int headerWidth = font.width(header);
        font.drawInBatch(header, -headerWidth / 2.0f, -6, 0xFFFFFFFF,
                false, poseStack.last().pose(), bufferSource,
                Font.DisplayMode.NORMAL, 0, packedLight);

        // Render player name below
        // Truncate if too long
        String displayName = playerName;
        int maxWidth = 90;
        if (font.width(displayName) > maxWidth) {
            while (font.width(displayName + "...") > maxWidth && displayName.length() > 1) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            displayName += "...";
        }

        int nameWidth = font.width(displayName);
        font.drawInBatch(displayName, -nameWidth / 2.0f, 4, 0xFFCCCCCC,
                false, poseStack.last().pose(), bufferSource,
                Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }
}
