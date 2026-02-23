package com.openblocks.tank;

import com.openblocks.core.network.OpenBlocksNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-client packet to sync tank fluid data for rendering.
 */
public record TankFluidSyncPacket(BlockPos pos, ResourceLocation fluidId, long amount) implements CustomPacketPayload {

    public static final Type<TankFluidSyncPacket> TYPE =
            new Type<>(OpenBlocksNetwork.id("tank_fluid_sync"));

    public static final StreamCodec<FriendlyByteBuf, TankFluidSyncPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public TankFluidSyncPacket decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    ResourceLocation fluidId = buf.readResourceLocation();
                    long amount = buf.readLong();
                    return new TankFluidSyncPacket(pos, fluidId, amount);
                }

                @Override
                public void encode(FriendlyByteBuf buf, TankFluidSyncPacket packet) {
                    buf.writeBlockPos(packet.pos);
                    buf.writeResourceLocation(packet.fluidId);
                    buf.writeLong(packet.amount);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
