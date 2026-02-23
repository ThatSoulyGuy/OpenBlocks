package com.openblocks.fabric;

import com.openblocks.OpenBlocksCommon;
import net.fabricmc.api.ModInitializer;

public final class OpenBlocksFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        OpenBlocksCommon.init();
    }
}
