package com.openblocks.core.base;

import net.minecraft.world.level.block.Block;

/**
 * Base block class replacing OpenModsLib's OpenBlock.
 * Provides common functionality shared by all OpenBlocks blocks.
 * For blocks with BlockEntities, use {@link OpenBlocksEntityBlock} instead.
 */
public abstract class OpenBlocksBlock extends Block {

    public OpenBlocksBlock(Properties properties) {
        super(properties);
    }
}
