package com.openblocks.canvas;

import com.openblocks.imaginary.StencilPattern;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Per-block paint data for non-canvas blocks. Stores per-face colors,
 * stencil patterns, and stencil covers. Same data model as CanvasBlockEntity
 * but decoupled from any specific block type.
 */
public class PaintEntry {

    private final int[] faceColors = new int[6];
    private final int[] faceStencils = {-1, -1, -1, -1, -1, -1};
    private final boolean[] stencilCovers = new boolean[6];

    public int getColor(Direction face) {
        return faceColors[face.ordinal()];
    }

    @Nullable
    public StencilPattern getStencil(Direction face) {
        int ordinal = faceStencils[face.ordinal()];
        StencilPattern[] values = StencilPattern.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }

    public boolean hasStencilCover(Direction face) {
        return stencilCovers[face.ordinal()];
    }

    /**
     * Apply paint to a face. If a stencil cover is present, the paint goes through
     * the stencil pattern and the cover is consumed.
     */
    public boolean applyPaint(int color, Direction face) {
        int idx = face.ordinal();
        if (stencilCovers[idx]) {
            faceColors[idx] = color;
            stencilCovers[idx] = false;
            // faceStencils[idx] stays — pattern is "painted in"
            return true;
        }
        if (faceColors[idx] == color && faceStencils[idx] == -1) return false;
        faceColors[idx] = color;
        faceStencils[idx] = -1;
        return true;
    }

    /**
     * Place a stencil cover on the given face. Returns false if a cover is already present.
     */
    public boolean placeStencil(Direction face, StencilPattern pattern) {
        int idx = face.ordinal();
        if (stencilCovers[idx]) return false;
        faceStencils[idx] = pattern.ordinal();
        stencilCovers[idx] = true;
        return true;
    }

    /**
     * Remove the stencil cover from a face. Returns the pattern or null.
     */
    @Nullable
    public StencilPattern removeStencilCover(Direction face) {
        int idx = face.ordinal();
        if (!stencilCovers[idx]) return null;
        StencilPattern pattern = getStencil(face);
        stencilCovers[idx] = false;
        faceStencils[idx] = -1;
        return pattern;
    }

    public boolean clearFace(Direction face) {
        int idx = face.ordinal();
        boolean changed = faceColors[idx] != 0 || faceStencils[idx] != -1 || stencilCovers[idx];
        faceColors[idx] = 0;
        faceStencils[idx] = -1;
        stencilCovers[idx] = false;
        return changed;
    }

    public void clearAllFaces() {
        for (int i = 0; i < 6; i++) {
            faceColors[i] = 0;
            faceStencils[i] = -1;
            stencilCovers[i] = false;
        }
    }

    public boolean isEmpty() {
        for (int i = 0; i < 6; i++) {
            if (faceColors[i] != 0 || faceStencils[i] != -1 || stencilCovers[i]) return false;
        }
        return true;
    }

    public int[] getFaceColors() {
        return faceColors;
    }

    public int[] getFaceStencils() {
        return faceStencils;
    }

    public byte getCoverBits() {
        byte bits = 0;
        for (int i = 0; i < 6; i++) {
            if (stencilCovers[i]) bits |= (byte) (1 << i);
        }
        return bits;
    }

    public void setCoverBits(byte bits) {
        for (int i = 0; i < 6; i++) {
            stencilCovers[i] = (bits & (1 << i)) != 0;
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("FaceColors", faceColors.clone());
        tag.putIntArray("FaceStencils", faceStencils.clone());
        tag.putByte("StencilCovers", getCoverBits());
        return tag;
    }

    public static PaintEntry load(CompoundTag tag) {
        PaintEntry entry = new PaintEntry();
        if (tag.contains("FaceColors")) {
            int[] saved = tag.getIntArray("FaceColors");
            System.arraycopy(saved, 0, entry.faceColors, 0, Math.min(saved.length, 6));
        }
        if (tag.contains("FaceStencils")) {
            int[] saved = tag.getIntArray("FaceStencils");
            System.arraycopy(saved, 0, entry.faceStencils, 0, Math.min(saved.length, 6));
        }
        if (tag.contains("StencilCovers")) {
            entry.setCoverBits(tag.getByte("StencilCovers"));
        }
        return entry;
    }

    /**
     * Create a PaintEntry from raw packet data.
     */
    public static PaintEntry fromPacketData(int[] faceColors, int[] faceStencils, byte coverBits) {
        PaintEntry entry = new PaintEntry();
        System.arraycopy(faceColors, 0, entry.faceColors, 0, Math.min(faceColors.length, 6));
        System.arraycopy(faceStencils, 0, entry.faceStencils, 0, Math.min(faceStencils.length, 6));
        entry.setCoverBits(coverBits);
        return entry;
    }
}
