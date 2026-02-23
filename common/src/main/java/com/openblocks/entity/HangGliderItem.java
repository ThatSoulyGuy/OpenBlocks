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
 * Item that toggles a hang glider entity for the player.
 * Right-click to deploy or retract the glider.
 */
public class HangGliderItem extends Item {

    public HangGliderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // Toggle glider
        HangGliderEntity existing = HangGliderEntity.GLIDER_MAP.get(player);
        if (existing != null && !existing.isRemoved()) {
            existing.discard();
            return InteractionResultHolder.success(stack);
        }

        // Create new glider
        HangGliderEntity glider = OpenBlocksEntities.HANG_GLIDER.get().create((ServerLevel) level);
        if (glider != null) {
            glider.setPlayer(player);
            level.addFreshEntity(glider);
        }

        return InteractionResultHolder.success(stack);
    }
}
