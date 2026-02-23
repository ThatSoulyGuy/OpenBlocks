package com.openblocks.elevator;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet sent when a player jumps or sneaks on an elevator.
 */
public record ElevatorActionPacket(boolean goingUp) implements CustomPacketPayload {

    public static final Type<ElevatorActionPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("elevator_action"));

    public static final StreamCodec<FriendlyByteBuf, ElevatorActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ElevatorActionPacket::goingUp,
                    ElevatorActionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
