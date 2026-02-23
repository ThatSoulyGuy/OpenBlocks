package com.openblocks.core.debug;

/**
 * Enum of all debuggable features in OpenBlocks.
 * Each feature can be independently toggled via /openblocks debug.
 */
public enum DebugFeature {
    GENERAL("general", "General mod diagnostics"),
    ELEVATOR("elevator", "Elevator search and teleportation"),
    TANK("tank", "Tank fluid levels and connections"),
    GRAVE("grave", "Grave spawning and inventory backup"),
    CANVAS("canvas", "Canvas painting and texture pool"),
    AUTOMATION("automation", "Block breaker/placer, hoppers"),
    ENTITY("entity", "Entity AI and physics"),
    ENCHANTMENT("enchantment", "Enchantment effects and flimflam"),
    GUIDE("guide", "Guide block shapes and rendering"),
    IMAGINARY("imaginary", "Imaginary block visibility"),
    NETWORK("network", "Network packet logging");

    private final String id;
    private final String description;

    DebugFeature(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public static DebugFeature fromId(String id) {
        for (DebugFeature feature : values()) {
            if (feature.id.equalsIgnoreCase(id)) {
                return feature;
            }
        }
        return null;
    }
}
