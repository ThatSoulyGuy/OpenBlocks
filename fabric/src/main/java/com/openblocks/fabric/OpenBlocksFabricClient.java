package com.openblocks.fabric;

import com.openblocks.OpenBlocksClient;
import com.openblocks.core.registry.OpenBlocksItems;
import com.openblocks.trophy.TrophyItemRendererHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public final class OpenBlocksFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OpenBlocksClient.init();

        // Trophy item: render pedestal + entity via BEWLR
        BuiltinItemRendererRegistry.INSTANCE.register(OpenBlocksItems.TROPHY.get(),
                (stack, mode, matrices, vertexConsumers, light, overlay) ->
                        TrophyItemRendererHelper.renderTrophyItem(stack, mode, matrices, vertexConsumers, light, overlay));
    }
}
