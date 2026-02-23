package com.openblocks.tank;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.FluidXpUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * XP Shower block entity. When powered by redstone, drains liquid XP
 * from the tank above and spawns XP orbs below.
 */
public class XpShowerBlockEntity extends OpenBlocksBlockEntity {

    private static final int SPAWN_INTERVAL = 3;
    private static final int XP_PER_SPAWN = 1;

    private int tickCounter = 0;

    public XpShowerBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.XP_SHOWER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, XpShowerBlockEntity be) {
        if (level.isClientSide()) return;

        // Only works when powered
        if (!level.hasNeighborSignal(pos)) return;

        be.tickCounter++;
        if (be.tickCounter < SPAWN_INTERVAL) return;
        be.tickCounter = 0;

        TankBlockEntity tankAbove = be.getTankAbove();
        if (tankAbove == null || tankAbove.isEmpty()) return;

        long fluidNeeded = FluidXpUtils.xpToFluid(XP_PER_SPAWN);
        long drained = tankAbove.drain(fluidNeeded, true);
        if (drained >= fluidNeeded) {
            tankAbove.drain(fluidNeeded, false);

            // Spawn XP orb below
            double x = pos.getX() + 0.5;
            double y = pos.getY() - 0.25;
            double z = pos.getZ() + 0.5;
            ExperienceOrb orb = new ExperienceOrb(level, x, y, z, XP_PER_SPAWN);
            level.addFreshEntity(orb);
        }
    }

    private TankBlockEntity getTankAbove() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.above());
        return be instanceof TankBlockEntity tank ? tank : null;
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Powered: " + (level != null && level.hasNeighborSignal(worldPosition)));
        info.add("Tank above: " + (getTankAbove() != null ? "yes" : "no"));
        return info;
    }
}
