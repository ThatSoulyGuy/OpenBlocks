package com.openblocks.interaction;

import com.openblocks.imaginary.IImaginationGlasses;
import com.openblocks.imaginary.ImaginaryBlockEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Pencil Glasses. When worn in helmet slot, reveals pencil-type imaginary blocks.
 */
public class PencilGlassesItem extends Item implements Equipable, IImaginationGlasses {

    public PencilGlassesItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public boolean canSeeImaginaryBlock(ItemStack glassesStack, ImaginaryBlockEntity block) {
        return block.isPencil() != block.isInverted();
    }

    @Override
    public boolean canTouchImaginaryBlock(ItemStack glassesStack, ImaginaryBlockEntity block) {
        return block.isPencil() != block.isInverted();
    }
}
