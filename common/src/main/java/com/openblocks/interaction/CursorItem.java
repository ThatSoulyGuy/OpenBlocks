package com.openblocks.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Remote block activator. Shift-right-click a block to store its position,
 * then right-click in air to activate that block from a distance.
 * Costs 1 XP level per 16 blocks of distance. Max range is 64 blocks.
 */
public class CursorItem extends Item {

    private static final int MAX_DISTANCE = 64;
    private static final int BLOCKS_PER_XP_LEVEL = 16;

    public CursorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide()) {
            // Store target position in CustomData
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("TargetX", pos.getX());
            tag.putInt("TargetY", pos.getY());
            tag.putInt("TargetZ", pos.getZ());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            player.displayClientMessage(
                    Component.translatable("item.openblocks.cursor.bound",
                            pos.getX(), pos.getY(), pos.getZ()), true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // Read stored target from CustomData
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (!tag.contains("TargetX")) {
            player.displayClientMessage(
                    Component.translatable("item.openblocks.cursor.not_bound"), true);
            return InteractionResultHolder.fail(stack);
        }

        BlockPos target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));

        // Check distance
        double distance = Math.sqrt(player.blockPosition().distSqr(target));
        if (distance > MAX_DISTANCE) {
            player.displayClientMessage(
                    Component.translatable("item.openblocks.cursor.too_far"), true);
            return InteractionResultHolder.fail(stack);
        }

        // Check that the target chunk is loaded
        if (!level.isLoaded(target)) {
            player.displayClientMessage(
                    Component.translatable("item.openblocks.cursor.not_loaded"), true);
            return InteractionResultHolder.fail(stack);
        }

        // Calculate XP cost: 1 level per 16 blocks
        int xpCost = Math.max(1, (int) Math.ceil(distance / BLOCKS_PER_XP_LEVEL));

        // Check XP (creative players are exempt)
        if (!player.getAbilities().instabuild) {
            if (player.experienceLevel < xpCost) {
                player.displayClientMessage(
                        Component.translatable("item.openblocks.cursor.no_xp", xpCost), true);
                return InteractionResultHolder.fail(stack);
            }
            // Deduct XP levels
            player.giveExperienceLevels(-xpCost);
        }

        // Activate the remote block
        BlockState blockState = level.getBlockState(target);
        BlockHitResult hitResult = new BlockHitResult(
                Vec3.atCenterOf(target), Direction.UP, target, false);

        InteractionResult result = blockState.useWithoutItem(level, player, hitResult);

        if (result.consumesAction()) {
            player.displayClientMessage(
                    Component.translatable("item.openblocks.cursor.activated",
                            target.getX(), target.getY(), target.getZ()), true);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("TargetX")) {
            tooltip.add(Component.translatable("item.openblocks.cursor.tooltip.target",
                    tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ")));
        } else {
            tooltip.add(Component.translatable("item.openblocks.cursor.tooltip.unbound"));
        }
        tooltip.add(Component.translatable("item.openblocks.cursor.tooltip.range", MAX_DISTANCE));
    }
}
