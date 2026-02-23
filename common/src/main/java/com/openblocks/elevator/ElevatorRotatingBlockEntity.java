package com.openblocks.elevator;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the rotating elevator. Stores the elevator color
 * (since using blockstate for 16 colors * 4 directions = 64 states is excessive).
 */
public class ElevatorRotatingBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Integer> colorOrdinal = syncedInt("color", DyeColor.WHITE.getId());

    public ElevatorRotatingBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.ELEVATOR_ROTATING.get(), pos, state);
    }

    public DyeColor getColor() {
        return DyeColor.byId(colorOrdinal.get());
    }

    public void setColor(DyeColor color) {
        colorOrdinal.set(color.getId());
        sync();
    }
}
