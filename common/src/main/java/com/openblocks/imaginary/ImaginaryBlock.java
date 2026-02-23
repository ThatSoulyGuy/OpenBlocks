package com.openblocks.imaginary;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Imaginary block — invisible by default, only visible/collidable when the
 * player wears the correct glasses. Pencil blocks are gray, crayon blocks
 * are colored. Inverted blocks reverse the visibility logic.
 */
public class ImaginaryBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<ImaginaryBlock> CODEC = simpleCodec(ImaginaryBlock::new);
    public static final EnumProperty<ImaginaryType> TYPE = EnumProperty.create("type", ImaginaryType.class);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ImaginaryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, ImaginaryType.PENCIL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ImaginaryBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // INVISIBLE suppresses crack/mining particles (ParticleEngine.crack checks this).
        // The BER still renders regardless of RenderShape.
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        // Suppress break particles — imaginary blocks have no real texture for TerrainParticle
        // to sample from, which causes crashes when the block entity is removed mid-render.
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ImaginaryBlockEntity be) {
            Player player = getPlayerFromContext(context);
            if (player == null) {
                // No player context (particle engine, etc.) — return full shape to
                // prevent crashes from VoxelShape.bounds() on empty shapes.
                return Shapes.block();
            }
            if (be.isSelectableFor(player)) {
                return Shapes.block();
            }
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ImaginaryBlockEntity be) {
            Player player = getPlayerFromContext(context);
            if (player != null && be.isSolidFor(player)) {
                return Shapes.block();
            }
            // For non-player entities, pencil blocks are always solid (unless inverted)
            if (player == null) {
                boolean solidByDefault = be.isPencil();
                if (solidByDefault != be.isInverted()) {
                    return Shapes.block();
                }
            }
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    private static Player getPlayerFromContext(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}
