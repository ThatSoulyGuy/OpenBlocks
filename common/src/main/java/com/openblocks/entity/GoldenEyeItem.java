package com.openblocks.entity;

import com.openblocks.core.registry.OpenBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Throwable item that spawns a GoldenEyeEntity pointing toward a stored structure location.
 * Sneak-click to lock onto nearest structure; click to throw.
 */
public class GoldenEyeItem extends Item {

    public GoldenEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                CustomData.EMPTY).copyTag();

        if (player.isShiftKeyDown()) {
            // Lock onto nearest structure
            ServerLevel serverLevel = (ServerLevel) level;
            // Try to find a stronghold
            BlockPos playerPos = player.blockPosition();
            var result = serverLevel.findNearestMapStructure(
                    net.minecraft.tags.StructureTags.EYE_OF_ENDER_LOCATED,
                    playerPos, 100, false);

            if (result != null) {
                tag.putInt("TargetX", result.getX());
                tag.putInt("TargetY", result.getY());
                tag.putInt("TargetZ", result.getZ());
                tag.putString("TargetName", "Stronghold");
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));

                player.displayClientMessage(
                        Component.translatable("openblocks.golden_eye.locked",
                                result.getX(), result.getZ()), true);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.displayClientMessage(
                        Component.translatable("openblocks.golden_eye.no_structure"), true);
            }

            return InteractionResultHolder.success(stack);
        }

        // Throw golden eye
        if (!tag.contains("TargetX")) {
            player.displayClientMessage(
                    Component.translatable("openblocks.golden_eye.not_locked"), true);
            return InteractionResultHolder.fail(stack);
        }

        BlockPos target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));

        GoldenEyeEntity entity = OpenBlocksEntities.GOLDEN_EYE.get().create((ServerLevel) level);
        if (entity != null) {
            entity.setup(stack, player, target);
            level.addFreshEntity(entity);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5f,
                    0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                CustomData.EMPTY).copyTag();
        if (tag.contains("TargetName")) {
            tooltip.add(Component.translatable("openblocks.golden_eye.target",
                    tag.getString("TargetName"), tag.getInt("TargetX"), tag.getInt("TargetZ")));
        }
    }
}
