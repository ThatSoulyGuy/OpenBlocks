package com.openblocks.utility;

import com.openblocks.core.config.OpenBlocksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * A portable sponge tool that absorbs liquids around the player.
 * Takes durability damage per use. If lava is absorbed, the item is
 * destroyed and the player catches fire.
 */
public class SpongeOnAStickItem extends Item {

    public SpongeOnAStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean result = absorbAt(level, player.blockPosition(), stack, player);
            if (result) {
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private boolean absorbAt(Level level, BlockPos center, ItemStack stack, Player player) {
        int range = OpenBlocksConfig.Sponge.stickRange;
        boolean absorbed = false;
        boolean foundLava = false;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos target = center.offset(x, y, z);
                    FluidState fluid = level.getFluidState(target);
                    if (!fluid.isEmpty()) {
                        if (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA) {
                            foundLava = true;
                        }
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                        absorbed = true;
                    }
                }
            }
        }

        if (foundLava) {
            player.igniteForSeconds(6);
            stack.shrink(stack.getCount());
        } else if (absorbed) {
            int newDamage = stack.getDamageValue() + 1;
            if (newDamage >= stack.getMaxDamage()) {
                stack.shrink(1);
            } else {
                stack.setDamageValue(newDamage);
            }
        }
        return absorbed;
    }
}
