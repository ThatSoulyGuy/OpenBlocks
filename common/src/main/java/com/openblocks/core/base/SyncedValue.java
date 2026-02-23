package com.openblocks.core.base;

import net.minecraft.nbt.CompoundTag;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A synchronized value that replaces OpenModsLib's SyncableXxx types.
 * Values are saved to NBT for persistence and network sync via
 * BlockEntity's getUpdateTag/handleUpdateTag mechanism.
 */
public class SyncedValue<T> {

    private final String key;
    private T value;
    private boolean dirty;
    private final BiConsumer<CompoundTag, T> writer;
    private final Function<CompoundTag, T> reader;
    private Runnable onChanged;

    public SyncedValue(String key, T initialValue,
                       BiConsumer<CompoundTag, T> writer,
                       Function<CompoundTag, T> reader) {
        this.key = key;
        this.value = initialValue;
        this.writer = writer;
        this.reader = reader;
        this.dirty = false;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (newValue == null ? value != null : !newValue.equals(value)) {
            this.value = newValue;
            this.dirty = true;
            if (onChanged != null) {
                onChanged.run();
            }
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public String getKey() {
        return key;
    }

    public void save(CompoundTag tag) {
        if (value != null) {
            CompoundTag nested = new CompoundTag();
            writer.accept(nested, value);
            tag.put(key, nested);
        }
    }

    public void load(CompoundTag tag) {
        if (tag.contains(key)) {
            CompoundTag nested = tag.getCompound(key);
            this.value = reader.apply(nested);
        }
    }

    public void setOnChanged(Runnable callback) {
        this.onChanged = callback;
    }
}
