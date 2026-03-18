package com.openblocks.canvas;

import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of paint data received from the server.
 * Thread-safe: packets arrive on the network thread, rendering reads on the render thread.
 */
public final class PaintClientCache {

    private static final ConcurrentHashMap<BlockPos, PaintEntry> cache = new ConcurrentHashMap<>();

    public static void put(BlockPos pos, PaintEntry entry) {
        cache.put(pos.immutable(), entry);
    }

    public static PaintEntry get(BlockPos pos) {
        return cache.get(pos);
    }

    public static void remove(BlockPos pos) {
        cache.remove(pos);
    }

    public static void clearAll() {
        cache.clear();
    }

    public static Collection<Map.Entry<BlockPos, PaintEntry>> getAll() {
        return cache.entrySet();
    }

    public static boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * Handle a single block paint update from the server.
     */
    public static void handleUpdate(PaintUpdateS2CPacket packet) {
        PaintEntry entry = PaintEntry.fromPacketData(packet.faceColors(), packet.faceStencils(), packet.coverBits());
        if (entry.isEmpty()) {
            cache.remove(packet.pos());
        } else {
            cache.put(packet.pos().immutable(), entry);
        }
    }

    /**
     * Handle bulk chunk paint data from the server.
     */
    public static void handleChunkData(PaintChunkS2CPacket packet) {
        for (PaintChunkS2CPacket.BlockPaintData data : packet.entries()) {
            PaintEntry entry = PaintEntry.fromPacketData(data.faceColors(), data.faceStencils(), data.coverBits());
            if (!entry.isEmpty()) {
                cache.put(data.pos().immutable(), entry);
            }
        }
    }

    private PaintClientCache() {}
}
