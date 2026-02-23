package com.openblocks.core.debug;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central manager for debug mode toggles.
 * Tracks which features have debug mode enabled.
 */
public final class DebugManager {

    private static final DebugManager INSTANCE = new DebugManager();

    private final Map<DebugFeature, Boolean> enabledFeatures = new EnumMap<>(DebugFeature.class);
    private final Map<DebugFeature, Long> profilingStart = new EnumMap<>(DebugFeature.class);
    private final Map<DebugFeature, Long> profilingResults = new EnumMap<>(DebugFeature.class);

    private DebugManager() {
        for (DebugFeature feature : DebugFeature.values()) {
            enabledFeatures.put(feature, false);
        }
    }

    public static DebugManager get() {
        return INSTANCE;
    }

    public boolean isEnabled(DebugFeature feature) {
        return enabledFeatures.getOrDefault(feature, false);
    }

    public void setEnabled(DebugFeature feature, boolean enabled) {
        enabledFeatures.put(feature, enabled);
    }

    public void toggle(DebugFeature feature) {
        setEnabled(feature, !isEnabled(feature));
    }

    public void setAll(boolean enabled) {
        for (DebugFeature feature : DebugFeature.values()) {
            enabledFeatures.put(feature, enabled);
        }
    }

    public Map<DebugFeature, Boolean> getAllStates() {
        return new EnumMap<>(enabledFeatures);
    }

    // --- Profiling ---

    public void startProfiling(DebugFeature feature) {
        profilingStart.put(feature, System.nanoTime());
    }

    public void endProfiling(DebugFeature feature) {
        Long start = profilingStart.remove(feature);
        if (start != null) {
            long duration = System.nanoTime() - start;
            profilingResults.merge(feature, duration, Long::sum);
        }
    }

    public Map<DebugFeature, Long> getProfilingResults() {
        return new EnumMap<>(profilingResults);
    }

    public void resetProfiling() {
        profilingStart.clear();
        profilingResults.clear();
    }
}
