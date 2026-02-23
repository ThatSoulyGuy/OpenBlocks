package com.openblocks.imaginary;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for drawing table actions (pattern cycling).
 * Action: 0 = next pattern, 1 = previous pattern.
 */
public record DrawingTableActionPacket(BlockPos pos, int action) implements CustomPacketPayload {

    public static final int ACTION_NEXT = 0;
    public static final int ACTION_PREV = 1;

    public static final Type<DrawingTableActionPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("drawing_table_action"));

    public static final StreamCodec<FriendlyByteBuf, DrawingTableActionPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public DrawingTableActionPacket decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    int action = buf.readVarInt();
                    return new DrawingTableActionPacket(pos, action);
                }

                @Override
                public void encode(FriendlyByteBuf buf, DrawingTableActionPacket packet) {
                    buf.writeBlockPos(packet.pos);
                    buf.writeVarInt(packet.action);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
