package com.openblocks.imaginary;

import net.minecraft.util.StringRepresentable;

/**
 * Available stencil patterns for the drawing table.
 */
public enum StencilPattern implements StringRepresentable {
    CREEPER_FACE("creeper_face"),
    BORDER("border"),
    STRIPES("stripes"),
    CORNER("corner"),
    HEART("heart"),
    DIAMOND("diamond"),
    CROSS("cross"),
    SKULL("skull"),
    ARROW("arrow"),
    CIRCLE("circle"),
    STAR("star");

    private final String name;

    StencilPattern(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public StencilPattern next() {
        StencilPattern[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public StencilPattern prev() {
        StencilPattern[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}
