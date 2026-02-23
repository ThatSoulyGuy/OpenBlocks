package com.openblocks.trophy;

import com.openblocks.core.base.OpenBlocksBlockEntity;
import com.openblocks.core.base.SyncedValue;
import com.openblocks.core.registry.OpenBlocksBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the trophy block. Stores which trophy type is displayed
 * and handles behavior cooldown.
 */
public class TrophyBlockEntity extends OpenBlocksBlockEntity {

    private final SyncedValue<TrophyType> trophyType = syncedEnum("trophy", TrophyType.CREEPER);
    private int cooldown = 0;

    public TrophyBlockEntity(BlockPos pos, BlockState state) {
        super(OpenBlocksBlockEntities.TROPHY.get(), pos, state);
    }

    public TrophyType getTrophyType() {
        return trophyType.get();
    }

    public void setTrophyType(TrophyType type) {
        if (type != null) {
            trophyType.set(type);
            sync();
        }
    }

    /**
     * Called when a player right-clicks the trophy. Plays sound + executes behavior.
     */
    public void activate(Player player) {
        if (level == null || level.isClientSide()) return;
        if (cooldown > 0) return;

        TrophyType type = trophyType.get();
        ITrophyBehavior behavior = type.getBehavior();
        if (behavior != null) {
            cooldown = behavior.executeActivateBehavior(this, player);
        }
    }

    /**
     * Server-side tick for behavior cooldown and tick behaviors.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TrophyBlockEntity be) {
        if (be.cooldown > 0) {
            be.cooldown--;
        }

        TrophyType type = be.trophyType.get();
        ITrophyBehavior behavior = type.getBehavior();
        if (behavior != null) {
            behavior.executeTickBehavior(be);
        }
    }
}
