package com.openblocks.grave;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Grave block entity. Stores the dead player's name, death message,
 * and all their inventory items. Optionally spawns hostile mobs nearby.
 */
public class GraveBlockEntity extends OpenBlocksBlockEntity {

    private String playerName = "";
    private String deathMessageText = "";
    private final List<ItemStack> storedItems = new ArrayList<>();

    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.GRAVE.get(), pos, state);
    }

    // --- Accessors ---

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name != null ? name : "";
        setChanged();
    }

    public Component getDeathMessage() {
        if (deathMessageText.isEmpty()) return null;
        return Component.literal(deathMessageText);
    }

    public void setDeathMessage(Component message) {
        this.deathMessageText = message != null ? message.getString() : "";
        setChanged();
    }

    public void storeItems(List<ItemStack> items) {
        storedItems.clear();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                storedItems.add(item.copy());
            }
        }
        sync();
    }

    public void dropAllItems() {
        if (level == null || level.isClientSide()) return;
        for (ItemStack item : storedItems) {
            if (!item.isEmpty()) {
                Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        item);
            }
        }
        storedItems.clear();
        setChanged();
    }

    // --- Skeleton Spawning ---

    public static void tick(Level level, BlockPos pos, BlockState state, GraveBlockEntity be) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        if (level.random.nextDouble() >= OpenBlocksConfig.Graves.skeletonSpawnRate) return;

        // Check not too many mobs nearby
        long nearbyMobs = level.getEntitiesOfClass(
                net.minecraft.world.entity.monster.Monster.class,
                new net.minecraft.world.phys.AABB(pos).inflate(7)).size();
        if (nearbyMobs >= 5) return;

        // Try to spawn skeleton
        double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 3;
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 3;

        Skeleton skeleton = EntityType.SKELETON.create(level);
        if (skeleton != null) {
            skeleton.moveTo(x, y, z, level.random.nextFloat() * 360f, 0f);
            skeleton.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(pos),
                    MobSpawnType.SPAWNER, null);
            level.addFreshEntity(skeleton);
        }
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("PlayerName", playerName);
        tag.putString("DeathMessage", deathMessageText);

        ListTag itemsList = new ListTag();
        for (ItemStack item : storedItems) {
            if (!item.isEmpty()) {
                itemsList.add(item.saveOptional(registries));
            }
        }
        tag.put("GraveItems", itemsList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        playerName = tag.getString("PlayerName");
        deathMessageText = tag.getString("DeathMessage");

        storedItems.clear();
        ListTag itemsList = tag.getList("GraveItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(registries, itemsList.getCompound(i));
            if (!stack.isEmpty()) {
                storedItems.add(stack);
            }
        }
    }

    // --- Debug ---

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Player: " + playerName);
        info.add("Items: " + storedItems.size());
        info.add("Message: " + deathMessageText);
        return info;
    }
}
