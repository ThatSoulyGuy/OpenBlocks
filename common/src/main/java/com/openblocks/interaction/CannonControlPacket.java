package com.openblocks.interaction;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for adjusting cannon pitch and speed.
 */
public record CannonControlPacket(BlockPos pos, float pitch, float speed) implements CustomPacketPayload {

    public static final Type<CannonControlPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("cannon_control"));

    public static final StreamCodec<FriendlyByteBuf, CannonControlPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CannonControlPacket decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    float pitch = buf.readFloat();
                    float speed = buf.readFloat();
                    return new CannonControlPacket(pos, pitch, speed);
                }

                @Override
                public void encode(FriendlyByteBuf buf, CannonControlPacket packet) {
                    buf.writeBlockPos(packet.pos);
                    buf.writeFloat(packet.pitch);
                    buf.writeFloat(packet.speed);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
