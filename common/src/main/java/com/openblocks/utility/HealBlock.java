package com.openblocks.utility;

import com.mojang.serialization.MapCodec;
import com.openblocks.core.base.OpenBlocksEntityBlock;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Creative-mode only healing block that gives a regeneration effect
 * to nearby players. Applies regen every 2 seconds within a 5-block radius.
 */
public class HealBlock extends OpenBlocksEntityBlock {

    public static final MapCodec<HealBlock> CODEC = simpleCodec(HealBlock::new);
    private static final double HEAL_RANGE = 5.0;

    public HealBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HealBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HealBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return createTickerHelper(type, OpenBlocksBlockEntities.HEAL.get(),
                    (lvl, pos, st, be) -> tickHeal(lvl, pos));
        }
        return null;
    }

    private static void tickHeal(Level level, BlockPos pos) {
        if (level.getGameTime() % 40 != 0) return;
        AABB area = new AABB(pos).inflate(HEAL_RANGE);
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
        }
    }
}
