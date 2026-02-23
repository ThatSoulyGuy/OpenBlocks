package com.openblocks.elevator;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Client-side handler that detects jump/sneak state transitions
 * and sends elevator action packets to the server.
 * <p>
 * The original OpenBlocks detected the <b>transition</b> (press event, not hold),
 * so we track the previous tick's state and only fire on rising edges.
 * <p>
 * This class is only referenced from client entrypoints via {@code OpenBlocksClient.init()}.
 */
public final class ElevatorInputHandler {

    private static boolean wasJumping = false;
    private static boolean wasSneaking = false;

    /**
     * Called every client tick (post) to check for elevator input transitions.
     */
    public static void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.isPaused()) {
            wasJumping = false;
            wasSneaking = false;
            return;
        }

        LocalPlayer player = client.player;
        boolean isJumping = player.input.jumping;
        boolean isSneaking = player.input.shiftKeyDown;

        // Detect rising edges (was not pressed last tick, is pressed this tick)
        if (isJumping && !wasJumping) {
            // Check if standing on an elevator
            if (isOnElevator(player)) {
                NetworkManager.sendToServer(new ElevatorActionPacket(true));
            }
        }

        if (isSneaking && !wasSneaking) {
            if (isOnElevator(player)) {
                NetworkManager.sendToServer(new ElevatorActionPacket(false));
            }
        }

        wasJumping = isJumping;
        wasSneaking = isSneaking;
    }

    private static boolean isOnElevator(LocalPlayer player) {
        return player.level().getBlockState(player.blockPosition().below()).getBlock() instanceof IElevatorBlock;
    }

    private ElevatorInputHandler() {}
}
