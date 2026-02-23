package com.openblocks.canvas;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for painting a canvas block face.
 */
public record CanvasPaintPacket(BlockPos pos, int face, int color) implements CustomPacketPayload {

    public static final Type<CanvasPaintPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("canvas_paint"));

    public static final StreamCodec<FriendlyByteBuf, CanvasPaintPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CanvasPaintPacket decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    int face = buf.readVarInt();
                    int color = buf.readInt();
                    return new CanvasPaintPacket(pos, face, color);
                }

                @Override
                public void encode(FriendlyByteBuf buf, CanvasPaintPacket packet) {
                    buf.writeBlockPos(packet.pos);
                    buf.writeVarInt(packet.face);
                    buf.writeInt(packet.color);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
