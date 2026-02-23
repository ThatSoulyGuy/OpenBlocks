package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Target block entity that stores the current signal strength from the last projectile hit.
 */
public class TargetBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Integer> signalStrength = syncedInt("signal", 0);

    public TargetBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.TARGET.get(), pos, state);
    }

    public int getSignalStrength() {
        return signalStrength.get();
    }

    public void setSignalStrength(int strength) {
        signalStrength.set(strength);
        sync();
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Signal: " + signalStrength.get());
        info.add("Powered: " + getBlockState().getValue(TargetBlock.POWERED));
        return info;
    }
}
