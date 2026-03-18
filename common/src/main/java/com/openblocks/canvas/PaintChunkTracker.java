package com.openblocks.canvas;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

/**
 * Tracks which chunks each player has received paint data for.
 * On each server tick, checks if the player's view distance includes new chunks
 * and sends paint data for them.
 */
public final class PaintChunkTracker {

    private static final Map<UUID, Set<ChunkPos>> sentChunks = new HashMap<>();
    private static int tickCounter = 0;

    public static void onPlayerTick(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        // Throttle to every 10 ticks
        tickCounter++;
        if (tickCounter % 10 != 0) return;

        var serverLevel = sp.serverLevel();
        PaintSavedData data = PaintSavedData.get(serverLevel);
        if (data.isEmpty()) return;

        Set<ChunkPos> sent = sentChunks.computeIfAbsent(sp.getUUID(), k -> new HashSet<>());
        ChunkPos playerChunk = sp.chunkPosition();
        int viewDist = sp.server.getPlayerList().getViewDistance();

        for (int dx = -viewDist; dx <= viewDist; dx++) {
            for (int dz = -viewDist; dz <= viewDist; dz++) {
                ChunkPos cp = new ChunkPos(playerChunk.x + dx, playerChunk.z + dz);
                if (!sent.contains(cp)) {
                    sent.add(cp);
                    sendChunkPaint(sp, data, cp);
                }
            }
        }
    }

    public static void onPlayerQuit(ServerPlayer player) {
        sentChunks.remove(player.getUUID());
    }

    /**
     * Force resend of a specific chunk to all nearby players.
     * Called when block break removes paint data.
     */
    public static void invalidateChunk(ChunkPos cp) {
        for (Set<ChunkPos> sent : sentChunks.values()) {
            sent.remove(cp);
        }
    }

    private static void sendChunkPaint(ServerPlayer player, PaintSavedData data, ChunkPos cp) {
        Set<BlockPos> positions = data.getPositionsInChunk(cp);
        if (positions.isEmpty()) return;

        List<PaintChunkS2CPacket.BlockPaintData> entries = new ArrayList<>();
        for (BlockPos pos : positions) {
            PaintEntry entry = data.get(pos);
            if (entry != null && !entry.isEmpty()) {
                entries.add(PaintChunkS2CPacket.BlockPaintData.from(pos, entry));
            }
        }
        if (!entries.isEmpty()) {
            NetworkManager.sendToPlayer(player, new PaintChunkS2CPacket(cp.x, cp.z, entries));
        }
    }

    private PaintChunkTracker() {}
}
