package com.openblocks.trophy;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MooshroomBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        ItemStack mushroom = new ItemStack(Items.RED_MUSHROOM);
        if (!player.getInventory().add(mushroom)) {
            player.drop(mushroom, false);
        }
        return 20000;
    }
}
