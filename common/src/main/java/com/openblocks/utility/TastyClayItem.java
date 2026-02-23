package com.openblocks.utility;

import net.minecraft.world.item.Item;

/**
 * A food item that can be eaten even when full.
 * In the original mod, eating tasty clay contributed to the "digestion"
 * tomfoolery system. That system will be added in a later phase.
 */
public class TastyClayItem extends Item {

    public TastyClayItem(Properties properties) {
        super(properties);
    }
}
