package com.openblocks.guide;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Guide block that renders translucent ghost blocks in configurable shapes.
 * Right-click a face to adjust the dimension for that axis (sneak to decrement).
 * Right-click the top face to cycle shapes.
 */
public class GuideBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<GuideBlock> CODEC = simpleCodec(GuideBlock::new);

    public GuideBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GuideBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof GuideBlockEntity guide)) {
            return InteractionResult.FAIL;
        }

        // Check if holding a dye
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof DyeItem dyeItem) {
            DyeColor dye = dyeItem.getDyeColor();
            int color = dye.getTextureDiffuseColor();
            guide.setColor(color);
            player.displayClientMessage(Component.translatable("openblocks.misc.change_color"), true);
            return InteractionResult.SUCCESS;
        }

        Direction face = hitResult.getDirection();
        boolean sneaking = player.isShiftKeyDown();

        if (face == Direction.UP) {
            guide.handleAction(sneaking ? "dec_shape" : "inc_shape");
            player.displayClientMessage(Component.translatable("openblocks.misc.shape",
                    Component.translatable("openblocks.shape." + guide.getShape().getSerializedName())), true);
        } else {
            String axis = getAxisAction(face, sneaking);
            guide.handleAction(axis);
            player.displayClientMessage(Component.translatable("openblocks.misc.change_box_size"), true);
        }

        return InteractionResult.SUCCESS;
    }

    private static String getAxisAction(Direction face, boolean sneaking) {
        String prefix = sneaking ? "dec_" : "inc_";
        return switch (face) {
            case NORTH -> prefix + "negZ";
            case SOUTH -> prefix + "posZ";
            case WEST -> prefix + "negX";
            case EAST -> prefix + "posX";
            case DOWN -> prefix + "negY";
            default -> prefix + "posY";
        };
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                    net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof GuideBlockEntity guide) {
            guide.onNeighborChanged();
        }
    }
}
