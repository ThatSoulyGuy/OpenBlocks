package com.openblocks;

import com.openblocks.core.config.ConfigLoader;
import com.openblocks.core.debug.DebugCommand;
import com.openblocks.core.debug.TestCommand;
import com.openblocks.core.loot.LootTableModifier;
import com.openblocks.core.network.OpenBlocksNetwork;
import com.openblocks.core.registry.*;
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
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
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
    }
}
