package com.openblocks.entity.renderer;

import com.openblocks.entity.MiniMeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Renders the MiniMe entity using the player's skin.
 * Falls back to default skin if the owner is not known.
 */
public class MiniMeRenderer extends HumanoidMobRenderer<MiniMeEntity, HumanoidModel<MiniMeEntity>> {

    public MiniMeRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(MiniMeEntity entity) {
        UUID ownerUUID = entity.getOwnerUUID();
        if (ownerUUID != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(ownerUUID);
                if (info != null) {
                    return info.getSkin().texture();
                }
            }
            // Fallback: deterministic default skin based on owner UUID
            PlayerSkin skin = DefaultPlayerSkin.get(ownerUUID);
            return skin.texture();
        }
        return DefaultPlayerSkin.getDefaultTexture();
    }
}
