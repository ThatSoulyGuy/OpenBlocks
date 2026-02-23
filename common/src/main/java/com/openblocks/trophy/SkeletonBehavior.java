package com.openblocks.trophy;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class SkeletonBehavior implements ITrophyBehavior {

    @Override
    public int executeActivateBehavior(TrophyBlockEntity tile, Player player) {
        Level level = tile.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos pos = tile.getBlockPos();

            Arrow arrow = new Arrow(level, player, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW), null);
            arrow.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            arrow.setBaseDamage(0.1);
            arrow.shoot(level.random.nextInt(10) - 5, 40, level.random.nextInt(10) - 5, 1.0f, 6.0f);
            level.playSound(null, pos, SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL,
                    1.0f, 1.0f / (level.random.nextFloat() * 0.4f + 1.2f) + 0.5f);
            level.addFreshEntity(arrow);
        }
        return 0;
    }
}
