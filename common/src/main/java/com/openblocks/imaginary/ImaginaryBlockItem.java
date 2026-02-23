package com.openblocks.imaginary;

import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlocks;
import com.openblocks.core.util.ColorMeta;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Pencil/Crayon item that places imaginary blocks. Pencils have no color (gray),
 * crayons have a stored color. Each use depletes the item.
 */
public class ImaginaryBlockItem extends Item {

    public ImaginaryBlockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Shift-click to toggle inverted mode
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());

        if (!level.getBlockState(placePos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        float uses = getUses(stack);
        if (uses <= 0) return InteractionResult.FAIL;

        if (!level.isClientSide()) {
            Integer color = getColor(stack);
            boolean inverted = isInverted(stack);
            ImaginaryType type = color == null ? ImaginaryType.PENCIL : ImaginaryType.CRAYON;

            BlockState state = OpenBlocksBlocks.IMAGINARY.get().defaultBlockState()
                    .setValue(ImaginaryBlock.TYPE, type);
            level.setBlock(placePos, state, 3);

            if (level.getBlockEntity(placePos) instanceof ImaginaryBlockEntity be) {
                be.setColor(color);
                be.setInverted(inverted);
            }

            float cost = inverted ? 1.5f : 1.0f;
            setUses(stack, uses - cost);
            if (getUses(stack) <= 0 && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide()) {
                boolean current = isInverted(stack);
                setInverted(stack, !current);
                String mode = current ? "openblocks.misc.mode.block" : "openblocks.misc.mode.inverted_block";
                player.displayClientMessage(Component.translatable("openblocks.misc.change_mode",
                        Component.translatable(mode)), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public Component getName(ItemStack stack) {
        Integer color = getColor(stack);
        if (color == null) {
            return Component.translatable("item.openblocks.imaginary.pencil");
        } else {
            return Component.translatable("item.openblocks.imaginary.crayon");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        float uses = getUses(stack);
        tooltipComponents.add(Component.translatable("openblocks.misc.uses", String.format("%.1f", uses)));

        Integer color = getColor(stack);
        if (color != null) {
            tooltipComponents.add(Component.translatable("openblocks.misc.color",
                    String.format("#%06X", color)));
        }

        if (isInverted(stack)) {
            tooltipComponents.add(Component.translatable("openblocks.misc.mode.inverted_block"));
        }
    }

    // --- Static data accessors ---

    public static Integer getColor(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Color")) {
            return tag.getInt("Color");
        }
        return null;
    }

    public static void setColor(ItemStack stack, Integer color) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (color != null) {
            tag.putInt("Color", color);
            int modelData = getModelDataForColor(color);
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(modelData));
        } else {
            tag.remove("Color");
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static int getModelDataForColor(int rgb) {
        for (ColorMeta meta : ColorMeta.values()) {
            if (meta.getRgb() == rgb) {
                return meta.ordinal() + 1; // 1-16 matching model overrides
            }
        }
        return 1; // fallback to white
    }

    public static float getUses(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Uses")) {
            return tag.getFloat("Uses");
        }
        return OpenBlocksConfig.Imaginary.itemUseCount;
    }

    public static void setUses(ItemStack stack, float uses) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putFloat("Uses", uses);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isInverted(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBoolean("Inverted");
    }

    public static void setInverted(ItemStack stack, boolean inverted) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("Inverted", inverted);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // --- Factory methods for creative tab ---

    public static ItemStack createPencil() {
        ItemStack stack = new ItemStack(com.openblocks.core.registry.OpenBlocksItems.IMAGINARY_ITEM.get());
        setUses(stack, OpenBlocksConfig.Imaginary.itemUseCount);
        return stack;
    }

    public static ItemStack createCrayon(int color) {
        ItemStack stack = new ItemStack(com.openblocks.core.registry.OpenBlocksItems.IMAGINARY_ITEM.get());
        setColor(stack, color);
        setUses(stack, OpenBlocksConfig.Imaginary.itemUseCount);
        return stack;
    }

    public static ItemStack createPencilInverted() {
        ItemStack stack = createPencil();
        setInverted(stack, true);
        return stack;
    }

    public static ItemStack createCrayonInverted(int color) {
        ItemStack stack = createCrayon(color);
        setInverted(stack, true);
        return stack;
    }
}
