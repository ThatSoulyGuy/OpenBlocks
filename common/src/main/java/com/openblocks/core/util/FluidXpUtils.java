package com.openblocks.core.util;

import com.openblocks.core.config.OpenBlocksConfig;
import dev.architectury.fluid.FluidStack;

/**
 * Utility for converting between fluid amounts and XP points.
 * Handles platform-specific fluid unit scaling (Fabric uses 81000/bucket, NeoForge uses 1000/bucket).
 */
public final class FluidXpUtils {

    public static final int XP_PER_BOTTLE = 8;

    private FluidXpUtils() {}

    /**
     * Convert fluid amount (in platform units) to XP points.
     */
    public static int fluidToXp(long fluidAmount) {
        long unitsPerMB = FluidStack.bucketAmount() / 1000;
        int ratio = OpenBlocksConfig.Features.xpToLiquidRatio;
        return (int) (fluidAmount / (ratio * unitsPerMB));
    }

    /**
     * Convert XP points to fluid amount (in platform units).
     */
    public static long xpToFluid(int xp) {
        long unitsPerMB = FluidStack.bucketAmount() / 1000;
        int ratio = OpenBlocksConfig.Features.xpToLiquidRatio;
        return (long) xp * ratio * unitsPerMB;
    }

    /**
     * Get the fluid amount for a single XP bottle (8 XP).
     */
    public static long getBottleFluidAmount() {
        return xpToFluid(XP_PER_BOTTLE);
    }
}
