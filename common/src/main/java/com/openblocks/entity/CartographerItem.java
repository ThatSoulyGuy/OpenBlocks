package com.openblocks.entity;

import com.openblocks.core.registry.OpenBlocksEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Item that spawns a Cartographer entity.
 */
public class CartographerItem extends Item {

    public CartographerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        CartographerEntity cartographer = OpenBlocksEntities.CARTOGRAPHER.get().create((ServerLevel) level);
        if (cartographer == null) return InteractionResultHolder.fail(stack);

        cartographer.setOwner(player);
        level.addFreshEntity(cartographer);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}
