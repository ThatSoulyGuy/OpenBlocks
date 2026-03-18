package com.openblocks.canvas;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server-to-client packet for a single block's paint update.
 * If all data is empty (colors=0, stencils=-1, covers=0), the client removes the entry.
 */
public record PaintUpdateS2CPacket(BlockPos pos, int[] faceColors, int[] faceStencils, byte coverBits)
        implements CustomPacketPayload {

    public static final Type<PaintUpdateS2CPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("paint_update"));

    public static final StreamCodec<FriendlyByteBuf, PaintUpdateS2CPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public PaintUpdateS2CPacket decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    int[] colors = new int[6];
                    int[] stencils = new int[6];
                    for (int i = 0; i < 6; i++) colors[i] = buf.readInt();
                    for (int i = 0; i < 6; i++) stencils[i] = buf.readInt();
                    byte covers = buf.readByte();
                    return new PaintUpdateS2CPacket(pos, colors, stencils, covers);
                }

                @Override
                public void encode(FriendlyByteBuf buf, PaintUpdateS2CPacket packet) {
                    buf.writeBlockPos(packet.pos);
                    for (int i = 0; i < 6; i++) buf.writeInt(packet.faceColors[i]);
                    for (int i = 0; i < 6; i++) buf.writeInt(packet.faceStencils[i]);
                    buf.writeByte(packet.coverBits);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PaintUpdateS2CPacket fromEntry(BlockPos pos, PaintEntry entry) {
        return new PaintUpdateS2CPacket(
                pos, entry.getFaceColors().clone(), entry.getFaceStencils().clone(), entry.getCoverBits());
    }

    public static PaintUpdateS2CPacket empty(BlockPos pos) {
        return new PaintUpdateS2CPacket(
                pos, new int[6], new int[]{-1, -1, -1, -1, -1, -1}, (byte) 0);
    }
}
