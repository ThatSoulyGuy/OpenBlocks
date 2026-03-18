package com.openblocks.canvas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Per-dimension persistent storage for paint data on non-canvas blocks.
 * Stores a map of BlockPos -> PaintEntry, with a secondary chunk index
 * for efficient chunk-level queries during player sync.
 */
public class PaintSavedData extends SavedData {

    private static final String DATA_NAME = "openblocks_paint";

    private final Map<BlockPos, PaintEntry> paintMap = new HashMap<>();
    private final Map<ChunkPos, Set<BlockPos>> chunkIndex = new HashMap<>();

    public PaintSavedData() {
    }

    public static PaintSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(PaintSavedData::new, PaintSavedData::load, null),
                DATA_NAME
        );
    }

    public PaintEntry getOrCreate(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        return paintMap.computeIfAbsent(immutable, k -> {
            chunkIndex.computeIfAbsent(new ChunkPos(k), c -> new HashSet<>()).add(k);
            return new PaintEntry();
        });
    }

    @Nullable
    public PaintEntry get(BlockPos pos) {
        return paintMap.get(pos);
    }

    public void remove(BlockPos pos) {
        PaintEntry removed = paintMap.remove(pos);
        if (removed != null) {
            ChunkPos cp = new ChunkPos(pos);
            Set<BlockPos> set = chunkIndex.get(cp);
            if (set != null) {
                set.remove(pos);
                if (set.isEmpty()) chunkIndex.remove(cp);
            }
            setDirty();
        }
    }

    /**
     * Store or update a paint entry and mark dirty.
     */
    public void put(BlockPos pos, PaintEntry entry) {
        BlockPos immutable = pos.immutable();
        paintMap.put(immutable, entry);
        chunkIndex.computeIfAbsent(new ChunkPos(immutable), c -> new HashSet<>()).add(immutable);
        setDirty();
    }

    public Set<BlockPos> getPositionsInChunk(ChunkPos chunkPos) {
        Set<BlockPos> set = chunkIndex.get(chunkPos);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    public boolean isEmpty() {
        return paintMap.isEmpty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, PaintEntry> entry : paintMap.entrySet()) {
            PaintEntry paint = entry.getValue();
            if (paint.isEmpty()) continue;
            BlockPos pos = entry.getKey();
            CompoundTag entryTag = paint.save();
            entryTag.putInt("X", pos.getX());
            entryTag.putInt("Y", pos.getY());
            entryTag.putInt("Z", pos.getZ());
            list.add(entryTag);
        }
        tag.put("PaintEntries", list);
        return tag;
    }

    public static PaintSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PaintSavedData data = new PaintSavedData();
        ListTag list = tag.getList("PaintEntries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = new BlockPos(entryTag.getInt("X"), entryTag.getInt("Y"), entryTag.getInt("Z"));
            PaintEntry paint = PaintEntry.load(entryTag);
            if (!paint.isEmpty()) {
                data.paintMap.put(pos, paint);
                data.chunkIndex.computeIfAbsent(new ChunkPos(pos), c -> new HashSet<>()).add(pos);
            }
        }
        return data;
    }
}
