package com.openblocks;

import com.openblocks.canvas.PaintChunkTracker;
import com.openblocks.canvas.PaintInteractionHandler;
import com.openblocks.canvas.PaintSavedData;
import com.openblocks.canvas.PaintUpdateS2CPacket;
import com.openblocks.core.config.ConfigLoader;
import com.openblocks.core.debug.DebugCommand;
import com.openblocks.core.debug.TestCommand;
import com.openblocks.core.loot.LootTableModifier;
import com.openblocks.core.network.OpenBlocksNetwork;
import com.openblocks.core.registry.*;
import com.openblocks.imaginary.StencilItem;
import com.openblocks.imaginary.StencilPattern;
import com.openblocks.canvas.PaintEntry;
import dev.architectury.platform.Platform;
import com.openblocks.enchantment.ExplosiveHandler;
import com.openblocks.enchantment.FlimFlamHandler;
import com.openblocks.enchantment.LastStandHandler;
import com.openblocks.enchantment.flimflam.FlimFlamRegistry;
import com.openblocks.entity.LuggageEntity;
import com.openblocks.entity.MiniMeEntity;
import com.openblocks.grave.PlayerDeathHandler;
import com.openblocks.trophy.TrophyDropHandler;
import com.openblocks.interaction.PedometerItem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;

public final class OpenBlocksCommon {

    public static void init() {
        // Config
        ConfigLoader.init(Platform.getGameFolder());

        // Registry
        OpenBlocksBlocks.register();
        OpenBlocksItems.register();
        OpenBlocksBlockEntities.register();
        OpenBlocksEntities.register();
        OpenBlocksSounds.register();
        OpenBlocksMenus.register();
        OpenBlocksCreativeTabs.register();

        // Entity attributes
        EntityAttributeRegistry.register(OpenBlocksEntities.LUGGAGE, LuggageEntity::createAttributes);
        EntityAttributeRegistry.register(OpenBlocksEntities.MINI_ME, MiniMeEntity::createAttributes);

        // Networking
        OpenBlocksNetwork.register();

        // Commands
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            DebugCommand.register(dispatcher);
            TestCommand.register(dispatcher);
        });

        // Tick events
        TickEvent.PLAYER_POST.register(PedometerItem::tickPlayer);

        // Death handler (graves)
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            PlayerDeathHandler.onPlayerDeath(entity, source);
            return EventResult.pass();
        });

        // Trophy drop handler
        TrophyDropHandler.register();

        // Loot table modifications
        LootTableModifier.register();

        // Enchantment handlers
        FlimFlamRegistry.registerAll();
        ExplosiveHandler.register();
        LastStandHandler.register();
        FlimFlamHandler.register();

        // Paint any block: global right-click handler
        InteractionEvent.RIGHT_CLICK_BLOCK.register(PaintInteractionHandler::onRightClickBlock);

        // Paint any block: chunk sync tracking
        TickEvent.PLAYER_POST.register(PaintChunkTracker::onPlayerTick);

        // Paint any block: cleanup on player quit
        PlayerEvent.PLAYER_QUIT.register(player -> {
            if (player instanceof ServerPlayer sp) {
                PaintChunkTracker.onPlayerQuit(sp);
            }
        });

        // Paint any block: cleanup on block break
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level instanceof ServerLevel serverLevel) {
                PaintSavedData data = PaintSavedData.get(serverLevel);
                PaintEntry entry = data.get(pos);
                if (entry != null) {
                    // Drop stencil covers as items
                    for (Direction d : Direction.values()) {
                        if (entry.hasStencilCover(d)) {
                            StencilPattern pattern = entry.getStencil(d);
                            if (pattern != null) {
                                ItemStack stencilStack = StencilItem.createStencil(pattern);
                                Containers.dropItemStack(level,
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stencilStack);
                            }
                        }
                    }
                    data.remove(pos);
                    PaintInteractionHandler.broadcastPaintUpdate(serverLevel, pos, new PaintEntry());
                }
            }
            return EventResult.pass();
        });
    }
}
