package com.openblocks.canvas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stores one ARGB color per face of a canvas block.
 * Color 0 = unpainted (transparent / no tint).
 */
public class CanvasBlockEntity extends BlockEntity {

    private final int[] faceColors = new int[6]; // One per Direction ordinal

    public CanvasBlockEntity(BlockPos pos, BlockState state) {
        super(com.openblocks.core.registry.OpenBlocksBlockEntities.CANVAS.get(), pos, state);
    }

    public int getColor(Direction face) {
        return faceColors[face.ordinal()];
    }

    public void setColor(Direction face, int color) {
        faceColors[face.ordinal()] = color;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean applyPaint(int color, Direction face) {
        if (faceColors[face.ordinal()] == color) return false;
        setColor(face, color);
        return true;
    }

    public boolean clearFace(Direction face) {
        if (faceColors[face.ordinal()] == 0) return false;
        setColor(face, 0);
        return true;
    }

    public void clearAllFaces() {
        for (int i = 0; i < 6; i++) {
            faceColors[i] = 0;
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isUnpainted() {
        for (int color : faceColors) {
            if (color != 0) return false;
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("FaceColors", faceColors.clone());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("FaceColors")) {
            int[] saved = tag.getIntArray("FaceColors");
            System.arraycopy(saved, 0, faceColors, 0, Math.min(saved.length, 6));
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
