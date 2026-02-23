package com.openblocks.grave;

import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles player death by capturing inventory and placing a grave block.
 * Registered via Architectury's EntityEvent.LIVING_DEATH.
 */
public final class PlayerDeathHandler {

    private PlayerDeathHandler() {}

    /**
     * Called when a living entity dies. If it's a server player, captures
     * their inventory and places a grave.
     */
    public static void onPlayerDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer player)) return;
        Level level = player.level();

        // Don't interfere if keepInventory is on
        if (level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        // Capture inventory
        List<ItemStack> items = captureInventory(player);
        if (items.isEmpty()) return;

        // Find placement position
        BlockPos deathPos = player.blockPosition();
        BlockPos gravePos = findGravePosition(level, deathPos);

        if (gravePos != null) {
            // Place grave
            Direction facing = player.getDirection().getOpposite();
            BlockState graveState = OpenBlocksBlocks.GRAVE.get().defaultBlockState()
                    .setValue(GraveBlock.FACING, facing);
            level.setBlock(gravePos, graveState, 3);

            if (level.getBlockEntity(gravePos) instanceof GraveBlockEntity grave) {
                grave.setPlayerName(player.getGameProfile().getName());
                grave.setDeathMessage(source.getLocalizedDeathMessage(player));
                grave.storeItems(items);
            }
        } else {
            // Failed to place grave - drop items normally
            for (ItemStack item : items) {
                player.drop(item, true, false);
            }
        }
    }

    private static List<ItemStack> captureInventory(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        var inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        return items;
    }

    private static BlockPos findGravePosition(Level level, BlockPos center) {
        int range = OpenBlocksConfig.Graves.spawnRange;
        int minY = Math.max(OpenBlocksConfig.Graves.minPosY, level.getMinBuildHeight());
        int maxY = Math.min(OpenBlocksConfig.Graves.maxPosY, level.getMaxBuildHeight() - 1);

        // Clamp center Y to valid range
        BlockPos searchCenter = new BlockPos(center.getX(),
                Math.max(minY, Math.min(maxY, center.getY())),
                center.getZ());

        // Search in expanding rings from center
        if (isValidGravePos(level, searchCenter)) return searchCenter;

        for (int r = 1; r <= range; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) != r && Math.abs(z) != r) continue; // Ring only
                    for (int y = 0; y <= r; y++) {
                        for (int dy : new int[]{y, -y}) {
                            if (y == 0 && dy != 0) continue;
                            BlockPos candidate = searchCenter.offset(x, dy, z);
                            if (candidate.getY() < minY || candidate.getY() > maxY) continue;
                            if (isValidGravePos(level, candidate)) return candidate;
                        }
                    }
                }
            }
        }

        // Destructive mode: replace non-bedrock blocks
        if (OpenBlocksConfig.Graves.destructiveGraves) {
            for (int r = 0; r <= range; r++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos candidate = searchCenter.offset(x, 0, z);
                        if (candidate.getY() >= minY && candidate.getY() <= maxY) {
                            BlockState existing = level.getBlockState(candidate);
                            if (existing.getDestroySpeed(level, candidate) >= 0) {
                                return candidate;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isValidGravePos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
