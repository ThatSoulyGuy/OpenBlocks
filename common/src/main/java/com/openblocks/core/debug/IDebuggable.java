package com.openblocks.core.debug;

import java.util.List;

/**
 * Interface for objects that can provide debug information.
 * Implemented by block entities, handlers, and other game objects
 * to contribute to the F3 debug overlay and debug commands.
 */
public interface IDebuggable {

    /**
     * Returns debug info lines to display in the F3 overlay
     * when debug mode is enabled for this feature.
     */
    List<String> getDebugInfo();
}
