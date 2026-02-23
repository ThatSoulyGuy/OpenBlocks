package com.openblocks.core.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base screen class for non-container screens (e.g. info book).
 * Replaces OpenModsLib's ComponentGui.
 */
public abstract class OpenBlocksScreen extends Screen {

    protected OpenBlocksScreen(Component title) {
        super(title);
    }
}
