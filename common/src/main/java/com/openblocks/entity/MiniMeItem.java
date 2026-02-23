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
 * Item that spawns a MiniMe entity that looks like the player.
 */
public class MiniMeItem extends Item {

    public MiniMeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        MiniMeEntity miniMe = OpenBlocksEntities.MINI_ME.get().create((ServerLevel) level);
        if (miniMe == null) return InteractionResultHolder.fail(stack);

        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x * 2;
        double y = player.getY();
        double z = player.getZ() + look.z * 2;

        miniMe.moveTo(x, y, z, player.getYRot(), 0);
        miniMe.setOwnerUUID(player.getUUID());
        level.addFreshEntity(miniMe);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}
