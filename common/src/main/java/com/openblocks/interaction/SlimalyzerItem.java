package com.openblocks.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Slimalyzer item. Right-click to check if the player is standing in a slime chunk.
 * Uses the vanilla slime chunk algorithm based on chunk coordinates and world seed.
 */
public class SlimalyzerItem extends Item {

    public SlimalyzerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            BlockPos pos = player.blockPosition();
            boolean isSlimeChunk = isSlimeChunk(level, pos);

            if (isSlimeChunk) {
                player.displayClientMessage(
                        Component.translatable("item.openblocks.slimalyzer.positive"), false);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(),
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.displayClientMessage(
                        Component.translatable("item.openblocks.slimalyzer.negative"), false);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(),
                        SoundSource.PLAYERS, 1.0f, 0.5f);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static boolean isSlimeChunk(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return false;

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        // Vanilla slime chunk algorithm
        java.util.Random rng = new java.util.Random(
                serverLevel.getSeed() +
                (long)(chunkX * chunkX * 0x4c1906) +
                (long)(chunkX * 0x5ac0db) +
                (long)(chunkZ * chunkZ) * 0x4307a7L +
                (long)(chunkZ * 0x5f24f) ^ 0x3ad8025fL
        );

        return rng.nextInt(10) == 0;
    }
}
