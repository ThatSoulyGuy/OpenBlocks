package com.openblocks.core.network;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.canvas.CanvasBlockEntity;
import com.openblocks.canvas.CanvasPaintPacket;
import com.openblocks.elevator.ElevatorActionHandler;
import com.openblocks.elevator.ElevatorActionPacket;
import com.openblocks.guide.GuideActionPacket;
import com.openblocks.guide.GuideBlockEntity;
import com.openblocks.imaginary.DrawingTableActionPacket;
import com.openblocks.imaginary.DrawingTableBlockEntity;
import com.openblocks.interaction.CannonBlockEntity;
import com.openblocks.interaction.CannonControlPacket;
import com.openblocks.tank.TankFluidSyncPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central networking setup for OpenBlocks using Architectury's NetworkManager.
 * Replaces OpenModsLib's RPC system and NetworkEventManager.
 */
public final class OpenBlocksNetwork {

    public static void register() {
        // Elevator: C2S packet for jump/sneak teleportation
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                ElevatorActionPacket.TYPE,
                ElevatorActionPacket.STREAM_CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                        ElevatorActionHandler.handleElevatorAction(serverPlayer, packet.goingUp());
                    }
                })
        );

        // Cannon: C2S packet for pitch/speed adjustment
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                CannonControlPacket.TYPE,
                CannonControlPacket.STREAM_CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                        ServerLevel level = serverPlayer.serverLevel();
                        if (level.getBlockEntity(packet.pos()) instanceof CannonBlockEntity cannon) {
                            cannon.setPitch(Math.max(0, Math.min(90, packet.pitch())));
                            cannon.setSpeed(Math.max(0.1f, Math.min(5.0f, packet.speed())));
                        }
                    }
                })
        );

        // Canvas: C2S packet for painting a face
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                CanvasPaintPacket.TYPE,
                CanvasPaintPacket.STREAM_CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                        ServerLevel level = serverPlayer.serverLevel();
                        int faceOrdinal = packet.face();
                        if (faceOrdinal >= 0 && faceOrdinal < Direction.values().length) {
                            if (level.getBlockEntity(packet.pos()) instanceof CanvasBlockEntity canvas) {
                                canvas.applyPaint(packet.color(), Direction.values()[faceOrdinal]);
                            }
                        }
                    }
                })
        );

        // Guide: C2S packet for dimension/shape actions
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                GuideActionPacket.TYPE,
                GuideActionPacket.STREAM_CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                        ServerLevel level = serverPlayer.serverLevel();
                        if (level.getBlockEntity(packet.pos()) instanceof GuideBlockEntity guide) {
                            guide.handleAction(packet.action());
                        }
                    }
                })
        );

        // Drawing Table: C2S packet for pattern cycling
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                DrawingTableActionPacket.TYPE,
                DrawingTableActionPacket.STREAM_CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                        ServerLevel level = serverPlayer.serverLevel();
                        if (level.getBlockEntity(packet.pos()) instanceof DrawingTableBlockEntity table) {
                            if (packet.action() == DrawingTableActionPacket.ACTION_NEXT) {
                                table.cyclePatternUp();
                            } else if (packet.action() == DrawingTableActionPacket.ACTION_PREV) {
                                table.cyclePatternDown();
                            }
                        }
                    }
                })
        );

        // Tank: S2C packet for fluid rendering sync
        NetworkManager.registerReceiver(
                NetworkManager.s2c(),
                TankFluidSyncPacket.TYPE,
                TankFluidSyncPacket.STREAM_CODEC,
                (packet, context) -> {
                    // Client-side handling is done via block entity data sync
                    // This packet serves as an additional sync mechanism for large fluid changes
                }
        );
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, path);
    }

    private OpenBlocksNetwork() {}
}
