package com.openblocks.projector;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Block entity for the projector. Stores a map item and its rotation.
 * The map ID is synced to clients for rendering the hologram.
 */
public class ProjectorBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Integer> mapId = syncedInt("mapId", -1);
    private final SyncedValue<Integer> rotation = syncedInt("rotation", 0);
    private ItemStack storedMap = ItemStack.EMPTY;

    public ProjectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(OpenBlocksBlockEntities.PROJECTOR.get(), pos, blockState);
    }

    public boolean hasMap() {
        return !storedMap.isEmpty();
    }

    public int getMapId() {
        return mapId.get();
    }

    public int getRotation() {
        return rotation.get();
    }

    public void insertMap(ItemStack stack) {
        this.storedMap = stack.copyWithCount(1);
        this.mapId.set(extractMapId(storedMap));
        sync();
    }

    public ItemStack removeMap() {
        ItemStack removed = storedMap.copy();
        storedMap = ItemStack.EMPTY;
        mapId.set(-1);
        sync();
        return removed;
    }

    public void rotate() {
        int r = (rotation.get() + 1) & 3;
        rotation.set(r);
        sync();
    }

    /**
     * Gets the map data for rendering. Client-side only.
     */
    public MapItemSavedData getMapData() {
        int id = mapId.get();
        if (id < 0 || level == null) return null;
        return level.getMapData(new MapId(id));
    }

    private static int extractMapId(ItemStack stack) {
        MapId id = stack.get(DataComponents.MAP_ID);
        return id != null ? id.id() : -1;
    }

    // --- Persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!storedMap.isEmpty()) {
            tag.put("StoredMap", storedMap.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StoredMap")) {
            storedMap = ItemStack.parse(registries, tag.getCompound("StoredMap")).orElse(ItemStack.EMPTY);
            if (!storedMap.isEmpty()) {
                mapId.set(extractMapId(storedMap));
            }
        }
    }
}
