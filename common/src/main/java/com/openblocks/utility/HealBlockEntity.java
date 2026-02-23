package com.openblocks.utility;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the healer block. Exists to enable the server-side ticker.
 */
public class HealBlockEntity extends OpenBlocksBlockEntity {

    public HealBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.HEAL.get(), pos, state);
    }
}
