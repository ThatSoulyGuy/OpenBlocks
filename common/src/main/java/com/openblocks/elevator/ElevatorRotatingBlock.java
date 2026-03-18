package com.openblocks.elevator;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Rotating elevator variant. Stores color in its block entity (so it doesn't
 * need 16 * 4 = 64 blockstate permutations). Has a FACING property that
 * determines which direction the player faces after teleporting.
 */
public class ElevatorRotatingBlock extends OpenBlocksEntityBlock implements IElevatorBlock {

    public static final MapCodec<ElevatorRotatingBlock> CODEC = simpleCodec(ElevatorRotatingBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ElevatorRotatingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends ElevatorRotatingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElevatorRotatingBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            if (!level.isClientSide()) {
                if (level.getBlockEntity(pos) instanceof ElevatorRotatingBlockEntity be) {
                    be.setColor(dyeItem.getDyeColor());
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        // Rotate the block when right-clicked with empty hand
        if (!level.isClientSide()) {
            Direction current = state.getValue(FACING);
            Direction next = current.getClockWise();
            level.setBlock(pos, state.setValue(FACING, next), 3);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            DyeColor color = getItemColor(stack);
            if (color != null && level.getBlockEntity(pos) instanceof ElevatorRotatingBlockEntity be) {
                be.setColor(color);
            }
        }
    }

    /** Key used to store elevator color in CUSTOM_DATA on item stacks. */
    public static final String COLOR_TAG = "elevator_color";

    /** Read the color stored on an item stack, or null if none. */
    public static @Nullable DyeColor getItemColor(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(COLOR_TAG)) {
                return DyeColor.byId(tag.getInt(COLOR_TAG));
            }
        }
        return null;
    }

    /** Create an item stack with the given color stored in CUSTOM_DATA. */
    public static ItemStack createColoredStack(net.minecraft.world.item.Item item, DyeColor color) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putInt(COLOR_TAG, color.getId());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public DyeColor getElevatorColor(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ElevatorRotatingBlockEntity be) {
            return be.getColor();
        }
        return DyeColor.WHITE;
    }

    @Override
    public PlayerRotation getElevatorRotation(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        // The model rotation makes the arrow point opposite to FACING,
        // so we invert the mapping to match the visual arrow direction.
        return switch (facing) {
            case NORTH -> PlayerRotation.SOUTH;
            case SOUTH -> PlayerRotation.NORTH;
            case EAST -> PlayerRotation.WEST;
            case WEST -> PlayerRotation.EAST;
            default -> PlayerRotation.NONE;
        };
    }
}
