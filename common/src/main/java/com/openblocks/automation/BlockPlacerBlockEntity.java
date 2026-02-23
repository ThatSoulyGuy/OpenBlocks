package com.openblocks.automation;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Block Placer block entity. 9-slot inventory, places blocks in front when powered.
 */
public class BlockPlacerBlockEntity extends OpenBlocksBlockEntity implements Container, MenuProvider {

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private boolean wasPowered = false;

    public BlockPlacerBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.BLOCK_PLACER.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockPlacerBlockEntity be) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        boolean powered = state.getValue(BlockPlacerBlock.POWERED);

        if (powered && !be.wasPowered) {
            be.placeBlock(serverLevel, state);
        }
        be.wasPowered = powered;
    }

    private void placeBlock(ServerLevel serverLevel, BlockState selfState) {
        Direction facing = selfState.getValue(BlockPlacerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        BlockState targetState = serverLevel.getBlockState(targetPos);

        if (!targetState.canBeReplaced()) return;

        // Find first non-empty block item in inventory
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem blockItem)) continue;

            // Try to place
            DirectionalPlaceContext ctx = new DirectionalPlaceContext(
                    serverLevel, targetPos, facing, stack, facing.getOpposite());
            BlockState placedState = blockItem.getBlock().getStateForPlacement(ctx);
            if (placedState != null) {
                serverLevel.setBlock(targetPos, placedState, 3);
                stack.shrink(1);
                setChanged();
                serverLevel.playSound(null, worldPosition,
                        SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, 0.7f);
                return;
            }
        }
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.openblocks.block_placer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BlockPlacerMenu(containerId, playerInventory, this);
    }

    // --- NBT ---

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
        int nonEmpty = (int) items.stream().filter(s -> !s.isEmpty()).count();
        info.add("Items: " + nonEmpty + "/9");
        info.add("Powered: " + wasPowered);
        return info;
    }
}
