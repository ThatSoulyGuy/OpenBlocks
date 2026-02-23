package com.openblocks.core.base;

import com.openblocks.core.debug.IDebuggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base BlockEntity class replacing OpenModsLib's SyncedTileEntity.
 * Provides automatic data sync via vanilla's update tag/packet mechanism
 * and a SyncedValue system for individual field synchronization.
 */
public abstract class OpenBlocksBlockEntity extends BlockEntity implements IDebuggable {

    private final List<SyncedValue<?>> syncedValues = new ArrayList<>();

    public OpenBlocksBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --- SyncedValue Factory Methods ---

    protected SyncedValue<Boolean> syncedBoolean(String key, boolean initial) {
        SyncedValue<Boolean> sv = new SyncedValue<>(key, initial,
                (tag, val) -> tag.putBoolean("v", val),
                tag -> tag.getBoolean("v"));
        syncedValues.add(sv);
        return sv;
    }

    protected SyncedValue<Integer> syncedInt(String key, int initial) {
        SyncedValue<Integer> sv = new SyncedValue<>(key, initial,
                (tag, val) -> tag.putInt("v", val),
                tag -> tag.getInt("v"));
        syncedValues.add(sv);
        return sv;
    }

    protected SyncedValue<Float> syncedFloat(String key, float initial) {
        SyncedValue<Float> sv = new SyncedValue<>(key, initial,
                (tag, val) -> tag.putFloat("v", val),
                tag -> tag.getFloat("v"));
        syncedValues.add(sv);
        return sv;
    }

    protected SyncedValue<Double> syncedDouble(String key, double initial) {
        SyncedValue<Double> sv = new SyncedValue<>(key, initial,
                (tag, val) -> tag.putDouble("v", val),
                tag -> tag.getDouble("v"));
        syncedValues.add(sv);
        return sv;
    }

    protected SyncedValue<String> syncedString(String key, String initial) {
        SyncedValue<String> sv = new SyncedValue<>(key, initial,
                (tag, val) -> tag.putString("v", val),
                tag -> tag.getString("v"));
        syncedValues.add(sv);
        return sv;
    }

    protected <E extends Enum<E>> SyncedValue<E> syncedEnum(String key, E initial) {
        Class<E> enumClass = initial.getDeclaringClass();
        SyncedValue<E> sv = new SyncedValue<>(key, initial,
                (tag, val) -> tag.putInt("v", val.ordinal()),
                tag -> {
                    int ordinal = tag.getInt("v");
                    E[] constants = enumClass.getEnumConstants();
                    return ordinal >= 0 && ordinal < constants.length ? constants[ordinal] : initial;
                });
        syncedValues.add(sv);
        return sv;
    }

    /**
     * Registers a custom SyncedValue. Use when the factory methods don't cover your type.
     */
    protected <T> SyncedValue<T> synced(String key, T initial,
                                         java.util.function.BiConsumer<CompoundTag, T> writer,
                                         java.util.function.Function<CompoundTag, T> reader) {
        SyncedValue<T> sv = new SyncedValue<>(key, initial, writer, reader);
        syncedValues.add(sv);
        return sv;
    }

    // --- NBT Persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag syncData = new CompoundTag();
        for (SyncedValue<?> sv : syncedValues) {
            sv.save(syncData);
        }
        tag.put("ob_sync", syncData);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ob_sync")) {
            CompoundTag syncData = tag.getCompound("ob_sync");
            for (SyncedValue<?> sv : syncedValues) {
                sv.load(syncData);
            }
        }
    }

    // --- Network Sync ---

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Call this when synced data changes to notify clients.
     */
    public void sync() {
        if (level != null && !level.isClientSide()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // --- Debug ---

    @Override
    public List<String> getDebugInfo() {
        List<String> info = new ArrayList<>();
        info.add("BlockEntity: " + getType().toString());
        info.add("Pos: " + worldPosition.toShortString());
        for (SyncedValue<?> sv : syncedValues) {
            info.add(sv.getKey() + ": " + sv.get());
        }
        return info;
    }

    protected List<SyncedValue<?>> getSyncedValues() {
        return syncedValues;
    }
}
