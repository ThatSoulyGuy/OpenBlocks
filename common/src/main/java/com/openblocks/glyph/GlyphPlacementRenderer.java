package com.openblocks.glyph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renders a placement grid and glyph preview on the block face when
 * the player is holding a glyph item and looking at a wall.
 */
public final class GlyphPlacementRenderer {

    private static final float GRID_ALPHA = 0.35f;
    private static final float MARKER_ALPHA = 0.4f;
    private static final float FACE_OFFSET = 0.005f;

    public static void render(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (!(held.getItem() instanceof GlyphItem)) {
            held = mc.player.getOffhandItem();
            if (!(held.getItem() instanceof GlyphItem)) return;
        }

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        Direction face = blockHit.getDirection();
        if (face == Direction.UP || face == Direction.DOWN) return;

        BlockPos pos = blockHit.getBlockPos();
        Vec3 camPos = camera.getPosition();

        // Calculate face-local UV from hit location (matching GlyphItem.useOn exactly)
        Vec3 hitLoc = blockHit.getLocation();
        double relX = hitLoc.x - pos.getX();
        double relY = hitLoc.y - pos.getY();
        double relZ = hitLoc.z - pos.getZ();

        double hitU;
        switch (face) {
            case NORTH, SOUTH -> hitU = relX;
            case WEST, EAST -> hitU = relZ;
            default -> { return; }
        }
        double hitV = relY;

        // Snap marker corner to 1/16 grid, centering 8px marker on cursor
        double markerU = Math.floor(hitU * 16 - 4) / 16.0;
        double markerV = Math.floor(hitV * 16 - 4) / 16.0;
        markerU = Math.max(0, Math.min(markerU, 0.5));
        markerV = Math.max(0, Math.min(markerV, 0.5));

        poseStack.pushPose();
        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);

        Matrix4f mat = poseStack.last().pose();

        // Draw grid (individual line segments using RenderType.lines)
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        drawGrid(mat, lines, face);

        // Draw marker quad
        VertexConsumer quads = bufferSource.getBuffer(RenderType.debugQuads());
        drawMarker(mat, quads, face, markerU, markerV);

        // Draw glyph item preview
        drawGlyphPreview(poseStack, bufferSource, held, face, markerU, markerV);

        poseStack.popPose();

        bufferSource.endBatch();
    }

    private static void drawGrid(Matrix4f mat, VertexConsumer consumer, Direction face) {
        // 17 lines per axis (every 1/16 block)
        for (int i = 0; i <= 16; i++) {
            float t = i / 16.0f;
            // Line along u-axis (horizontal)
            float[] a = facePoint(face, 0, t);
            float[] b = facePoint(face, 1, t);
            consumer.addVertex(mat, a[0], a[1], a[2]).setColor(0.5f, 0.5f, 0.5f, GRID_ALPHA).setNormal(0, 1, 0);
            consumer.addVertex(mat, b[0], b[1], b[2]).setColor(0.5f, 0.5f, 0.5f, GRID_ALPHA).setNormal(0, 1, 0);

            // Line along v-axis (vertical)
            float[] c = facePoint(face, t, 0);
            float[] d = facePoint(face, t, 1);
            consumer.addVertex(mat, c[0], c[1], c[2]).setColor(0.5f, 0.5f, 0.5f, GRID_ALPHA).setNormal(0, 1, 0);
            consumer.addVertex(mat, d[0], d[1], d[2]).setColor(0.5f, 0.5f, 0.5f, GRID_ALPHA).setNormal(0, 1, 0);
        }
    }

    private static void drawMarker(Matrix4f mat, VertexConsumer consumer, Direction face,
                                    double markerU, double markerV) {
        float u0 = (float) markerU;
        float v0 = (float) markerV;
        float u1 = (float) (markerU + 0.5); // 8px = 0.5 blocks
        float v1 = (float) (markerV + 0.5);

        float[] a = facePoint(face, u0, v0);
        float[] b = facePoint(face, u1, v0);
        float[] c = facePoint(face, u1, v1);
        float[] d = facePoint(face, u0, v1);

        // Two tris to form quad (draw both winding orders for double-sided)
        consumer.addVertex(mat, a[0], a[1], a[2]).setColor(1f, 1f, 1f, MARKER_ALPHA);
        consumer.addVertex(mat, b[0], b[1], b[2]).setColor(1f, 1f, 1f, MARKER_ALPHA);
        consumer.addVertex(mat, c[0], c[1], c[2]).setColor(1f, 1f, 1f, MARKER_ALPHA);
        consumer.addVertex(mat, d[0], d[1], d[2]).setColor(1f, 1f, 1f, MARKER_ALPHA);
    }

    private static void drawGlyphPreview(PoseStack poseStack, MultiBufferSource bufferSource,
                                          ItemStack held, Direction face,
                                          double markerU, double markerV) {
        Minecraft mc = Minecraft.getInstance();

        // Center of marker in face-local coords
        double centerU = markerU + 0.25; // center of 8px marker
        double centerV = markerV + 0.25;
        float[] center = facePoint(face, (float) centerU, (float) centerV);

        // Extra offset so preview floats slightly in front of the grid
        float extraOff = FACE_OFFSET * 2;

        poseStack.pushPose();
        poseStack.translate(
                center[0] + face.getStepX() * extraOff,
                center[1] + face.getStepY() * extraOff,
                center[2] + face.getStepZ() * extraOff);

        // Rotate to face outward from wall
        float yaw = switch (face) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f - yaw));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        mc.getItemRenderer().renderStatic(held, ItemDisplayContext.FIXED,
                15728880, OverlayTexture.NO_OVERLAY, poseStack, bufferSource,
                mc.level, 0);

        poseStack.popPose();
    }

    /**
     * Convert face-local UV (u,v) to block-local XYZ, offset slightly from the face surface.
     * u = relX (N/S) or relZ (W/E), v = relY. Both range [0, 1].
     * The point is offset outward from the face (towards the player) by FACE_OFFSET.
     */
    private static float[] facePoint(Direction face, float u, float v) {
        float ox = face.getStepX() * FACE_OFFSET;
        float oy = face.getStepY() * FACE_OFFSET;
        float oz = face.getStepZ() * FACE_OFFSET;

        return switch (face) {
            // NORTH face at z=0. Normal=(0,0,-1), offset pushes towards -Z (outward, towards player)
            case NORTH -> new float[] { u + ox, v + oy, 0f + oz };
            // SOUTH face at z=1. Normal=(0,0,+1), offset pushes towards +Z
            case SOUTH -> new float[] { u + ox, v + oy, 1f + oz };
            // WEST face at x=0. Normal=(-1,0,0), offset pushes towards -X
            case WEST  -> new float[] { 0f + ox, v + oy, u + oz };
            // EAST face at x=1. Normal=(+1,0,0), offset pushes towards +X
            case EAST  -> new float[] { 1f + ox, v + oy, u + oz };
            default -> new float[] { ox, oy, oz };
        };
    }

    private GlyphPlacementRenderer() {}
}
