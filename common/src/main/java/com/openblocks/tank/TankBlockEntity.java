package com.openblocks.tank;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Tank block entity. Stores fluid, balances with horizontal neighbors,
 * fills downward via gravity, and supports column fill/drain operations.
 */
public class TankBlockEntity extends OpenBlocksBlockEntity {

    private Fluid fluidType = Fluids.EMPTY;
    private long amount = 0;

    private static final int SYNC_THRESHOLD = 8;
    private static final int UPDATE_THRESHOLD = 20;

    private int ticksSinceLastSync;
    private int ticksSinceLastUpdate;
    private long lastSyncedAmount = 0;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.TANK.get(), pos, state);
        int hash = Math.abs(pos.hashCode());
        this.ticksSinceLastSync = hash % SYNC_THRESHOLD;
        this.ticksSinceLastUpdate = hash % UPDATE_THRESHOLD;
    }

    // --- Capacity ---

    public long getCapacity() {
        return (long) OpenBlocksConfig.Tanks.bucketsPerTank * FluidStack.bucketAmount();
    }

    // --- Fluid Access ---

    public Fluid getFluidType() {
        return fluidType;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isEmpty() {
        return amount <= 0 || fluidType == Fluids.EMPTY;
    }

    public float getFillRatio() {
        long cap = getCapacity();
        return cap > 0 ? (float) amount / cap : 0f;
    }

    public FluidStack getFluidStack() {
        if (isEmpty()) return FluidStack.empty();
        return FluidStack.create(fluidType, amount);
    }

    public boolean accepts(Fluid fluid) {
        return fluid != Fluids.EMPTY && (isEmpty() || fluidType == fluid);
    }

    // --- Fill / Drain ---

    /**
     * Fill this tank with fluid.
     * @return amount actually filled
     */
    public long fill(Fluid fluid, long maxFill, boolean simulate) {
        if (fluid == Fluids.EMPTY || maxFill <= 0) return 0;
        if (!accepts(fluid)) return 0;

        long space = getCapacity() - amount;
        long toFill = Math.min(maxFill, space);

        if (!simulate && toFill > 0) {
            if (isEmpty()) fluidType = fluid;
            amount += toFill;
            markDirtyAndSync();
        }

        return toFill;
    }

    /**
     * Drain fluid from this tank.
     * @return amount actually drained
     */
    public long drain(long maxDrain, boolean simulate) {
        if (isEmpty() || maxDrain <= 0) return 0;

        long toDrain = Math.min(maxDrain, amount);

        if (!simulate && toDrain > 0) {
            amount -= toDrain;
            if (amount <= 0) {
                amount = 0;
                fluidType = Fluids.EMPTY;
            }
            markDirtyAndSync();
        }

        return toDrain;
    }

    /**
     * Set fluid directly without triggering network sync. Used by balancing algorithm.
     * Caller is responsible for marking dirty/syncing.
     */
    void setFluidDirect(Fluid fluid, long newAmount) {
        this.fluidType = fluid;
        this.amount = newAmount;
        if (this.amount <= 0) {
            this.amount = 0;
            this.fluidType = Fluids.EMPTY;
        }
        setChanged();
    }

    // --- Column Operations ---

    /**
     * Fill this tank, overflow goes to the tank above (recursively).
     */
    public long fillColumn(Fluid fluid, long maxFill, boolean simulate) {
        long filled = fill(fluid, maxFill, simulate);
        long remaining = maxFill - filled;

        if (remaining > 0) {
            TankBlockEntity above = getNeighborTank(Direction.UP);
            if (above != null && above.accepts(fluid)) {
                filled += above.fillColumn(fluid, remaining, simulate);
            }
        }

        return filled;
    }

    /**
     * Drain from the column: drains from top tanks first, then this one.
     */
    public long drainFromColumn(long maxDrain, boolean simulate) {
        TankBlockEntity above = getNeighborTank(Direction.UP);
        long drained = 0;

        if (above != null && !above.isEmpty()) {
            drained = above.drainFromColumn(maxDrain, simulate);
        }

        long remaining = maxDrain - drained;
        if (remaining > 0) {
            drained += drain(remaining, simulate);
        }

        return drained;
    }

    // --- Neighbor Operations ---

    private TankBlockEntity getNeighborTank(Direction dir) {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
        return be instanceof TankBlockEntity tank ? tank : null;
    }

    // --- Gravity Fill ---

    private void tryFillBottomTank() {
        if (isEmpty()) return;

        TankBlockEntity below = getNeighborTank(Direction.DOWN);
        if (below == null || !below.accepts(fluidType)) return;

        long belowSpace = below.getCapacity() - below.amount;
        if (belowSpace <= 0) return;

        long toTransfer = Math.min(amount, belowSpace);
        if (toTransfer > 0) {
            Fluid fluid = fluidType;
            below.setFluidDirect(fluid, below.amount + toTransfer);
            setFluidDirect(fluid, amount - toTransfer);
        }
    }

    // --- Horizontal Balancing ---

    private void tryBalanceNeighbors() {
        if (isEmpty()) return;

        List<TankBlockEntity> neighbors = new ArrayList<>();
        long totalFluid = amount;

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            TankBlockEntity neighbor = getNeighborTank(dir);
            if (neighbor != null && neighbor.accepts(fluidType)) {
                neighbors.add(neighbor);
                totalFluid += neighbor.amount;
            }
        }

        if (neighbors.isEmpty()) return;

        int count = neighbors.size() + 1;
        long target = totalFluid / count;
        long remainder = totalFluid % count;

        int threshold = OpenBlocksConfig.Tanks.fluidDifferenceUpdateThreshold;
        if (threshold > 0) {
            boolean needsUpdate = Math.abs(amount - target) > threshold;
            if (!needsUpdate) {
                for (TankBlockEntity neighbor : neighbors) {
                    if (Math.abs(neighbor.amount - target) > threshold) {
                        needsUpdate = true;
                        break;
                    }
                }
            }
            if (!needsUpdate) return;
        }

        Fluid fluid = fluidType;
        setFluidDirect(fluid, target + remainder);
        for (TankBlockEntity neighbor : neighbors) {
            neighbor.setFluidDirect(fluid, target);
        }
    }

    // --- Server Tick ---

    public static void tick(Level level, BlockPos pos, BlockState state, TankBlockEntity be) {
        if (level.isClientSide()) return;
        if (!OpenBlocksConfig.Tanks.shouldUpdate) return;

        be.ticksSinceLastUpdate++;
        be.ticksSinceLastSync++;

        if (be.ticksSinceLastUpdate >= UPDATE_THRESHOLD) {
            be.ticksSinceLastUpdate = 0;
            be.tryFillBottomTank();
            be.tryBalanceNeighbors();
        }

        if (be.ticksSinceLastSync >= SYNC_THRESHOLD) {
            if (be.amount != be.lastSyncedAmount) {
                be.lastSyncedAmount = be.amount;
                be.sync();
            }
            be.ticksSinceLastSync = 0;
        }
    }

    // --- Sync ---

    private void markDirtyAndSync() {
        setChanged();
        sync();
        lastSyncedAmount = amount;
    }

    // --- Comparator ---

    public int getComparatorOutput() {
        if (isEmpty()) return 0;
        return Math.max(1, (int) (getFillRatio() * 15));
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!isEmpty()) {
            CompoundTag tankTag = new CompoundTag();
            tankTag.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluidType).toString());
            tankTag.putLong("Amount", amount);
            tag.put("Tank", tankTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) {
            CompoundTag tankTag = tag.getCompound("Tank");
            String fluidName = tankTag.getString("FluidName");
            ResourceLocation fluidId = ResourceLocation.tryParse(fluidName);
            if (fluidId != null) {
                fluidType = BuiltInRegistries.FLUID.get(fluidId);
            } else {
                fluidType = Fluids.EMPTY;
            }
            amount = tankTag.getLong("Amount");
            if (fluidType == Fluids.EMPTY) amount = 0;
        } else {
            fluidType = Fluids.EMPTY;
            amount = 0;
        }
    }

    // --- Debug ---

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Fluid: " + (isEmpty() ? "empty" : BuiltInRegistries.FLUID.getKey(fluidType)));
        info.add("Amount: " + amount + " / " + getCapacity());
        info.add("Fill: " + String.format("%.1f%%", getFillRatio() * 100));
        info.add("Comparator: " + getComparatorOutput());
        return info;
    }
}
