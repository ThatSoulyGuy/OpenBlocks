package com.openblocks.trophy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SnowGolemBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        Level level = tile.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos base = tile.getBlockPos();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = base.offset(x, 0, z);
                    if (level.getBlockState(pos).isAir()) {
                        int layers = level.random.nextInt(8) + 1;
                        BlockState snow = Blocks.SNOW.defaultBlockState()
                                .setValue(SnowLayerBlock.LAYERS, layers);
                        if (snow.canSurvive(level, pos)) {
                            level.setBlock(pos, snow, 3);
                        }
                    }
                }
            }
        }
        return 10;
    }
}
