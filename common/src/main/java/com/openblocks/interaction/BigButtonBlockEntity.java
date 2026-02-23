package com.openblocks.interaction;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Stores an 8-slot inventory that determines how long the big button
 * stays pressed. Total item count across all slots = tick duration (min 1).
 */
public class BigButtonBlockEntity extends OpenBlocksBlockEntity {

    private final NonNullList<ItemStack> items = NonNullList.withSize(8, ItemStack.EMPTY);

    public BigButtonBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.BIG_BUTTON.get(), pos, state);
    }

    public int getTickTime() {
        int total = 0;
        for (ItemStack stack : items) {
            total += stack.getCount();
        }
        return Math.max(1, total);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Tick time: " + getTickTime());
        return info;
    }
}
