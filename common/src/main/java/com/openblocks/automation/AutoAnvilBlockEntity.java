package com.openblocks.automation;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.FluidXpUtils;
import com.openblocks.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Auto Anvil block entity. Automatically repairs/combines tool + modifier items
 * using XP fluid from a tank below.
 *
 * Slot 0: Tool (item to repair/enchant)
 * Slot 1: Modifier (repair material or enchanted book)
 * Slot 2: Output
 */
public class AutoAnvilBlockEntity extends OpenBlocksBlockEntity implements Container, MenuProvider {

    private static final int TOOL_SLOT = 0;
    private static final int MODIFIER_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;
    private static final int COOLDOWN_TICKS = 40;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int cooldown = 0;

    public AutoAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.AUTO_ANVIL.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AutoAnvilBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.cooldown > 0) {
            be.cooldown--;
            return;
        }

        if (!be.items.get(OUTPUT_SLOT).isEmpty()) return;
        if (be.items.get(TOOL_SLOT).isEmpty()) return;
        if (be.items.get(MODIFIER_SLOT).isEmpty()) return;

        be.tryRepair(level);
    }

    private void tryRepair(Level level) {
        ItemStack tool = items.get(TOOL_SLOT);
        ItemStack modifier = items.get(MODIFIER_SLOT);

        // Simple repair: same item type repairs durability
        if (tool.isDamaged() && tool.getItem() == modifier.getItem()) {
            int repairAmount = Math.min(tool.getDamageValue(),
                    tool.getMaxDamage() / 4);

            // Cost: 1 XP level equivalent per repair
            int xpCost = 5;
            TankBlockEntity tank = getTankBelow();
            if (tank == null) return;

            long fluidCost = FluidXpUtils.xpToFluid(xpCost);
            if (tank.drain(fluidCost, true) < fluidCost) return;

            // Perform repair
            ItemStack result = tool.copy();
            result.setDamageValue(Math.max(0, result.getDamageValue() - repairAmount));

            tank.drain(fluidCost, false);
            items.set(TOOL_SLOT, ItemStack.EMPTY);
            modifier.shrink(1);
            items.set(OUTPUT_SLOT, result);

            cooldown = COOLDOWN_TICKS;
            setChanged();

            level.playSound(null, worldPosition,
                    SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 1.0f);
            return;
        }

        // Enchanted book application
        if (modifier.is(Items.ENCHANTED_BOOK)) {
            int xpCost = 10;
            TankBlockEntity tank = getTankBelow();
            if (tank == null) return;

            long fluidCost = FluidXpUtils.xpToFluid(xpCost);
            if (tank.drain(fluidCost, true) < fluidCost) return;

            // Apply enchantments from book to tool
            ItemStack result = tool.copy();
            var bookEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(modifier);
            var mutable = new ItemEnchantments.Mutable(
                    EnchantmentHelper.getEnchantmentsForCrafting(result));
            bookEnchantments.entrySet().forEach(entry ->
                    mutable.set(entry.getKey(), entry.getIntValue()));
            EnchantmentHelper.setEnchantments(result, mutable.toImmutable());

            tank.drain(fluidCost, false);
            items.set(TOOL_SLOT, ItemStack.EMPTY);
            modifier.shrink(1);
            items.set(OUTPUT_SLOT, result);

            cooldown = COOLDOWN_TICKS;
            setChanged();

            level.playSound(null, worldPosition,
                    SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 1.0f);
        }
    }

    private TankBlockEntity getTankBelow() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return be instanceof TankBlockEntity tank ? tank : null;
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return 3;
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
        return Component.translatable("block.openblocks.auto_anvil");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoAnvilMenu(containerId, playerInventory, this);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        cooldown = tag.getInt("Cooldown");
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Tool: " + items.get(TOOL_SLOT));
        info.add("Modifier: " + items.get(MODIFIER_SLOT));
        info.add("Output: " + items.get(OUTPUT_SLOT));
        info.add("Cooldown: " + cooldown);
        info.add("Tank below: " + (getTankBelow() != null ? "yes" : "no"));
        return info;
    }
}
