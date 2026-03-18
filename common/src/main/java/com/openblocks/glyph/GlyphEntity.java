package com.openblocks.glyph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class GlyphEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_CHAR_INDEX =
            SynchedEntityData.defineId(GlyphEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_OFFSET_X =
            SynchedEntityData.defineId(GlyphEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_OFFSET_Y =
            SynchedEntityData.defineId(GlyphEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_DIRECTION =
            SynchedEntityData.defineId(GlyphEntity.class, EntityDataSerializers.BYTE);

    private static final double DEPTH = 0.5;

    private BlockPos hangingPos = BlockPos.ZERO;

    public GlyphEntity(EntityType<? extends GlyphEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CHAR_INDEX, 0);
        builder.define(DATA_OFFSET_X, (byte) 8);
        builder.define(DATA_OFFSET_Y, (byte) 8);
        builder.define(DATA_DIRECTION, (byte) Direction.NORTH.ordinal());
    }

    public void setup(BlockPos pos, Direction facing, int charIndex, byte offsetX, byte offsetY) {
        this.hangingPos = pos;
        entityData.set(DATA_CHAR_INDEX, charIndex);
        entityData.set(DATA_OFFSET_X, offsetX);
        entityData.set(DATA_OFFSET_Y, offsetY);
        entityData.set(DATA_DIRECTION, (byte) facing.ordinal());
        setYRot(directionToYaw(facing));
        recalculatePosition();
    }

    private static float directionToYaw(Direction dir) {
        return switch (dir) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
    }

    private void recalculatePosition() {
        Direction normal = getDirection();
        if (normal.getAxis().isVertical()) return;

        double centerX = hangingPos.getX() + 0.5;
        double centerY = hangingPos.getY() + 0.5;
        double centerZ = hangingPos.getZ() + 0.5;

        // Move toward wall
        centerX -= normal.getStepX() * (16.0 - DEPTH) / 16.0 / 2.0;
        centerZ -= normal.getStepZ() * (16.0 - DEPTH) / 16.0 / 2.0;

        // Apply offsets
        byte ox = getOffsetX();
        byte oy = getOffsetY();
        Direction left = normal.getClockWise();

        centerY += (oy - 8) / 16.0;
        if (left.getAxis() == Direction.Axis.Z) {
            centerZ += (ox - 8) / 16.0;
        } else {
            centerX += (ox - 8) / 16.0;
        }

        // Set position without resetting the bounding box (we set it manually)
        setPosRaw(centerX, centerY, centerZ);

        // Calculate bounding box (8px x 8px x 0.5px depth)
        double halfW = 8.0 / 16.0 / 2.0; // 0.25
        double halfH = 8.0 / 16.0 / 2.0; // 0.25
        double halfD = DEPTH / 16.0 / 2.0;

        double halfSizeX, halfSizeZ;
        if (normal.getAxis() == Direction.Axis.Z) {
            halfSizeX = halfW;
            halfSizeZ = halfD;
        } else {
            halfSizeX = halfD;
            halfSizeZ = halfW;
        }

        setBoundingBox(new AABB(
                centerX - halfSizeX, centerY - halfH, centerZ - halfSizeZ,
                centerX + halfSizeX, centerY + halfH, centerZ + halfSizeZ));
    }

    @Override
    public void tick() {
        // No super.tick() — static entity, no physics/movement
        if (!level().isClientSide() && tickCount % 20 == 0) {
            if (!checkSurface()) {
                dropItem(null);
                discard();
            }
        }
    }

    public boolean checkSurface() {
        Direction normal = getDirection();
        BlockPos behind = hangingPos.relative(normal.getOpposite());
        return level().getBlockState(behind).isFaceSturdy(level(), behind, normal);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide()) {
            dropItem(source.getEntity());
            playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);
            discard();
        }
        return true;
    }

    private void dropItem(Entity attacker) {
        if (!level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) return;
        if (attacker instanceof Player player && player.getAbilities().instabuild) return;
        spawnAtLocation(GlyphItem.createGlyph(getCharIndex()));
    }

    // --- Getters ---

    public Direction getDirection() {
        int ordinal = entityData.get(DATA_DIRECTION);
        Direction[] values = Direction.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : Direction.NORTH;
    }

    public int getCharIndex() {
        return entityData.get(DATA_CHAR_INDEX);
    }

    public byte getOffsetX() {
        return entityData.get(DATA_OFFSET_X);
    }

    public byte getOffsetY() {
        return entityData.get(DATA_OFFSET_Y);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return GlyphItem.createGlyph(getCharIndex());
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player player) {
            return hurt(damageSources().playerAttack(player), 0);
        }
        return false;
    }

    // --- NBT ---

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_CHAR_INDEX, tag.getInt("CharIndex"));
        entityData.set(DATA_OFFSET_X, tag.getByte("OffsetX"));
        entityData.set(DATA_OFFSET_Y, tag.getByte("OffsetY"));
        entityData.set(DATA_DIRECTION, tag.getByte("Direction"));
        hangingPos = new BlockPos(tag.getInt("HangX"), tag.getInt("HangY"), tag.getInt("HangZ"));
        setYRot(directionToYaw(getDirection()));
        recalculatePosition();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("CharIndex", getCharIndex());
        tag.putByte("OffsetX", getOffsetX());
        tag.putByte("OffsetY", getOffsetY());
        tag.putByte("Direction", entityData.get(DATA_DIRECTION));
        tag.putInt("HangX", hangingPos.getX());
        tag.putInt("HangY", hangingPos.getY());
        tag.putInt("HangZ", hangingPos.getZ());
    }
}
