package com.openblocks.canvas;

import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A transparent variant of the canvas block.
 */
public class CanvasGlassBlock extends CanvasBlock {

    public CanvasGlassBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return OpenBlocksBlockEntities.CANVAS.get().create(pos, state);
    }
}
