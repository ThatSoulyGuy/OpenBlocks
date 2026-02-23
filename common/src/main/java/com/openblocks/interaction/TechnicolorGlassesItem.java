package com.openblocks.interaction;

import com.openblocks.imaginary.IImaginationGlasses;
import com.openblocks.imaginary.ImaginaryBlockEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Technicolor Glasses. When worn in helmet slot, reveals both pencil
 * and crayon imaginary blocks. Found in dungeon loot.
 */
public class TechnicolorGlassesItem extends Item implements Equipable, IImaginationGlasses {

    public TechnicolorGlassesItem(Properties properties) {
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
        return block.isInverted();
    }
}
