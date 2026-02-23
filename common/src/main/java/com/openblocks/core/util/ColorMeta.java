package com.openblocks.core.util;

import net.minecraft.world.item.DyeColor;

/**
 * Maps dye colors to RGB values.
 * Ported from OpenModsLib's openmods.colors.ColorMeta.
 */
public enum ColorMeta {
    WHITE(DyeColor.WHITE, 0xFFFFFF),
    ORANGE(DyeColor.ORANGE, 0xD87F33),
    MAGENTA(DyeColor.MAGENTA, 0xB24CD8),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, 0x6699D8),
    YELLOW(DyeColor.YELLOW, 0xE5E533),
    LIME(DyeColor.LIME, 0x7FCC19),
    PINK(DyeColor.PINK, 0xF27FA5),
    GRAY(DyeColor.GRAY, 0x4C4C4C),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, 0x999999),
    CYAN(DyeColor.CYAN, 0x4C7F99),
    PURPLE(DyeColor.PURPLE, 0x7F3FB2),
    BLUE(DyeColor.BLUE, 0x334CB2),
    BROWN(DyeColor.BROWN, 0x664C33),
    GREEN(DyeColor.GREEN, 0x667F33),
    RED(DyeColor.RED, 0x993333),
    BLACK(DyeColor.BLACK, 0x191919);

    private final DyeColor dyeColor;
    private final int rgb;
    private final int red;
    private final int green;
    private final int blue;

    ColorMeta(DyeColor dyeColor, int rgb) {
        this.dyeColor = dyeColor;
        this.rgb = rgb;
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    public int getRgb() {
        return rgb;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public static ColorMeta fromDyeColor(DyeColor color) {
        for (ColorMeta meta : values()) {
            if (meta.dyeColor == color) {
                return meta;
            }
        }
        return WHITE;
    }

    public static ColorMeta fromOrdinal(int ordinal) {
        ColorMeta[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : WHITE;
    }
}
