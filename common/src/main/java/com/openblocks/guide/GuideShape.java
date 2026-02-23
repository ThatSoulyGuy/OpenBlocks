package com.openblocks.guide;

import net.minecraft.util.StringRepresentable;

public enum GuideShape implements StringRepresentable {
    SPHERE("sphere"),
    CYLINDER("cylinder"),
    DOME("dome"),
    CUBOID("cuboid"),
    DIAMOND("diamond");

    private final String name;

    GuideShape(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public GuideShape next() {
        GuideShape[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public GuideShape prev() {
        GuideShape[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}
