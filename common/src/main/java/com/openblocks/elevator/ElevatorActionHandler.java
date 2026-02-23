package com.openblocks.elevator;

import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server-side handler for elevator teleportation.
 * Searches the column above/below the player for a matching elevator and teleports them.
 */
public final class ElevatorActionHandler {

    public static void handleElevatorAction(ServerPlayer player, boolean goingUp) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Player must be standing on an elevator block
        BlockPos standingOn = playerPos.below();
        BlockState standingState = level.getBlockState(standingOn);

        if (!(standingState.getBlock() instanceof IElevatorBlock sourceElevator)) {
            return;
        }

        DyeColor sourceColor = sourceElevator.getElevatorColor(level, standingOn, standingState);
        int maxDistance = OpenBlocksConfig.Elevator.travelDistance;
        int maxPassThrough = OpenBlocksConfig.Elevator.maxPassThrough;
        int blocksInTheWay = 0;

        // Search column for matching elevator
        int direction = goingUp ? 1 : -1;
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();

        for (int dy = 1; dy <= maxDistance; dy++) {
            searchPos.set(standingOn.getX(), standingOn.getY() + (dy * direction), standingOn.getZ());

            // Check world bounds
            if (searchPos.getY() < level.getMinBuildHeight() || searchPos.getY() >= level.getMaxBuildHeight()) {
                break;
            }

            BlockState searchState = level.getBlockState(searchPos);

            // Check if it's a matching elevator
            if (searchState.getBlock() instanceof IElevatorBlock targetElevator) {
                DyeColor targetColor = targetElevator.getElevatorColor(level, searchPos, searchState);
                if (targetColor == sourceColor) {
                    // Found a matching elevator - check the block above is passable
                    BlockPos aboveTarget = searchPos.above();
                    BlockState aboveState = level.getBlockState(aboveTarget);
                    if (!aboveState.isAir() && !aboveState.getCollisionShape(level, aboveTarget).isEmpty()) {
                        // Target has no room — treat as obstacle and continue
                        ElevatorBlockRules.Action action = ElevatorBlockRules.getAction(level, aboveTarget, aboveState);
                        if (action == ElevatorBlockRules.Action.ABORT) break;
                        if (action == ElevatorBlockRules.Action.INCREMENT) {
                            blocksInTheWay++;
                            if (blocksInTheWay > maxPassThrough) break;
                        }
                        continue;
                    }

                    // XP drain
                    if (!player.isCreative() && OpenBlocksConfig.Elevator.xpDrainRatio > 0) {
                        int distance = Math.abs(dy);
                        int xpCost = Mth.ceil(OpenBlocksConfig.Elevator.xpDrainRatio * distance);
                        if (xpCost > 0) {
                            int playerXp = getPlayerXpPoints(player);
                            if (playerXp < xpCost) {
                                return; // Not enough XP
                            }
                            player.giveExperiencePoints(-xpCost);
                        }
                    }

                    // Teleport
                    double targetX, targetZ;
                    if (OpenBlocksConfig.Elevator.centerOnBlock) {
                        targetX = searchPos.getX() + 0.5;
                        targetZ = searchPos.getZ() + 0.5;
                    } else {
                        targetX = player.getX();
                        targetZ = player.getZ();
                    }

                    double targetY = searchPos.getY() + 1.0;

                    // Apply rotation from target elevator
                    PlayerRotation rotation = targetElevator.getElevatorRotation(level, searchPos, searchState);
                    float yRot = rotation.shouldRotate() ? rotation.getYRot() : player.getYRot();

                    player.teleportTo(targetX, targetY, targetZ);
                    player.setYRot(yRot);
                    player.setXRot(player.getXRot());

                    // Play sound
                    level.playSound(null, searchPos, OpenBlocksSounds.ELEVATOR_ACTIVATE.get(),
                            SoundSource.BLOCKS, 1.0f, 1.0f);

                    return;
                }
            }

            // Not an elevator — evaluate block rules
            ElevatorBlockRules.Action action = ElevatorBlockRules.getAction(level, searchPos, searchState);
            switch (action) {
                case ABORT:
                    return;
                case INCREMENT:
                    blocksInTheWay++;
                    if (blocksInTheWay > maxPassThrough) {
                        return;
                    }
                    break;
                case IGNORE:
                    break;
            }
        }
    }

    private static int getPlayerXpPoints(ServerPlayer player) {
        int level = player.experienceLevel;
        int points;
        if (level >= 30) {
            points = (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 15) {
            points = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            points = level * level + 6 * level;
        }
        points += Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return points;
    }

    private ElevatorActionHandler() {}
}
