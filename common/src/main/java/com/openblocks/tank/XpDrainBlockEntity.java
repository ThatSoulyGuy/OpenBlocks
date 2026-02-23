package com.openblocks.tank;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.FluidXpUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * XP Drain block entity. Scans for XP orbs and sneaking players above it,
 * converts XP to liquid XP and stores it in the tank below.
 */
public class XpDrainBlockEntity extends OpenBlocksBlockEntity {

    private static final int SCAN_INTERVAL = 5;
    private static final int MAX_XP_PER_TICK = 4;
    private static final double SCAN_RANGE = 2.0;

    private int tickCounter = 0;

    public XpDrainBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.XP_DRAIN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, XpDrainBlockEntity be) {
        if (level.isClientSide()) return;

        be.tickCounter++;
        if (be.tickCounter < SCAN_INTERVAL) return;
        be.tickCounter = 0;

        TankBlockEntity tankBelow = be.getTankBelow();
        if (tankBelow == null) return;

        // Collect XP orbs
        AABB scanBox = new AABB(pos).inflate(SCAN_RANGE, SCAN_RANGE + 1, SCAN_RANGE);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, scanBox);

        for (ExperienceOrb orb : orbs) {
            if (orb.isRemoved()) continue;

            int xpValue = orb.getValue();
            long fluidAmount = FluidXpUtils.xpToFluid(xpValue);

            // Try to store in tank below (use Fluids.WATER as placeholder for XP fluid)
            // TODO: Replace with XP juice fluid when registered
            long filled = tankBelow.fill(Fluids.WATER, fluidAmount, true);
            if (filled >= fluidAmount) {
                tankBelow.fill(Fluids.WATER, fluidAmount, false);
                orb.discard();
            }
        }

        // Drain XP from sneaking players
        List<Player> players = level.getEntitiesOfClass(Player.class, scanBox);
        for (Player player : players) {
            if (!player.isShiftKeyDown()) continue;
            if (!(player instanceof ServerPlayer serverPlayer)) continue;

            int playerXp = getPlayerTotalXp(serverPlayer);
            if (playerXp <= 0) continue;

            int toDrain = Math.min(playerXp, MAX_XP_PER_TICK);
            long fluidAmount = FluidXpUtils.xpToFluid(toDrain);

            long filled = tankBelow.fill(Fluids.WATER, fluidAmount, true);
            if (filled > 0) {
                int actualXp = FluidXpUtils.fluidToXp(filled);
                if (actualXp > 0) {
                    tankBelow.fill(Fluids.WATER, FluidXpUtils.xpToFluid(actualXp), false);
                    addPlayerXp(serverPlayer, -actualXp);
                }
            }
        }
    }

    private TankBlockEntity getTankBelow() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return be instanceof TankBlockEntity tank ? tank : null;
    }

    private static int getPlayerTotalXp(ServerPlayer player) {
        int level = player.experienceLevel;
        int total = 0;
        if (level >= 30) {
            total = (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 15) {
            total = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            total = level * level + 6 * level;
        }
        total += (int) (player.experienceProgress * player.getXpNeededForNextLevel());
        return total;
    }

    private static void addPlayerXp(ServerPlayer player, int amount) {
        player.giveExperiencePoints(amount);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Tank below: " + (getTankBelow() != null ? "yes" : "no"));
        return info;
    }
}
