package com.openblocks.automation;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Block Breaker block entity. When powered, breaks the block in front
 * and drops its items.
 */
public class BlockBreakerBlockEntity extends OpenBlocksBlockEntity {

    private boolean wasPowered = false;

    public BlockBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.BLOCK_BREAKER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockBreakerBlockEntity be) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        boolean powered = state.getValue(BlockBreakerBlock.POWERED);

        // Rising edge detection
        if (powered && !be.wasPowered) {
            be.breakTargetBlock(serverLevel, state);
        }
        be.wasPowered = powered;
    }

    private void breakTargetBlock(ServerLevel serverLevel, BlockState selfState) {
        Direction facing = selfState.getValue(BlockBreakerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        BlockState targetState = serverLevel.getBlockState(targetPos);

        if (targetState.isAir()) return;
        if (targetState.getDestroySpeed(serverLevel, targetPos) < 0) return; // Unbreakable

        // Get drops
        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                targetState, serverLevel, targetPos, serverLevel.getBlockEntity(targetPos));

        // Break the block
        serverLevel.destroyBlock(targetPos, false);

        // Drop items at target position
        for (ItemStack drop : drops) {
            net.minecraft.world.level.block.Block.popResource(serverLevel, targetPos, drop);
        }

        // Play sound
        serverLevel.playSound(null, worldPosition,
                SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, 0.7f);
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> info = super.getDebugInfo();
        info.add("Powered: " + wasPowered);
        return info;
    }
}
