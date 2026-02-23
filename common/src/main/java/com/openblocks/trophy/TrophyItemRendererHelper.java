package com.openblocks.trophy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared rendering logic for trophy items. Used by platform-specific BEWLR implementations.
 * Renders the pedestal block model + scaled entity on top.
 */
public final class TrophyItemRendererHelper {

    private static final Map<TrophyType, Entity> ENTITY_CACHE = new HashMap<>();

    public static void renderTrophyItem(ItemStack stack, ItemDisplayContext displayContext,
                                         PoseStack poseStack, MultiBufferSource bufferSource,
                                         int packedLight, int packedOverlay) {
        TrophyType type = TrophyBlockItem.getTrophyType(stack);

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        // Render the pedestal block model
        poseStack.pushPose();
        BlockState trophyState = com.openblocks.core.registry.OpenBlocksBlocks.TROPHY.get()
                .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        blockRenderer.renderSingleBlock(trophyState, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();

        // Render the entity on top if we have a trophy type
        if (type != null) {
            Entity entity = getOrCreateEntity(type);
            if (entity != null) {
                EntityRenderDispatcher entityRenderer = mc.getEntityRenderDispatcher();

                poseStack.pushPose();
                poseStack.translate(0.5, 0.2 + type.getVerticalOffset(), 0.5);

                float scale = (float) type.getScale();
                poseStack.scale(scale, scale, scale);

                try {
                    entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0f, 0.0f,
                            poseStack, bufferSource, packedLight);
                } catch (Exception ignored) {
                    // Some entities may fail to render without a proper level
                }

                poseStack.popPose();
            }
        }
    }

    private static Entity getOrCreateEntity(TrophyType type) {
        return ENTITY_CACHE.computeIfAbsent(type, t -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return null;
                EntityType<?> entityType = t.getEntityType();
                return entityType.create(mc.level);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private TrophyItemRendererHelper() {}
}
