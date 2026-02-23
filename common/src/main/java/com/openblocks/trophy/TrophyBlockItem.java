package com.openblocks.trophy;

import com.openblocks.core.registry.OpenBlocksItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

/**
 * Custom block item for trophies. Stores the trophy type in CustomData
 * and provides a custom name showing the entity name.
 */
public class TrophyBlockItem extends BlockItem {

    public TrophyBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        TrophyType type = getTrophyType(stack);
        if (type != null) {
            Component entityName = type.getEntityType().getDescription();
            return Component.translatable("block.openblocks.trophy.entity", entityName);
        }
        return Component.translatable("block.openblocks.trophy");
    }

    // --- Static data accessors ---

    public static TrophyType getTrophyType(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("TrophyType")) {
            String name = tag.getString("TrophyType");
            return TrophyType.fromName(name);
        }
        return null;
    }

    public static void setTrophyType(ItemStack stack, TrophyType type) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("TrophyType", type.getSerializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static ItemStack createTrophyItem(TrophyType type) {
        ItemStack stack = new ItemStack(OpenBlocksItems.TROPHY.get());
        setTrophyType(stack, type);
        return stack;
    }
}
