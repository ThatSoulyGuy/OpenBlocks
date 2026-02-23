package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Village highlighter block entity. Periodically scans for villager POIs
 * (beds, workstations, meeting points) and outputs a redstone signal
 * proportional to the count found.
 */
public class VillageHighlighterBlockEntity extends OpenBlocksBlockEntity {

    private static final int SCAN_INTERVAL = 100; // ticks (5 seconds)
    private static final int SCAN_RADIUS = 64;

    private final SyncedValue<Integer> signalStrength = syncedInt("signal", 0);
    private int tickCounter = 0;

    public VillageHighlighterBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.VILLAGE_HIGHLIGHTER.get(), pos, state);
    }

    public int getSignalStrength() {
        return signalStrength.get();
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        tickCounter++;
        if (tickCounter % SCAN_INTERVAL != 0) return;

        if (level instanceof ServerLevel serverLevel) {
            PoiManager poiManager = serverLevel.getPoiManager();

            // Count nearby POIs related to villages
            long poiCount = poiManager.getCountInRange(
                    holder -> true, // Count all POI types
                    worldPosition,
                    SCAN_RADIUS,
                    PoiManager.Occupancy.ANY
            );

            // Map count to 0-15 signal (logarithmic scale)
            int newSignal;
            if (poiCount <= 0) {
                newSignal = 0;
            } else if (poiCount >= 100) {
                newSignal = 15;
            } else {
                newSignal = (int) Math.max(1, Math.round(15.0 * poiCount / 100.0));
            }

            if (newSignal != signalStrength.get()) {
                signalStrength.set(newSignal);
                sync();
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Signal: " + signalStrength.get());
        return info;
    }
}
