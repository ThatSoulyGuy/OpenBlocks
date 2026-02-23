package com.openblocks.interaction;

import com.openblocks.imaginary.IImaginationGlasses;
import com.openblocks.imaginary.ImaginaryBlockEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Sonic Glasses. When worn in helmet slot, provides a sound
 * visualization overlay showing nearby sound sources.
 * Also reveals all imaginary blocks.
 */
public class SonicGlassesItem extends Item implements Equipable, IImaginationGlasses {

    public SonicGlassesItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public boolean canSeeImaginaryBlock(ItemStack glassesStack, ImaginaryBlockEntity block) {
        return true;
    }

    @Override
    public boolean canTouchImaginaryBlock(ItemStack glassesStack, ImaginaryBlockEntity block) {
        return true;
    }
}
