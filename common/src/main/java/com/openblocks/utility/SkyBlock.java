package com.openblocks.utility;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/**
 * A block that becomes transparent when powered by redstone (or when
 * not powered if inverted). Used to create windows that show the sky.
 * The client-side rendering will handle the transparency effect.
 */
public class SkyBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<SkyBlock> CODEC = simpleCodec(props -> new SkyBlock(props, false));
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    private final boolean defaultInverted;

    public SkyBlock(Properties properties, boolean defaultInverted) {
        super(properties);
        this.defaultInverted = defaultInverted;
        registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(INVERTED, defaultInverted));
    }

    @Override
    protected MapCodec<? extends SkyBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, INVERTED);
    }

    public boolean isActive(BlockState state) {
        return state.getValue(POWERED) ^ state.getValue(INVERTED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                    BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyBlockEntity(pos, state);
    }
}
