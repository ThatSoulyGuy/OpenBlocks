package com.openblocks.elevator;

/**
 * Represents a player rotation applied after elevator teleportation.
 * Used by the rotating elevator variant to force player facing.
 */
public enum PlayerRotation {
    NONE(-1),
    NORTH(180.0f),
    SOUTH(0.0f),
    EAST(-90.0f),
    WEST(90.0f);

    private final float yRot;

    PlayerRotation(float yRot) {
        this.yRot = yRot;
    }

    public float getYRot() {
        return yRot;
    }

    public boolean shouldRotate() {
        return this != NONE;
    }
}
