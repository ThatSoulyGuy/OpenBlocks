package com.openblocks.guide;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for guide block actions (dimension changes, shape cycling).
 */
public record GuideActionPacket(BlockPos pos, String action) implements CustomPacketPayload {

    public static final Type<GuideActionPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("guide_action"));

    public static final StreamCodec<FriendlyByteBuf, GuideActionPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public GuideActionPacket decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    String action = buf.readUtf(64);
                    return new GuideActionPacket(pos, action);
                }

                @Override
                public void encode(FriendlyByteBuf buf, GuideActionPacket packet) {
                    buf.writeBlockPos(packet.pos);
                    buf.writeUtf(packet.action, 64);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
