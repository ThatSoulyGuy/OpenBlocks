package com.openblocks.interaction;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * /dev/null item. A single-slot trash inventory that auto-consumes matching
 * items on pickup. Shift-right-click to open the GUI and set the filter item.
 * Any items picked up that match the stored filter are automatically voided.
 */
public class DevNullItem extends Item {

    public DevNullItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, p) -> new DevNullMenu(containerId, inventory),
                        Component.translatable("item.openblocks.dev_null")
                ));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Get the filter item stored in this /dev/null stack.
     *
     * @param stack     the /dev/null ItemStack
     * @param registries the registry access for deserialization
     * @return the stored filter ItemStack, or ItemStack.EMPTY if none
     */
    public static ItemStack getFilterItem(ItemStack stack, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("FilterItem")) {
            return ItemStack.parseOptional(registries, tag.getCompound("FilterItem"));
        }
        return ItemStack.EMPTY;
    }

    /**
     * Set the filter item stored in this /dev/null stack.
     *
     * @param stack       the /dev/null ItemStack
     * @param filterStack the item to use as the filter
     * @param registries  the registry access for serialization
     */
    public static void setFilterItem(ItemStack stack, ItemStack filterStack, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.put("FilterItem", filterStack.saveOptional(registries));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Check if the given item stack matches the filter stored in a /dev/null stack.
     * Matching is by item type only (ignores count).
     *
     * @param devNullStack the /dev/null ItemStack
     * @param pickedUp     the item being picked up
     * @param registries   the registry access for deserialization
     * @return true if the picked-up item matches the filter
     */
    public static boolean matchesFilter(ItemStack devNullStack, ItemStack pickedUp, net.minecraft.core.HolderLookup.Provider registries) {
        ItemStack filter = getFilterItem(devNullStack, registries);
        if (filter.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(filter, pickedUp);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("FilterItem") && context.registries() != null) {
            ItemStack filter = ItemStack.parseOptional(context.registries(), tag.getCompound("FilterItem"));
            if (!filter.isEmpty()) {
                tooltip.add(Component.translatable("item.openblocks.dev_null.tooltip.filter",
                        filter.getHoverName()));
            } else {
                tooltip.add(Component.translatable("item.openblocks.dev_null.tooltip.empty"));
            }
        } else {
            tooltip.add(Component.translatable("item.openblocks.dev_null.tooltip.empty"));
        }
    }
}
