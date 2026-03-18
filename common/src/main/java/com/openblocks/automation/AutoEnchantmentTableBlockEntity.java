package com.openblocks.automation;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.util.FluidXpUtils;
import com.openblocks.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Auto Enchantment Table block entity. Automatically enchants items
 * using XP fluid from a tank below. Respects nearby bookshelves.
 *
 * Slot 0: Item to enchant
 * Slot 1: Lapis lazuli
 * Slot 2: Output (enchanted item)
 */
public class AutoEnchantmentTableBlockEntity extends OpenBlocksBlockEntity implements Container, MenuProvider {

    private static final int ITEM_SLOT = 0;
    private static final int LAPIS_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;
    private static final int COOLDOWN_TICKS = 60;
    private static final int POWER_CHECK_INTERVAL = 20;
    private static final RandomSource BOOK_RANDOM = RandomSource.create();

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int cooldown = 0;
    private int cachedPower = 0;
    private int powerCheckTimer = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            TankBlockEntity tank = getTankBelow();
            return switch (index) {
                case 0 -> tank != null ? FluidXpUtils.fluidToXp(tank.getAmount()) : 0;
                case 1 -> tank != null ? FluidXpUtils.fluidToXp(tank.getCapacity()) : 0;
                case 2 -> cachedPower;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() { return 3; }
    };

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    // Book animation state (client-side only, mirrors vanilla EnchantingTableBlockEntity)
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;

    public AutoEnchantmentTableBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.AUTO_ENCHANTMENT_TABLE.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AutoEnchantmentTableBlockEntity be) {
        if (level.isClientSide()) return;

        // Periodically update bookshelf power
        be.powerCheckTimer++;
        if (be.powerCheckTimer >= POWER_CHECK_INTERVAL) {
            be.powerCheckTimer = 0;
            be.cachedPower = countBookshelves(level, pos);
        }

        if (be.cooldown > 0) {
            be.cooldown--;
            return;
        }

        if (!be.items.get(OUTPUT_SLOT).isEmpty()) return;
        if (be.items.get(ITEM_SLOT).isEmpty()) return;
        if (be.items.get(LAPIS_SLOT).isEmpty() || !be.items.get(LAPIS_SLOT).is(Items.LAPIS_LAZULI)) return;

        be.tryEnchant(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AutoEnchantmentTableBlockEntity be) {
        be.oOpen = be.open;
        be.oRot = be.rot;

        Player player = level.getNearestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);

        if (player != null) {
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);
            be.tRot = (float) Mth.atan2(dz, dx);
            be.open += 0.1f;

            if (be.open < 0.5f || BOOK_RANDOM.nextInt(40) == 0) {
                float old = be.flipT;
                do {
                    be.flipT += BOOK_RANDOM.nextInt(4) - BOOK_RANDOM.nextInt(4);
                } while (old == be.flipT);
            }
        } else {
            be.tRot += 0.02f;
            be.open -= 0.1f;
        }

        while (be.rot >= (float) Math.PI) be.rot -= (float) Math.PI * 2f;
        while (be.rot < -(float) Math.PI) be.rot += (float) Math.PI * 2f;
        while (be.tRot >= (float) Math.PI) be.tRot -= (float) Math.PI * 2f;
        while (be.tRot < -(float) Math.PI) be.tRot += (float) Math.PI * 2f;

        float delta = be.tRot - be.rot;
        while (delta >= (float) Math.PI) delta -= (float) Math.PI * 2f;
        while (delta < -(float) Math.PI) delta += (float) Math.PI * 2f;

        be.rot += delta * 0.4f;
        be.open = Mth.clamp(be.open, 0.0f, 1.0f);
        be.time++;
        be.oFlip = be.flip;
        float f = (be.flipT - be.flip) * 0.4f;
        f = Mth.clamp(f, -0.2f, 0.2f);
        be.flipA += (f - be.flipA) * 0.9f;
        be.flip += be.flipA;
    }

    private void tryEnchant(Level level) {
        ItemStack toEnchant = items.get(ITEM_SLOT);
        ItemStack lapis = items.get(LAPIS_SLOT);

        // Check if item is enchantable
        if (!toEnchant.isEnchantable()) return;
        // Skip already enchanted items
        ItemEnchantments existing = EnchantmentHelper.getEnchantmentsForCrafting(toEnchant);
        if (!existing.isEmpty()) return;

        // Calculate enchantment level based on bookshelf power
        int enchantLevel = Math.min(30, 1 + cachedPower * 2);
        int lapisCost = Math.max(1, enchantLevel / 10);
        if (lapis.getCount() < lapisCost) return;

        // XP cost scales with enchantment level
        int xpCost = enchantLevel;
        TankBlockEntity tank = getTankBelow();
        if (tank == null) return;

        long fluidCost = FluidXpUtils.xpToFluid(xpCost);
        if (tank.drain(fluidCost, true) < fluidCost) return;

        // Perform enchantment using default enchantment pool
        var registryAccess = level.registryAccess();
        ItemStack result = EnchantmentHelper.enchantItem(
                RandomSource.create(), toEnchant.copy(), enchantLevel,
                registryAccess, java.util.Optional.empty());

        if (result == null || EnchantmentHelper.getEnchantmentsForCrafting(result).isEmpty()) return;

        // Consume resources
        tank.drain(fluidCost, false);
        items.set(ITEM_SLOT, ItemStack.EMPTY);
        lapis.shrink(lapisCost);
        items.set(OUTPUT_SLOT, result);

        cooldown = COOLDOWN_TICKS;
        setChanged();

        level.playSound(null, worldPosition,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private TankBlockEntity getTankBelow() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return be instanceof TankBlockEntity tank ? tank : null;
    }

    private static int countBookshelves(Level level, BlockPos pos) {
        int count = 0;
        for (BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
             count < 15; ) {
            // Check 5x5 ring at distances 1 and 2 around the table
            boolean found = false;
            for (int x = -2; x <= 2 && count < 15; x++) {
                for (int z = -2; z <= 2 && count < 15; z++) {
                    if (Math.abs(x) != 2 && Math.abs(z) != 2) continue; // Ring only
                    for (int y = 0; y <= 1 && count < 15; y++) {
                        check.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        if (level.getBlockState(check).is(Blocks.BOOKSHELF)) {
                            // Check air gap between table and shelf
                            BlockPos between = new BlockPos(
                                    pos.getX() + x / 2, pos.getY() + y, pos.getZ() + z / 2);
                            if (level.getBlockState(between).isAir()) {
                                count++;
                            }
                        }
                    }
                }
            }
            break; // Only check once
        }
        return count;
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
        return Component.translatable("block.openblocks.auto_enchantment_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoEnchantmentTableMenu(containerId, playerInventory, this, dataAccess);
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
        info.add("Item: " + items.get(ITEM_SLOT));
        info.add("Lapis: " + items.get(LAPIS_SLOT));
        info.add("Output: " + items.get(OUTPUT_SLOT));
        info.add("Bookshelves: " + cachedPower);
        info.add("Cooldown: " + cooldown);
        info.add("Tank below: " + (getTankBelow() != null ? "yes" : "no"));
        return info;
    }
}
