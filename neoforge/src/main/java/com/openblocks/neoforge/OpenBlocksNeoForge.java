package com.openblocks.neoforge;

import com.openblocks.OpenBlocksCommon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("openblocks")
public final class OpenBlocksNeoForge {

    public OpenBlocksNeoForge(IEventBus modEventBus) {
        OpenBlocksCommon.init();
    }
}
