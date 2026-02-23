package com.openblocks.tank;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.FluidXpUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * XP Bottler block entity. Takes glass bottles as input and produces
 * bottles o' enchanting by consuming liquid XP from the tank below.
 * <p>
 * Slot 0: Input (glass bottles)
 * Slot 1: Output (experience bottles)
 */
public class XpBottlerBlockEntity extends OpenBlocksBlockEntity {

    private static final int PROCESS_TIME = 40;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress = 0;

    public XpBottlerBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.XP_BOTTLER.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, XpBottlerBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.canProcess()) {
            be.progress++;
            if (be.progress >= PROCESS_TIME) {
                be.progress = 0;
                be.processItem();
            }
            be.setChanged();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.setChanged();
            }
        }
    }

    private boolean canProcess() {
        // Need glass bottle in input
        ItemStack input = items.get(INPUT_SLOT);
        if (!input.is(Items.GLASS_BOTTLE) || input.isEmpty()) return false;

        // Need space in output
        ItemStack output = items.get(OUTPUT_SLOT);
        if (!output.isEmpty()) {
            if (!output.is(Items.EXPERIENCE_BOTTLE)) return false;
            if (output.getCount() >= output.getMaxStackSize()) return false;
        }

        // Need XP fluid in tank below
        TankBlockEntity tankBelow = getTankBelow();
        if (tankBelow == null || tankBelow.isEmpty()) return false;

        long fluidNeeded = FluidXpUtils.getBottleFluidAmount();
        return tankBelow.drain(fluidNeeded, true) >= fluidNeeded;
    }

    private void processItem() {
        TankBlockEntity tankBelow = getTankBelow();
        if (tankBelow == null) return;

        long fluidNeeded = FluidXpUtils.getBottleFluidAmount();
        long drained = tankBelow.drain(fluidNeeded, false);
        if (drained < fluidNeeded) return;

        // Consume glass bottle
        items.get(INPUT_SLOT).shrink(1);

        // Produce experience bottle
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, new ItemStack(Items.EXPERIENCE_BOTTLE));
        } else {
            output.grow(1);
        }

        setChanged();
    }

    private TankBlockEntity getTankBelow() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return be instanceof TankBlockEntity tank ? tank : null;
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
    }

    // --- Debug ---

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Input: " + items.get(INPUT_SLOT));
        info.add("Output: " + items.get(OUTPUT_SLOT));
        info.add("Progress: " + progress + "/" + PROCESS_TIME);
        info.add("Tank below: " + (getTankBelow() != null ? "yes" : "no"));
        return info;
    }
}
