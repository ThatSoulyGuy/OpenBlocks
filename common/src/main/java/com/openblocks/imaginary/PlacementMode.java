package com.openblocks.imaginary;

import net.minecraft.network.chat.Component;

/**
 * The 8 placement modes for imaginary items (pencils/crayons).
 * 4 shapes × (normal + inverted). Cycled via shift-right-click.
 */
public enum PlacementMode {
    BLOCK(1.0f, "block", false, ImaginaryShape.BLOCK),
    PANEL(0.5f, "panel", false, ImaginaryShape.PANEL),
    HALF_PANEL(0.5f, "half_panel", false, ImaginaryShape.HALF_PANEL),
    STAIRS(0.75f, "stairs", false, ImaginaryShape.STAIRS),
    INV_BLOCK(1.5f, "inverted_block", true, ImaginaryShape.BLOCK),
    INV_PANEL(1.0f, "inverted_panel", true, ImaginaryShape.PANEL),
    INV_HALF_PANEL(1.0f, "inverted_half_panel", true, ImaginaryShape.HALF_PANEL),
    INV_STAIRS(1.25f, "inverted_stairs", true, ImaginaryShape.STAIRS);

    private final float cost;
    private final String translationKey;
    private final boolean inverted;
    private final ImaginaryShape shape;

    PlacementMode(float cost, String name, boolean inverted, ImaginaryShape shape) {
        this.cost = cost;
        this.translationKey = "openblocks.misc.mode." + name;
        this.inverted = inverted;
        this.shape = shape;
    }

    public float getCost() {
        return cost;
    }

    public boolean isInverted() {
        return inverted;
    }

    public ImaginaryShape getShape() {
        return shape;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public PlacementMode next() {
        PlacementMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static PlacementMode fromOrdinal(int ordinal) {
        PlacementMode[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BLOCK;
    }
}
