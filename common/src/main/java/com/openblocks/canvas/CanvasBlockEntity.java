package com.openblocks.canvas;

import com.openblocks.imaginary.StencilPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stores per-face paint colors and stencil data for canvas blocks.
 * Color 0 = unpainted (transparent / no tint).
 * Stencil -1 = no stencil on that face.
 */
public class CanvasBlockEntity extends BlockEntity {

    private final int[] faceColors = new int[6]; // One per Direction ordinal
    /** Stencil pattern ordinal per face, or -1 if none. */
    private final int[] faceStencils = {-1, -1, -1, -1, -1, -1};
    /** Whether the face has an active stencil cover (not yet painted through, can be removed). */
    private final boolean[] stencilCovers = new boolean[6];

    public CanvasBlockEntity(BlockPos pos, BlockState state) {
        super(com.openblocks.core.registry.OpenBlocksBlockEntities.CANVAS.get(), pos, state);
    }

    public int getColor(Direction face) {
        return faceColors[face.ordinal()];
    }

    public void setColor(Direction face, int color) {
        faceColors[face.ordinal()] = color;
        markDirtyAndSync();
    }

    /**
     * Get the stencil pattern on a face, or null if none.
     */
    @Nullable
    public StencilPattern getStencil(Direction face) {
        int ordinal = faceStencils[face.ordinal()];
        if (ordinal >= 0 && ordinal < StencilPattern.values().length) {
            return StencilPattern.values()[ordinal];
        }
        return null;
    }

    /**
     * Whether this face has an active stencil cover that can be removed.
     */
    public boolean hasStencilCover(Direction face) {
        return stencilCovers[face.ordinal()];
    }

    /**
     * Place a stencil cover on the given face. Returns false if a stencil is already present.
     */
    public boolean placeStencil(Direction face, StencilPattern pattern) {
        int idx = face.ordinal();
        if (stencilCovers[idx]) return false; // Already has a cover
        faceStencils[idx] = pattern.ordinal();
        stencilCovers[idx] = true;
        markDirtyAndSync();
        return true;
    }

    /**
     * Remove the stencil cover from a face. Returns the pattern that was removed, or null.
     */
    @Nullable
    public StencilPattern removeStencilCover(Direction face) {
        int idx = face.ordinal();
        if (!stencilCovers[idx]) return null;
        StencilPattern pattern = getStencil(face);
        stencilCovers[idx] = false;
        faceStencils[idx] = -1;
        markDirtyAndSync();
        return pattern;
    }

    /**
     * Apply paint to a face. If the face has a stencil cover, the paint is applied
     * through the stencil (storing both color and pattern), and the cover is consumed.
     */
    public boolean applyPaint(int color, Direction face) {
        int idx = face.ordinal();
        if (stencilCovers[idx]) {
            // Paint through stencil — store color + pattern, consume cover
            faceColors[idx] = color;
            stencilCovers[idx] = false;
            // faceStencils[idx] remains set — the pattern is "painted in"
            markDirtyAndSync();
            return true;
        }
        // No stencil — plain solid paint
        if (faceColors[idx] == color && faceStencils[idx] == -1) return false;
        faceColors[idx] = color;
        faceStencils[idx] = -1;
        markDirtyAndSync();
        return true;
    }

    public boolean clearFace(Direction face) {
        int idx = face.ordinal();
        boolean changed = faceColors[idx] != 0 || faceStencils[idx] != -1 || stencilCovers[idx];
        faceColors[idx] = 0;
        faceStencils[idx] = -1;
        stencilCovers[idx] = false;
        if (changed) markDirtyAndSync();
        return changed;
    }

    /**
     * Clear a face but return the stencil cover if one was present (for dropping as item).
     */
    @Nullable
    public StencilPattern clearFaceAndReturnCover(Direction face) {
        int idx = face.ordinal();
        StencilPattern cover = stencilCovers[idx] ? getStencil(face) : null;
        faceColors[idx] = 0;
        faceStencils[idx] = -1;
        stencilCovers[idx] = false;
        markDirtyAndSync();
        return cover;
    }

    public void clearAllFaces() {
        for (int i = 0; i < 6; i++) {
            faceColors[i] = 0;
            faceStencils[i] = -1;
            stencilCovers[i] = false;
        }
        markDirtyAndSync();
    }

    public boolean isUnpainted() {
        for (int i = 0; i < 6; i++) {
            if (faceColors[i] != 0 || faceStencils[i] != -1 || stencilCovers[i]) return false;
        }
        return true;
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("FaceColors", faceColors.clone());
        tag.putIntArray("FaceStencils", faceStencils.clone());
        // Pack booleans into a byte
        byte coverBits = 0;
        for (int i = 0; i < 6; i++) {
            if (stencilCovers[i]) coverBits |= (byte) (1 << i);
        }
        tag.putByte("StencilCovers", coverBits);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("FaceColors")) {
            int[] saved = tag.getIntArray("FaceColors");
            System.arraycopy(saved, 0, faceColors, 0, Math.min(saved.length, 6));
        }
        if (tag.contains("FaceStencils")) {
            int[] saved = tag.getIntArray("FaceStencils");
            System.arraycopy(saved, 0, faceStencils, 0, Math.min(saved.length, 6));
        }
        if (tag.contains("StencilCovers")) {
            byte coverBits = tag.getByte("StencilCovers");
            for (int i = 0; i < 6; i++) {
                stencilCovers[i] = (coverBits & (1 << i)) != 0;
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
