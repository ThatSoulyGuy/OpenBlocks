package com.openblocks.guide;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Block entity for the guide block. Stores shape dimensions and generates
 * ghost block positions for rendering.
 */
public class GuideBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<Integer> posX = syncedInt("posX", 8);
    private final SyncedValue<Integer> posY = syncedInt("posY", 8);
    private final SyncedValue<Integer> posZ = syncedInt("posZ", 8);
    private final SyncedValue<Integer> negX = syncedInt("negX", 8);
    private final SyncedValue<Integer> negY = syncedInt("negY", 8);
    private final SyncedValue<Integer> negZ = syncedInt("negZ", 8);
    private final SyncedValue<GuideShape> shape = syncedEnum("shape", GuideShape.SPHERE);
    private final SyncedValue<Integer> color = syncedInt("color", 0xFFFFFF);
    private final SyncedValue<Boolean> active = syncedBoolean("active", true);

    private List<BlockPos> cachedCoords;
    private boolean dirty = true;

    public GuideBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.GUIDE.get(), pos, state);
    }

    public GuideShape getShape() {
        return shape.get();
    }

    public int getColor() {
        return color.get();
    }

    public boolean isActive() {
        return active.get();
    }

    public void handleAction(String action) {
        switch (action) {
            case "inc_posX" -> posX.set(Math.min(posX.get() + 1, 64));
            case "dec_posX" -> posX.set(Math.max(posX.get() - 1, 0));
            case "inc_posY" -> posY.set(Math.min(posY.get() + 1, 64));
            case "dec_posY" -> posY.set(Math.max(posY.get() - 1, 0));
            case "inc_posZ" -> posZ.set(Math.min(posZ.get() + 1, 64));
            case "dec_posZ" -> posZ.set(Math.max(posZ.get() - 1, 0));
            case "inc_negX" -> negX.set(Math.min(negX.get() + 1, 64));
            case "dec_negX" -> negX.set(Math.max(negX.get() - 1, 0));
            case "inc_negY" -> negY.set(Math.min(negY.get() + 1, 64));
            case "dec_negY" -> negY.set(Math.max(negY.get() - 1, 0));
            case "inc_negZ" -> negZ.set(Math.min(negZ.get() + 1, 64));
            case "dec_negZ" -> negZ.set(Math.max(negZ.get() - 1, 0));
            case "inc_shape" -> shape.set(shape.get().next());
            case "dec_shape" -> shape.set(shape.get().prev());
            default -> { return; }
        }
        dirty = true;
        sync();
    }

    public void setColor(int newColor) {
        color.set(newColor);
        sync();
    }

    public List<BlockPos> getShapeCoords() {
        if (dirty || cachedCoords == null) {
            regenerateShape();
            dirty = false;
        }
        return cachedCoords;
    }

    private void regenerateShape() {
        Set<BlockPos> coords = ShapeGenerators.generate(
                shape.get(),
                negX.get(), negY.get(), negZ.get(),
                posX.get(), posY.get(), posZ.get()
        );
        cachedCoords = List.copyOf(coords);
    }

    public boolean shouldRender() {
        if (!active.get()) return false;
        int sensitivity = OpenBlocksConfig.Guide.redstoneSensitivity;
        if (sensitivity == 0) return true;
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        return sensitivity > 0 ? powered : !powered;
    }

    public void onNeighborChanged() {
        // Redstone change may affect rendering
        sync();
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Shape: " + shape.get().getSerializedName());
        info.add("Dims: -(" + negX.get() + "," + negY.get() + "," + negZ.get()
                + ") +(" + posX.get() + "," + posY.get() + "," + posZ.get() + ")");
        int size = cachedCoords != null ? cachedCoords.size() : 0;
        info.add("Blocks: " + size);
        return info;
    }
}
