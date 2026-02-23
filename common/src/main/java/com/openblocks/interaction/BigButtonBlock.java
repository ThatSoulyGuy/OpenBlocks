package com.openblocks.interaction;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A large button that outputs a redstone signal. The press duration is
 * determined by the number of items in its internal 8-slot inventory.
 * Can be mounted on any surface. Comes in stone and wood variants.
 */
public class BigButtonBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<BigButtonBlock> CODEC = simpleCodec(props -> new BigButtonBlock(props, false));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape UP_SHAPE = Block.box(1, 0, 1, 15, 2, 15);
    private static final VoxelShape UP_PRESSED = Block.box(1, 0, 1, 15, 1, 15);
    private static final VoxelShape DOWN_SHAPE = Block.box(1, 14, 1, 15, 16, 15);
    private static final VoxelShape DOWN_PRESSED = Block.box(1, 15, 1, 15, 16, 15);
    private static final VoxelShape NORTH_SHAPE = Block.box(1, 1, 14, 15, 15, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(1, 1, 0, 15, 15, 2);
    private static final VoxelShape WEST_SHAPE = Block.box(14, 1, 1, 16, 15, 15);
    private static final VoxelShape EAST_SHAPE = Block.box(0, 1, 1, 2, 15, 15);

    private final boolean wooden;

    public BigButtonBlock(Properties properties, boolean wooden) {
        super(properties);
        this.wooden = wooden;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BigButtonBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean pressed = state.getValue(POWERED);
        return switch (state.getValue(FACING)) {
            case DOWN -> pressed ? DOWN_PRESSED : DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> pressed ? UP_PRESSED : UP_SHAPE;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (state.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(POWERED, true), 3);
            level.playSound(null, pos,
                    wooden ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_ON,
                    SoundSource.BLOCKS, 0.3f, 0.6f);
            updateNeighbors(state, level, pos);

            int tickTime = 1;
            if (level.getBlockEntity(pos) instanceof BigButtonBlockEntity be) {
                tickTime = be.getTickTime();
            }
            level.scheduleTick(pos, this, tickTime);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            level.playSound(null, pos,
                    wooden ? SoundEvents.WOODEN_BUTTON_CLICK_OFF : SoundEvents.STONE_BUTTON_CLICK_OFF,
                    SoundSource.BLOCKS, 0.3f, 0.5f);
            updateNeighbors(state, level, pos);
        }
    }

    private void updateNeighbors(BlockState state, Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        Direction facing = state.getValue(FACING);
        level.updateNeighborsAt(pos.relative(facing.getOpposite()), this);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) && state.getValue(FACING) == direction ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigButtonBlockEntity(pos, state);
    }
}
