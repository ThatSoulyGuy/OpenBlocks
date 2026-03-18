package com.openblocks.imaginary;

import net.minecraft.util.StringRepresentable;

/**
 * Available stencil patterns for the drawing table.
 * Each pattern has a 16x16 bit grid defining the cutout area.
 * 'X' = cutout (paint passes through), ' ' = solid (paint blocked).
 */
public enum StencilPattern implements StringRepresentable {
    CREEPER_FACE("creeper_face",
            "                " +
            "                " +
            "  XXXX    XXXX  " +
            "  XXXX    XXXX  " +
            "  XXXX    XXXX  " +
            "  XXXX    XXXX  " +
            "      XXXX      " +
            "      XXXX      " +
            "    XXXXXXXX    " +
            "    XXXXXXXX    " +
            "    XXXXXXXX    " +
            "    XXXXXXXX    " +
            "    XX    XX    " +
            "    XX    XX    " +
            "                " +
            "                "),
    BORDER("border",
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              " +
            "XX              "),
    STRIPES("stripes",
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X " +
            "X X X X X X X X "),
    CORNER("corner",
            "                " +
            "                " +
            "  XXXXXX        " +
            "  XXXXXX        " +
            "  XX            " +
            "  XX            " +
            "  XX            " +
            "  XX            " +
            "                " +
            "                " +
            "                " +
            "                " +
            "                " +
            "                " +
            "                " +
            "                "),
    HEART("heart",
            "                " +
            "                " +
            "                " +
            "   XXX    XXX   " +
            "  XXXXX  XXXXX  " +
            "  XXXXXXXXXXXX  " +
            "  XXXXXXXXXXXX  " +
            "  XXXXXXXXXXXX  " +
            "   XXXXXXXXXX   " +
            "    XXXXXXXX    " +
            "     XXXXXX     " +
            "      XXXX      " +
            "       XX       " +
            "                " +
            "                " +
            "                "),
    DIAMOND("diamond",
            "                " +
            "       XX       " +
            "      XXXX      " +
            "     XXXXXX     " +
            "    XXXXXXXX    " +
            "   XXXXXXXXXX   " +
            "  XXXXXXXXXXXX  " +
            " XXXXXXXXXXXXXX " +
            " XXXXXXXXXXXXXX " +
            "  XXXXXXXXXXXX  " +
            "   XXXXXXXXXX   " +
            "    XXXXXXXX    " +
            "     XXXXXX     " +
            "      XXXX      " +
            "       XX       " +
            "                "),
    CROSS("cross",
            "     XXXXXX     " +
            "     XXXXXX     " +
            "     XXXXXX     " +
            "     XXXXXX     " +
            "     XXXXXX     " +
            "XXXXXXXXXXXXXXXX" +
            "XXXXXXXXXXXXXXXX" +
            "XXXXXXXXXXXXXXXX" +
            "XXXXXXXXXXXXXXXX" +
            "XXXXXXXXXXXXXXXX" +
            "XXXXXXXXXXXXXXXX" +
            "     XXXXXX     " +
            "     XXXXXX     " +
            "     XXXXXX     " +
            "     XXXXXX     " +
            "     XXXXXX     "),
    SKULL("skull",
            "                " +
            "    XXXXXXXX    " +
            "   XXXXXXXXXX   " +
            "  XXXXXXXXXXXX  " +
            "  XXXXXXXXXXXX  " +
            "  XXX  XX  XXX  " +
            "  XXX  XX  XXX  " +
            "  XXXXXXXXXXXX  " +
            "   XXXXXXXXXX   " +
            "    XXXXXXXX    " +
            "    X XX XX X   " +
            "     XXXXXX     " +
            "      XXXX      " +
            "                " +
            "                " +
            "                "),
    ARROW("arrow",
            "       XX       " +
            "      XXXX      " +
            "     XXXXXX     " +
            "    XXXXXXXX    " +
            "   XXXXXXXXXX   " +
            "  XXXXXXXXXXXX  " +
            " XXXXXXXXXXXXXX " +
            "       XX       " +
            "       XX       " +
            "       XX       " +
            "       XX       " +
            "       XX       " +
            "       XX       " +
            "       XX       " +
            "       XX       " +
            "       XX       "),
    CIRCLE("circle",
            "                " +
            "     XXXXXX     " +
            "   XXXXXXXXXX   " +
            "  XXXXXXXXXXXX  " +
            "  XXXXXXXXXXXX  " +
            " XXXXXXXXXXXXXX " +
            " XXXXXXXXXXXXXX " +
            " XXXXXXXXXXXXXX " +
            " XXXXXXXXXXXXXX " +
            " XXXXXXXXXXXXXX " +
            " XXXXXXXXXXXXXX " +
            "  XXXXXXXXXXXX  " +
            "  XXXXXXXXXXXX  " +
            "   XXXXXXXXXX   " +
            "     XXXXXX     " +
            "                "),
    STAR("star",
            "       XX       " +
            "       XX       " +
            "      XXXX      " +
            "      XXXX      " +
            "XXXXXXXXXXXXXXXX" +
            " XXXXXXXXXXXXXX " +
            "  XXXXXXXXXXXX  " +
            "   XXXXXXXXXX   " +
            "    XXXXXXXX    " +
            "   XXXXXXXXXX   " +
            "  XX  XXXX  XX  " +
            "  X   XXXX   X  " +
            "      XXXX      " +
            "     X    X     " +
            "     XX  XX     " +
            "                ");

    private final String name;
    private final long[] bits; // 4 longs = 256 bits = 16x16 grid

    StencilPattern(String name, String format) {
        this.name = name;
        if (format.length() != 256) {
            throw new IllegalArgumentException("Stencil pattern must be 16x16 (256 chars), got " + format.length());
        }
        this.bits = new long[4];
        for (int i = 0; i < 256; i++) {
            if (format.charAt(i) != ' ') {
                bits[i / 64] |= (1L << (i % 64));
            }
        }
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * Check if the pixel at (x, y) is a cutout (paint passes through).
     * @param x Column (0-15, left to right)
     * @param y Row (0-15, top to bottom)
     * @return true if this pixel is a cutout
     */
    public boolean isSet(int x, int y) {
        int index = y * 16 + x;
        return (bits[index / 64] & (1L << (index % 64))) != 0;
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
