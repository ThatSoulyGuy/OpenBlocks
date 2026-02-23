package com.openblocks.decoration;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.ColorMeta;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stores the flag's rotation angle and color.
 */
public class FlagBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Float> angle = syncedFloat("angle", 0.0f);
    private final SyncedValue<Integer> colorIndex = syncedInt("color", ColorMeta.LIME.ordinal());

    public FlagBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.FLAG.get(), pos, state);
    }

    public float getAngle() {
        return angle.get();
    }

    public void setAngle(float value) {
        angle.set(value);
        sync();
    }

    public ColorMeta getColor() {
        return ColorMeta.fromOrdinal(colorIndex.get());
    }

    public void setColor(DyeColor dye) {
        colorIndex.set(ColorMeta.fromDyeColor(dye).ordinal());
        sync();
    }
}
