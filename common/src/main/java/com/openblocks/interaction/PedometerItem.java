package com.openblocks.interaction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pedometer item. Right-click to start/stop tracking movement.
 * On stop, displays total distance traveled, speed, and elapsed time.
 */
public class PedometerItem extends Item {

    private static final Map<UUID, TrackingData> activeTrackers = new HashMap<>();

    public PedometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            UUID playerId = player.getUUID();

            if (activeTrackers.containsKey(playerId)) {
                // Stop tracking and report
                TrackingData data = activeTrackers.remove(playerId);
                double totalDistance = data.totalDistance + player.position().distanceTo(data.lastPosition);
                long elapsedTicks = level.getGameTime() - data.startTick;
                double elapsedSeconds = elapsedTicks / 20.0;
                double avgSpeed = elapsedSeconds > 0 ? totalDistance / elapsedSeconds : 0;

                player.displayClientMessage(Component.translatable(
                        "item.openblocks.pedometer.report",
                        String.format("%.2f", totalDistance),
                        String.format("%.1f", elapsedSeconds),
                        String.format("%.2f", avgSpeed)
                ), false);
            } else {
                // Start tracking
                activeTrackers.put(playerId, new TrackingData(
                        player.position(),
                        level.getGameTime()
                ));
                player.displayClientMessage(
                        Component.translatable("item.openblocks.pedometer.started"), false);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Call each tick for tracked players to accumulate distance.
     * This is handled via server player tick events.
     */
    public static void tickPlayer(Player player) {
        UUID playerId = player.getUUID();
        TrackingData data = activeTrackers.get(playerId);
        if (data != null) {
            data.totalDistance += player.position().distanceTo(data.lastPosition);
            data.lastPosition = player.position();
        }
    }

    private static class TrackingData {
        Vec3 lastPosition;
        final long startTick;
        double totalDistance = 0;

        TrackingData(Vec3 startPos, long startTick) {
            this.lastPosition = startPos;
            this.startTick = startTick;
        }
    }
}
