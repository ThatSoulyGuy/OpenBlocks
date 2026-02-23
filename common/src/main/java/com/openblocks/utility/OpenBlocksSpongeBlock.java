package com.openblocks.utility;

import com.openblocks.core.base.OpenBlocksBlock;
import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Absorbs water and lava in a configurable radius around itself.
 * If lava is absorbed, the sponge catches fire and is destroyed.
 * Triggers on placement, neighbor updates, and random ticks.
 */
public class OpenBlocksSpongeBlock extends OpenBlocksBlock {

    public OpenBlocksSpongeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        absorbLiquids(level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            absorbLiquids(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                    BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            absorbLiquids(level, pos);
        }
    }

    private void absorbLiquids(Level level, BlockPos center) {
        int range = OpenBlocksConfig.Sponge.blockRange;
        boolean foundLava = false;
        int flags = OpenBlocksConfig.Sponge.blockUpdate ? 3 : 2;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos target = center.offset(x, y, z);
                    if (target.equals(center)) continue;

                    FluidState fluid = level.getFluidState(target);
                    if (!fluid.isEmpty()) {
                        if (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA) {
                            foundLava = true;
                        }
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), flags);
                    }
                }
            }
        }

        if (foundLava) {
            level.setBlock(center, Blocks.FIRE.defaultBlockState(), 3);
        }
    }
}
