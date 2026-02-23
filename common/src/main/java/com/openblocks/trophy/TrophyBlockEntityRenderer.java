package com.openblocks.trophy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders a scaled-down entity on top of the trophy pedestal.
 */
public class TrophyBlockEntityRenderer implements BlockEntityRenderer<TrophyBlockEntity> {

    private static final Map<TrophyType, Entity> ENTITY_CACHE = new HashMap<>();
    private final EntityRenderDispatcher entityRenderer;

    public TrophyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public void render(TrophyBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        TrophyType type = be.getTrophyType();
        if (type == null) return;

        Entity entity = getOrCreateEntity(type);
        if (entity == null) return;

        poseStack.pushPose();

        // Position on top of pedestal
        poseStack.translate(0.5, 0.2 + type.getVerticalOffset(), 0.5);

        // Face the direction the block is facing
        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        float rotation = switch (facing) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Scale down
        float scale = (float) type.getScale();
        poseStack.scale(scale, scale, scale);

        try {
            entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0f, partialTick,
                    poseStack, bufferSource, packedLight);
        } catch (Exception ignored) {
            // Some entities may fail to render without a proper level
        }

        poseStack.popPose();
    }

    private Entity getOrCreateEntity(TrophyType type) {
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
}
