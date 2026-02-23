package com.openblocks.utility;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the sky block. Minimal — exists primarily for
 * client-side rendering support (custom transparency effect).
 */
public class SkyBlockEntity extends OpenBlocksBlockEntity {

    public SkyBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.SKY.get(), pos, state);
    }
}
