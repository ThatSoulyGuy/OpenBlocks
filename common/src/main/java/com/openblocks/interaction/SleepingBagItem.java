package com.openblocks.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sleeping bag item. Right-click to sleep without setting spawn point.
 * Unlike beds, the sleeping bag does not change the player's respawn position.
 */
public class SleepingBagItem extends Item {

    public SleepingBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockPos pos = player.blockPosition();

            // Check if it's night time
            if (!level.dimensionType().hasFixedTime() && level.isNight()) {
                // Save current spawn point before sleeping
                BlockPos savedSpawn = serverPlayer.getRespawnPosition();
                float savedAngle = serverPlayer.getRespawnAngle();
                ResourceKey<Level> savedDimension = serverPlayer.getRespawnDimension();

                // Attempt to sleep
                Player.BedSleepingProblem problem = player.startSleepInBed(pos).left().orElse(null);

                if (problem != null) {
                    player.displayClientMessage(
                            Component.translatable("block.minecraft.bed." + problem.name().toLowerCase()), true);
                } else {
                    // Restore spawn point so sleeping bag doesn't change it
                    serverPlayer.setRespawnPosition(savedDimension, savedSpawn, savedAngle, false, false);
                }
            } else {
                player.displayClientMessage(
                        Component.translatable("block.minecraft.bed.no_sleep"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
