package com.openblocks.entity;

import com.openblocks.core.registry.OpenBlocksEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Item that spawns or despawns a Luggage entity.
 * Right-click to spawn luggage in front of the player.
 */
public class LuggageItem extends Item {

    public LuggageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        LuggageEntity luggage = OpenBlocksEntities.LUGGAGE.get().create((ServerLevel) level);
        if (luggage == null) return InteractionResultHolder.fail(stack);

        // Spawn 2 blocks in front of the player
        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x * 2;
        double y = player.getY();
        double z = player.getZ() + look.z * 2;

        luggage.moveTo(x, y, z, player.getYRot(), 0);
        luggage.setOwner(player);
        luggage.restoreFromStack(stack);
        level.addFreshEntity(luggage);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}
