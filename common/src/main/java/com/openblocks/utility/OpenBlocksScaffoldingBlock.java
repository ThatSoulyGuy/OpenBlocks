package com.openblocks.utility;

import com.openblocks.core.base.OpenBlocksBlock;
import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Temporary construction block that randomly despawns over time.
 * Very low hardness, making it easy to break. The despawn rate is configurable.
 */
public class OpenBlocksScaffoldingBlock extends OpenBlocksBlock {

    public OpenBlocksScaffoldingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int despawnRate = OpenBlocksConfig.Scaffolding.despawnRate;
        if (despawnRate <= 0 || random.nextInt(despawnRate) == 0) {
            level.destroyBlock(pos, true);
        }
    }
}
