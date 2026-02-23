package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Sprinkler block entity. Fertilizes crops in the surrounding area.
 * Stores a 9-slot inventory for bonemeal and tracks water consumption.
 */
public class SprinklerBlockEntity extends OpenBlocksBlockEntity {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
    private final SyncedValue<Integer> waterStored = syncedInt("water", 0);
    private int tickCounter = 0;

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.SPRINKLER.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;
        tickCounter++;

        // Try to consume water from below (simulate tank interaction)
        if (tickCounter % OpenBlocksConfig.Sprinkler.waterConsumeRate == 0) {
            // For now, check if rain or water source block below
            if (level.isRainingAt(worldPosition.above())) {
                waterStored.set(Math.min(waterStored.get() + 1, OpenBlocksConfig.Sprinkler.internalTankCapacity));
            }
        }

        // Fertilize nearby crops
        if (waterStored.get() > 0 || level.isRainingAt(worldPosition.above())) {
            int range = OpenBlocksConfig.Sprinkler.effectiveRange;
            boolean hasBonemeal = hasBonemeal();
            int chance = hasBonemeal ? OpenBlocksConfig.Sprinkler.bonemealFertilizeChance
                                     : OpenBlocksConfig.Sprinkler.fertilizeChance;

            if (level.random.nextInt(chance) == 0) {
                // Pick a random position in range
                int dx = level.random.nextInt(range * 2 + 1) - range;
                int dz = level.random.nextInt(range * 2 + 1) - range;
                BlockPos cropPos = worldPosition.offset(dx, -1, dz);

                // Search down a few blocks for crop
                for (int dy = 0; dy < 3; dy++) {
                    BlockPos checkPos = cropPos.below(dy);
                    BlockState cropState = level.getBlockState(checkPos);
                    Block cropBlock = cropState.getBlock();

                    if (cropBlock instanceof BonemealableBlock bonemealable) {
                        if (bonemealable.isValidBonemealTarget(level, checkPos, cropState)) {
                            if (level instanceof ServerLevel serverLevel) {
                                if (bonemealable.isBonemealSuccess(level, level.random, checkPos, cropState)) {
                                    bonemealable.performBonemeal(serverLevel, level.random, checkPos, cropState);
                                }
                            }

                            // Consume bonemeal item
                            if (hasBonemeal && tickCounter % OpenBlocksConfig.Sprinkler.bonemealConsumeRate == 0) {
                                consumeBonemeal();
                            }
                            break;
                        }
                    }
                }

                // Consume water
                if (waterStored.get() > 0) {
                    waterStored.set(waterStored.get() - 1);
                }
            }
        }
    }

    private boolean hasBonemeal() {
        for (ItemStack stack : inventory) {
            if (stack.is(Items.BONE_MEAL) && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void consumeBonemeal() {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (stack.is(Items.BONE_MEAL) && !stack.isEmpty()) {
                stack.shrink(1);
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, inventory, registries);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Water: " + waterStored.get() + "/" + OpenBlocksConfig.Sprinkler.internalTankCapacity);
        int bonemealCount = 0;
        for (ItemStack stack : inventory) {
            if (stack.is(Items.BONE_MEAL)) bonemealCount += stack.getCount();
        }
        info.add("Bonemeal: " + bonemealCount);
        return info;
    }
}
