package com.openblocks.tank;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

/**
 * Tank block. Stores fluid, connects with adjacent tanks for balancing,
 * supports bucket fill/drain interaction, and preserves fluid on break/place.
 */
public class TankBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<TankBlock> CODEC = simpleCodec(TankBlock::new);

    public TankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :
                createTickerHelper(type, OpenBlocksBlockEntities.TANK.get(), TankBlockEntity::tick);
    }

    // --- Comparator ---

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TankBlockEntity tank) {
            return tank.getComparatorOutput();
        }
        return 0;
    }

    // --- Bucket Interaction ---

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof TankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Try fill tank from bucket
        Fluid bucketFluid = getBucketFluid(stack);
        if (bucketFluid != Fluids.EMPTY && tank.accepts(bucketFluid)) {
            long bucketAmount = FluidStack.bucketAmount();
            long filled = tank.fill(bucketFluid, bucketAmount, true);
            if (filled == bucketAmount) {
                tank.fill(bucketFluid, bucketAmount, false);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        // Try drain tank into empty bucket
        if (stack.is(Items.BUCKET) && !tank.isEmpty()) {
            long bucketAmount = FluidStack.bucketAmount();
            if (tank.getAmount() >= bucketAmount) {
                ItemStack filledBucket = getFilledBucket(tank.getFluidType());
                if (!filledBucket.isEmpty()) {
                    tank.drain(bucketAmount, false);
                    if (!player.getAbilities().instabuild) {
                        if (stack.getCount() == 1) {
                            player.setItemInHand(hand, filledBucket);
                        } else {
                            stack.shrink(1);
                            if (!player.getInventory().add(filledBucket)) {
                                player.drop(filledBucket, false);
                            }
                        }
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // --- Drop with fluid data (no loot table needed) ---

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()) {
            if (level.getBlockEntity(pos) instanceof TankBlockEntity tank) {
                ItemStack drop = new ItemStack(this);
                if (!tank.isEmpty()) {
                    TankBlockItem.setFluid(drop, tank.getFluidType(), tank.getAmount());
                }
                Block.popResource(level, pos, drop);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // --- Placement restores fluid from item ---

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TankBlockEntity tank) {
            TankBlockItem.FluidInfo info = TankBlockItem.getFluid(stack);
            if (info != null) {
                tank.setFluidDirect(info.fluid(), info.amount());
                tank.sync();
            }
        }
    }

    // --- Bucket Fluid Helpers ---

    private static Fluid getBucketFluid(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) return Fluids.WATER;
        if (stack.is(Items.LAVA_BUCKET)) return Fluids.LAVA;
        return Fluids.EMPTY;
    }

    private static ItemStack getFilledBucket(Fluid fluid) {
        if (fluid == Fluids.WATER) return new ItemStack(Items.WATER_BUCKET);
        if (fluid == Fluids.LAVA) return new ItemStack(Items.LAVA_BUCKET);
        return ItemStack.EMPTY;
    }
}
