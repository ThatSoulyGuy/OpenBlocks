package com.openblocks.goldenegg;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.entity.MiniMeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Block entity for the golden egg with state-machine hatching animation.
 * Goes through INERT -> ROTATING_SLOW -> ROTATING_MEDIUM -> ROTATING_FAST
 * -> FLOATING -> FALLING -> EXPLODING, spawning a MiniMe at the end.
 */
public class GoldenEggBlockEntity extends OpenBlocksBlockEntity {

    private static final float SPEED_CHANGE_RATE = 0.1f;
    private static final int STAGE_CHANGE_TICK = 100;
    private static final int RISING_TIME = 400;
    private static final int FALLING_TIME = 10;
    public static final int MAX_HEIGHT = 5;
    private static final double STAGE_CHANGE_CHANCE = 0.8;

    private final SyncedValue<GoldenEggState> state = syncedEnum("state", GoldenEggState.INERT);
    private int tickCounter = 0;
    private UUID ownerUuid;

    // Client-side animation values
    private float rotation;
    private float progress;
    private float rotationSpeed;
    private float progressSpeed;

    public GoldenEggBlockEntity(BlockPos pos, BlockState blockState) {
        super(OpenBlocksBlockEntities.GOLDEN_EGG.get(), pos, blockState);
    }

    public GoldenEggState getState() {
        return state.get();
    }

    public float getRotation(float partialTick) {
        return rotation + rotationSpeed * partialTick;
    }

    public float getOffset(float partialTick) {
        return (progress + progressSpeed * partialTick) * MAX_HEIGHT;
    }

    public void setPlacedBy(LivingEntity placer) {
        if (placer instanceof net.minecraft.world.entity.player.Player player) {
            this.ownerUuid = player.getUUID();
        }
    }

    // --- Ticking ---

    public static void serverTick(Level level, BlockPos pos, BlockState blockState, GoldenEggBlockEntity be) {
        GoldenEggState currentState = be.state.get();

        switch (currentState) {
            case INERT, ROTATING_SLOW, ROTATING_MEDIUM, ROTATING_FAST -> {
                be.tickCounter++;
                if (be.tickCounter % STAGE_CHANGE_TICK == 0 && level.random.nextDouble() < STAGE_CHANGE_CHANCE) {
                    GoldenEggState next = switch (currentState) {
                        case INERT -> GoldenEggState.ROTATING_SLOW;
                        case ROTATING_SLOW -> GoldenEggState.ROTATING_MEDIUM;
                        case ROTATING_MEDIUM -> GoldenEggState.ROTATING_FAST;
                        case ROTATING_FAST -> GoldenEggState.FLOATING;
                        default -> null;
                    };
                    if (next != null) {
                        be.state.set(next);
                        if (next == GoldenEggState.FLOATING) {
                            be.tickCounter = RISING_TIME;
                        }
                        be.sync();
                    }
                }
            }
            case FLOATING -> {
                be.tickCounter--;
                if (be.tickCounter <= 0) {
                    be.state.set(GoldenEggState.FALLING);
                    be.tickCounter = FALLING_TIME;
                    be.sync();
                }
            }
            case FALLING -> {
                be.tickCounter--;
                if (be.tickCounter <= 0) {
                    be.state.set(GoldenEggState.EXPLODING);
                    be.explode();
                    be.sync();
                }
            }
            case EXPLODING -> {
                // Already exploded
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState blockState, GoldenEggBlockEntity be) {
        GoldenEggState currentState = be.state.get();

        be.rotationSpeed = (1 - SPEED_CHANGE_RATE) * be.rotationSpeed + SPEED_CHANGE_RATE * currentState.rotationSpeed;
        be.rotation += be.rotationSpeed;

        be.progressSpeed = (1 - SPEED_CHANGE_RATE) * be.progressSpeed + SPEED_CHANGE_RATE * currentState.progressSpeed;
        be.progress += be.progressSpeed;
    }

    private void explode() {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = getBlockPos();
            level.removeBlock(pos, false);
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    2.0f, Level.ExplosionInteraction.NONE);

            // Spawn MiniMe
            UUID spawnUuid = ownerUuid != null ? ownerUuid
                    : UUID.fromString("d4d119aa-d410-488a-8734-0053577d4a1a");
            MiniMeEntity miniMe = new MiniMeEntity(
                    com.openblocks.core.registry.OpenBlocksEntities.MINI_ME.get(), serverLevel);
            miniMe.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            miniMe.setOwnerUUID(spawnUuid);
            serverLevel.addFreshEntity(miniMe);
        }
    }

    // --- Persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TickCounter", tickCounter);
        if (ownerUuid != null) {
            tag.putUUID("OwnerUUID", ownerUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("TickCounter");
        if (tag.hasUUID("OwnerUUID")) {
            ownerUuid = tag.getUUID("OwnerUUID");
        }
    }
}
