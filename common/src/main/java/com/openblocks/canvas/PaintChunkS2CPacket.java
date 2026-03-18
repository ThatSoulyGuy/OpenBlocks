package com.openblocks.canvas;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-to-client packet for bulk paint data in a chunk.
 * Sent when a player starts tracking a chunk that contains painted blocks.
 */
public record PaintChunkS2CPacket(int chunkX, int chunkZ, List<BlockPaintData> entries)
        implements CustomPacketPayload {

    public record BlockPaintData(BlockPos pos, int[] faceColors, int[] faceStencils, byte coverBits) {
        public static BlockPaintData from(BlockPos pos, PaintEntry entry) {
            return new BlockPaintData(
                    pos, entry.getFaceColors().clone(), entry.getFaceStencils().clone(), entry.getCoverBits());
        }
    }

    public static final Type<PaintChunkS2CPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("paint_chunk"));

    public static final StreamCodec<FriendlyByteBuf, PaintChunkS2CPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public PaintChunkS2CPacket decode(FriendlyByteBuf buf) {
                    int cx = buf.readInt();
                    int cz = buf.readInt();
                    int count = buf.readVarInt();
                    List<BlockPaintData> entries = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        BlockPos pos = buf.readBlockPos();
                        int[] colors = new int[6];
                        int[] stencils = new int[6];
                        for (int j = 0; j < 6; j++) colors[j] = buf.readInt();
                        for (int j = 0; j < 6; j++) stencils[j] = buf.readInt();
                        byte covers = buf.readByte();
                        entries.add(new BlockPaintData(pos, colors, stencils, covers));
                    }
                    return new PaintChunkS2CPacket(cx, cz, entries);
                }

                @Override
                public void encode(FriendlyByteBuf buf, PaintChunkS2CPacket packet) {
                    buf.writeInt(packet.chunkX);
                    buf.writeInt(packet.chunkZ);
                    buf.writeVarInt(packet.entries.size());
                    for (BlockPaintData entry : packet.entries) {
                        buf.writeBlockPos(entry.pos);
                        for (int j = 0; j < 6; j++) buf.writeInt(entry.faceColors[j]);
                        for (int j = 0; j < 6; j++) buf.writeInt(entry.faceStencils[j]);
                        buf.writeByte(entry.coverBits);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
