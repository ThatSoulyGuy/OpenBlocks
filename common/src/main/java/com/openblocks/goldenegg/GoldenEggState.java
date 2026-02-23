package com.openblocks.goldenegg;

import net.minecraft.util.StringRepresentable;

/**
 * State machine for the golden egg hatching process.
 */
public enum GoldenEggState implements StringRepresentable {
    INERT("inert", 0, 0, false),
    ROTATING_SLOW("rotating_slow", 1, 0, false),
    ROTATING_MEDIUM("rotating_medium", 10, 0, false),
    ROTATING_FAST("rotating_fast", 50, 0, false),
    FLOATING("floating", 100, 1.0f / 400, true),
    FALLING("falling", 150, -1.0f / 10, true),
    EXPLODING("exploding", 666, 0, true);

    private final String name;
    public final float rotationSpeed;
    public final float progressSpeed;
    public final boolean specialEffects;

    GoldenEggState(String name, float rotationSpeed, float progressSpeed, boolean specialEffects) {
        this.name = name;
        this.rotationSpeed = rotationSpeed;
        this.progressSpeed = progressSpeed;
        this.specialEffects = specialEffects;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
