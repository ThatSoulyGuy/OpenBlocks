package com.openblocks.grave;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Grave block. Placed by the death handler to store a player's inventory.
 * Right-click with a shovel to dig up the grave and retrieve items.
 * High blast resistance prevents destruction by explosions.
 */
public class GraveBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<GraveBlock> CODEC = simpleCodec(GraveBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 6, 16);

    public GraveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GraveBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (OpenBlocksConfig.Graves.spawnSkeletons) {
            return level.isClientSide() ? null :
                    createTickerHelper(type, OpenBlocksBlockEntities.GRAVE.get(), GraveBlockEntity::tick);
        }
        return null;
    }

    // --- Shovel Interaction (dig up grave) ---

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        if (stack.getItem() instanceof ShovelItem) {
            if (level.getBlockEntity(pos) instanceof GraveBlockEntity grave) {
                grave.dropAllItems();
                stack.hurtAndBreak(2, player, player.getEquipmentSlotForItem(stack));
                level.removeBlock(pos, false);
                level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // --- Empty hand: show death message ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GraveBlockEntity grave) {
            Component message = grave.getDeathMessage();
            if (message != null) {
                player.displayClientMessage(message, false);
            } else {
                String name = grave.getPlayerName();
                if (name != null && !name.isEmpty()) {
                    player.displayClientMessage(
                            Component.translatable("block.openblocks.grave.here_lies", name), false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // --- Drop items when broken ---

    @Override
    protected void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (level.getBlockEntity(pos) instanceof GraveBlockEntity grave) {
            grave.dropAllItems();
        }
    }
}
