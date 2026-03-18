package com.openblocks.canvas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.openblocks.imaginary.StencilPattern;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Renders paint overlays on non-canvas blocks using data from PaintClientCache.
 * Called from platform-specific render hooks (Fabric WorldRenderEvents / NeoForge RenderLevelStageEvent).
 * Shares stencil texture initialization with CanvasBlockEntityRenderer.
 */
public final class PaintOverlayRenderer {

    /** File-based white texture for solid color overlays. */
    static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("openblocks", "textures/block/canvas_white.png");

    private static Map<StencilPattern, ResourceLocation> stencilTextures;
    private static boolean stencilTexturesInitialized = false;

    private static final float OFFSET = 0.001f;

    /**
     * Normal used for all paint overlay vertices. (0, 1, 0) is chosen because
     * the entity shader's minecraft_mix_light function produces a shading factor
     * of 1.0 for this normal with MC's default light directions, ensuring paint
     * colors render without any directional darkening.
     */
    private static final float NEUTRAL_NX = 0, NEUTRAL_NY = 1, NEUTRAL_NZ = 0;
    private static final double MAX_RENDER_DIST_SQ = 64.0 * 64.0;

    /**
     * Initialize stencil pattern DynamicTextures. Called lazily on first render.
     * Both CanvasBlockEntityRenderer and this class use these textures.
     */
    public static synchronized void ensureStencilTextures() {
        if (stencilTexturesInitialized) return;
        stencilTexturesInitialized = true;

        var tm = Minecraft.getInstance().getTextureManager();
        stencilTextures = new EnumMap<>(StencilPattern.class);
        for (StencilPattern pattern : StencilPattern.values()) {
            NativeImage image = new NativeImage(16, 16, false);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    image.setPixelRGBA(x, y, pattern.isSet(x, y) ? 0xFFFFFFFF : 0x00000000);
                }
            }
            DynamicTexture dynTex = new DynamicTexture(image);
            ResourceLocation loc = tm.register(
                    "openblocks_stencil_" + pattern.getSerializedName(), dynTex);
            stencilTextures.put(pattern, loc);
        }
    }

    public static ResourceLocation getStencilTexture(StencilPattern pattern) {
        ensureStencilTextures();
        return stencilTextures.getOrDefault(pattern, WHITE_TEXTURE);
    }

    /**
     * Render all paint overlays for blocks in PaintClientCache.
     * Called from platform-specific world render hooks.
     */
    public static void renderAllPaint(PoseStack poseStack, Camera camera,
                                       MultiBufferSource.BufferSource bufferSource) {
        if (PaintClientCache.isEmpty()) return;
        ensureStencilTextures();

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Vec3 camPos = camera.getPosition();

        for (Map.Entry<BlockPos, PaintEntry> e : PaintClientCache.getAll()) {
            BlockPos pos = e.getKey();
            PaintEntry entry = e.getValue();

            double dx = pos.getX() + 0.5 - camPos.x;
            double dy = pos.getY() + 0.5 - camPos.y;
            double dz = pos.getZ() + 0.5 - camPos.z;
            if (dx * dx + dy * dy + dz * dz > MAX_RENDER_DIST_SQ) continue;

            BlockState state = level.getBlockState(pos);
            Vec3 modelOffset = state.getOffset(level, pos);

            poseStack.pushPose();
            poseStack.translate(
                    pos.getX() - camPos.x + modelOffset.x,
                    pos.getY() - camPos.y + modelOffset.y,
                    pos.getZ() - camPos.z + modelOffset.z
            );
            renderEntry(entry, poseStack, bufferSource, state, level, pos);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    private static void renderEntry(PaintEntry entry, PoseStack poseStack,
                                      MultiBufferSource bufferSource,
                                      BlockState state, Level level, BlockPos pos) {
        boolean isFullCube = state.isCollisionShapeFullBlock(level, pos);

        if (isFullCube) {
            // Full-cube blocks: render full-face quads (supports stencils)
            for (Direction face : Direction.values()) {
                int color = entry.getColor(face);
                StencilPattern pattern = entry.getStencil(face);
                boolean hasCover = entry.hasStencilCover(face);

                if (color != 0 || hasCover) {
                    renderFaceOverlay(face, color, pattern, hasCover, poseStack, bufferSource);
                }
            }
        } else {
            // Irregular blocks: render paint conforming to model geometry
            renderModelOverlay(entry, state, poseStack, bufferSource);
        }
    }

    /**
     * Render paint overlay conforming to the actual BakedModel geometry.
     * Used for irregular blocks (stairs, slabs, fences, etc.).
     */
    private static void renderModelOverlay(PaintEntry entry, BlockState state,
                                             PoseStack poseStack, MultiBufferSource bufferSource) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        RandomSource random = RandomSource.create(42L);
        int vertexSize = DefaultVertexFormat.BLOCK.getVertexSize() / 4;

        poseStack.pushPose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(WHITE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // Process culled quads (per face direction)
        for (Direction face : Direction.values()) {
            int color = entry.getColor(face);
            if (color == 0) continue;

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(state, face, random);
            for (BakedQuad quad : quads) {
                renderBakedQuadOverlay(quad, matrix, consumer, r, g, b, face, vertexSize);
            }
        }

        // Process unculled quads (face = null, e.g. stair step inner faces)
        random.setSeed(42L);
        List<BakedQuad> unculledQuads = model.getQuads(state, null, random);
        for (BakedQuad quad : unculledQuads) {
            Direction quadDir = quad.getDirection();
            int color = entry.getColor(quadDir);
            if (color == 0) continue;

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            renderBakedQuadOverlay(quad, matrix, consumer, r, g, b, quadDir, vertexSize);
        }

        poseStack.popPose();
    }

    /**
     * Render a single BakedQuad with paint color overlay, offset slightly along the face normal.
     */
    private static void renderBakedQuadOverlay(BakedQuad quad, Matrix4f matrix,
                                                 VertexConsumer consumer,
                                                 float r, float g, float b,
                                                 Direction face, int vertexSize) {
        int[] vertices = quad.getVertices();
        float ox = face.getStepX() * OFFSET;
        float oy = face.getStepY() * OFFSET;
        float oz = face.getStepZ() * OFFSET;

        for (int v = 0; v < 4; v++) {
            int base = v * vertexSize;
            float x = Float.intBitsToFloat(vertices[base]) + ox;
            float y = Float.intBitsToFloat(vertices[base + 1]) + oy;
            float z = Float.intBitsToFloat(vertices[base + 2]) + oz;

            consumer.addVertex(matrix, x, y, z)
                    .setColor(r, g, b, 1.0f)
                    .setUv(0, 0)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
        }
    }

    static void renderFaceOverlay(Direction face, int color, StencilPattern pattern,
                                    boolean hasCover, PoseStack poseStack,
                                    MultiBufferSource bufferSource) {
        ResourceLocation texture;
        if (pattern != null) {
            texture = stencilTextures.getOrDefault(pattern, WHITE_TEXTURE);
        } else {
            texture = WHITE_TEXTURE;
        }

        float r, g, b, a;
        if (hasCover && color == 0) {
            r = 0.85f; g = 0.85f; b = 0.85f; a = 1.0f;
        } else {
            r = ((color >> 16) & 0xFF) / 255.0f;
            g = ((color >> 8) & 0xFF) / 255.0f;
            b = (color & 0xFF) / 255.0f;
            a = 1.0f;
        }

        poseStack.pushPose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        Matrix4f matrix = poseStack.last().pose();
        renderFaceQuad(face, matrix, consumer, r, g, b, a, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderFaceQuad(Direction face, Matrix4f matrix, VertexConsumer consumer,
                                         float r, float g, float b, float a,
                                         int packedLight, int packedOverlay) {
        switch (face) {
            case DOWN -> {
                float y = -OFFSET;
                consumer.addVertex(matrix, 0, y, 0).setColor(r, g, b, a).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 0, y, 1).setColor(r, g, b, a).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, y, 1).setColor(r, g, b, a).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, y, 0).setColor(r, g, b, a).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
            }
            case UP -> {
                float y = 1 + OFFSET;
                consumer.addVertex(matrix, 0, y, 1).setColor(r, g, b, a).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 0, y, 0).setColor(r, g, b, a).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, y, 0).setColor(r, g, b, a).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, y, 1).setColor(r, g, b, a).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
            }
            case NORTH -> {
                float z = -OFFSET;
                consumer.addVertex(matrix, 1, 1, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, 0, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 0, 0, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 0, 1, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
            }
            case SOUTH -> {
                float z = 1 + OFFSET;
                consumer.addVertex(matrix, 0, 1, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 0, 0, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, 0, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, 1, 1, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
            }
            case WEST -> {
                float x = -OFFSET;
                consumer.addVertex(matrix, x, 1, 0).setColor(r, g, b, a).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, x, 0, 0).setColor(r, g, b, a).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, x, 0, 1).setColor(r, g, b, a).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, x, 1, 1).setColor(r, g, b, a).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
            }
            case EAST -> {
                float x = 1 + OFFSET;
                consumer.addVertex(matrix, x, 1, 1).setColor(r, g, b, a).setUv(0, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, x, 0, 1).setColor(r, g, b, a).setUv(0, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, x, 0, 0).setColor(r, g, b, a).setUv(1, 1).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
                consumer.addVertex(matrix, x, 1, 0).setColor(r, g, b, a).setUv(1, 0).setOverlay(packedOverlay).setLight(packedLight).setNormal(NEUTRAL_NX, NEUTRAL_NY, NEUTRAL_NZ);
            }
        }
    }

    private PaintOverlayRenderer() {}
}
