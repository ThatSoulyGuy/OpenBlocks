package com.openblocks.tank;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

/**
 * Tank block item that preserves fluid contents in the item's custom data component.
 */
public class TankBlockItem extends BlockItem {

    public TankBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    // --- Fluid data on ItemStack ---

    public static void setFluid(ItemStack stack, Fluid fluid, long amount) {
        if (fluid == Fluids.EMPTY || amount <= 0) return;
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        CompoundTag tankTag = new CompoundTag();
        tankTag.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid).toString());
        tankTag.putLong("Amount", amount);
        tag.put("Tank", tankTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static FluidInfo getFluid(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("Tank")) return null;
        CompoundTag tankTag = tag.getCompound("Tank");
        String fluidName = tankTag.getString("FluidName");
        ResourceLocation fluidId = ResourceLocation.tryParse(fluidName);
        if (fluidId == null) return null;
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        long amount = tankTag.getLong("Amount");
        if (fluid == Fluids.EMPTY || amount <= 0) return null;
        return new FluidInfo(fluid, amount);
    }

    public record FluidInfo(Fluid fluid, long amount) {}

    // --- Tooltip ---

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        FluidInfo info = getFluid(stack);
        if (info != null) {
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(info.fluid());
            long bucketAmount = FluidStack.bucketAmount();
            float buckets = (float) info.amount() / bucketAmount;
            lines.add(Component.translatable("tooltip.openblocks.tank.contents",
                    fluidId.toString(), String.format("%.1f", buckets)));
        }
    }
}
