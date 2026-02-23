package com.openblocks.canvas;

import com.openblocks.core.registry.OpenBlocksBlockEntities;
import com.openblocks.core.registry.OpenBlocksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Paint mixer: combines dye items with a bucket to produce a paint can item.
 * Simplified implementation — auto-crafts when right-clicked with dye + bucket.
 */
public class PaintMixerBlockEntity extends BlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    // Slot 0: bucket input, Slots 1-4: dye inputs

    public PaintMixerBlockEntity(BlockPos pos, BlockState state) {
        super(com.openblocks.core.registry.OpenBlocksBlockEntities.PAINT_MIXER.get(), pos, state);
    }

    /**
     * Mix colors from dye items and produce a paint can.
     * Called when the block is right-clicked.
     */
    public ItemStack tryMix(Player player) {
        // Collect dyes from player's hand or find dyes nearby
        // Simplified: just compute the average color from the dye items in the input
        ItemStack held = player.getMainHandItem();

        if (held.getItem() instanceof DyeItem dyeItem) {
            int color = getDyeColor(dyeItem.getDyeColor());

            // Create paint can item with this color
            ItemStack paintCan = new ItemStack(OpenBlocksItems.PAINT_CAN_ITEM.get());
            CompoundTag tag = new CompoundTag();
            tag.putInt("Color", color);
            tag.putInt("Amount", PaintCanBlockEntity.FULL_CAN_SIZE);
            paintCan.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(tag));

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return paintCan;
        }

        return ItemStack.EMPTY;
    }

    private static int getDyeColor(DyeColor dye) {
        return com.openblocks.core.util.ColorMeta.fromDyeColor(dye).getRgb();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(5, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
