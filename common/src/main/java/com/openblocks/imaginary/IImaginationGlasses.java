package com.openblocks.imaginary;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for glasses items that allow viewing imaginary blocks.
 */
public interface IImaginationGlasses {

    /**
     * Whether this pair of glasses allows the wearer to see the given imaginary block.
     */
    boolean canSeeImaginaryBlock(ItemStack glassesStack, ImaginaryBlockEntity block);

    /**
     * Whether this pair of glasses allows the wearer to collide with the given imaginary block.
     */
    boolean canTouchImaginaryBlock(ItemStack glassesStack, ImaginaryBlockEntity block);
}
