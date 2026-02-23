package com.openblocks.imaginary;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for imaginary blocks. Stores color (null = pencil, non-null = crayon),
 * inversion state, and provides visibility/solidity checks based on player glasses.
 */
public class ImaginaryBlockEntity extends OpenBlocksBlockEntity {

    private Integer color; // null = pencil (gray), non-null = crayon RGB
    private boolean inverted;

    // Client-side fade animation
    private float visibility = 0.0f;

    public ImaginaryBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.IMAGINARY.get(), pos, state);
    }

    public boolean isPencil() {
        return color == null;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
        sync();
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        sync();
    }

    public float getVisibility() {
        return visibility;
    }

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }

    /**
     * Check if this block is visible for the given player based on their glasses.
     */
    public boolean isVisibleFor(Player player) {
        if (player.isSpectator()) return true;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof IImaginationGlasses glasses) {
            return glasses.canSeeImaginaryBlock(helmet, this);
        }

        // No glasses: visible only if inverted
        return inverted;
    }

    /**
     * Check if this block is solid (collidable) for the given player.
     */
    public boolean isSolidFor(Player player) {
        if (player.isSpectator()) return false;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof IImaginationGlasses glasses) {
            return glasses.canTouchImaginaryBlock(helmet, this);
        }

        // No glasses: pencil blocks are always solid, crayon blocks are not
        // Inverted reverses this
        boolean solidByDefault = isPencil();
        return solidByDefault != inverted;
    }

    /**
     * Check if this block is selectable (outline visible) for the given player.
     */
    public boolean isSelectableFor(Player player) {
        return isVisibleFor(player);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (color != null) {
            tag.putInt("Color", color);
        }
        tag.putBoolean("Inverted", inverted);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Color")) {
            color = tag.getInt("Color");
        } else {
            color = null;
        }
        inverted = tag.getBoolean("Inverted");
    }
}
