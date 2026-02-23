package com.openblocks.imaginary;

import net.minecraft.util.StringRepresentable;

public enum ImaginaryType implements StringRepresentable {
    PENCIL("pencil"),
    CRAYON("crayon");

    private final String name;

    ImaginaryType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
