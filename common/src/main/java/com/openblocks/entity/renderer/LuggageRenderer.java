package com.openblocks.entity.renderer;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.entity.LuggageEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Luggage entity using the original 1.12.2 model.
 * Texture: textures/models/luggage.png (128x64)
 */
public class LuggageRenderer extends MobRenderer<LuggageEntity, LuggageModel> {

    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "luggage"), "main");

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "textures/models/luggage.png");

    public LuggageRenderer(EntityRendererProvider.Context context) {
        super(context, new LuggageModel(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(LuggageEntity entity) {
        return TEXTURE;
    }
}
