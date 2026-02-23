package com.openblocks.trophy;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines all available trophy types, mapping to vanilla entity types.
 * Each trophy has a scale, vertical offset, and optional behavior.
 */
public enum TrophyType implements StringRepresentable {
    BAT("bat", EntityType.BAT, 1.0, -0.3),
    BLAZE("blaze", EntityType.BLAZE, 0.4, 0.0, new BlazeBehavior()),
    CAT("cat", EntityType.CAT, 0.4, 0.0),
    CAVE_SPIDER("cave_spider", EntityType.CAVE_SPIDER, 0.4, 0.0),
    CHICKEN("chicken", EntityType.CHICKEN, 0.4, 0.0,
            new ItemDropBehavior(10000, new ItemStack(Items.EGG), SoundEvents.CHICKEN_EGG)),
    COW("cow", EntityType.COW, 0.4, 0.0,
            new ItemDropBehavior(20000, new ItemStack(Items.LEATHER))),
    CREEPER("creeper", EntityType.CREEPER, 0.4, 0.0, new CreeperBehavior()),
    ENDERMAN("enderman", EntityType.ENDERMAN, 0.3, 0.0, new EndermanBehavior()),
    GHAST("ghast", EntityType.GHAST, 0.1, 0.3),
    GUARDIAN("guardian", EntityType.GUARDIAN, 0.4, 0.0, new GuardianBehavior()),
    IRON_GOLEM("iron_golem", EntityType.IRON_GOLEM, 0.3, 0.0),
    MAGMA_CUBE("magma_cube", EntityType.MAGMA_CUBE, 0.6, 0.0),
    MOOSHROOM("mooshroom", EntityType.MOOSHROOM, 0.4, 0.0, new MooshroomBehavior()),
    PIG("pig", EntityType.PIG, 0.4, 0.0,
            new ItemDropBehavior(20000, new ItemStack(Items.PORKCHOP))),
    RABBIT("rabbit", EntityType.RABBIT, 0.4, 0.0,
            new ItemDropBehavior(20000, new ItemStack(Items.CARROT))),
    SHEEP("sheep", EntityType.SHEEP, 0.4, 0.0),
    SKELETON("skeleton", EntityType.SKELETON, 0.4, 0.0, new SkeletonBehavior()),
    SLIME("slime", EntityType.SLIME, 0.6, 0.0),
    SNOW_GOLEM("snow_golem", EntityType.SNOW_GOLEM, 0.4, 0.0, new SnowGolemBehavior()),
    SPIDER("spider", EntityType.SPIDER, 0.4, 0.0),
    SQUID("squid", EntityType.SQUID, 0.3, 0.5, new SquidBehavior()),
    VILLAGER("villager", EntityType.VILLAGER, 0.4, 0.0),
    WITCH("witch", EntityType.WITCH, 0.35, 0.0, new WitchBehavior()),
    WOLF("wolf", EntityType.WOLF, 0.4, 0.0),
    ZOMBIE("zombie", EntityType.ZOMBIE, 0.4, 0.0),
    ZOMBIFIED_PIGLIN("zombified_piglin", EntityType.ZOMBIFIED_PIGLIN, 0.4, 0.0,
            new ItemDropBehavior(20000, new ItemStack(Items.GOLD_NUGGET)));

    private final String name;
    private final EntityType<?> entityType;
    private final double scale;
    private final double verticalOffset;
    private final ITrophyBehavior behavior;

    private static final Map<EntityType<?>, TrophyType> BY_ENTITY_TYPE = new HashMap<>();

    static {
        for (TrophyType type : values()) {
            BY_ENTITY_TYPE.put(type.entityType, type);
        }
    }

    TrophyType(String name, EntityType<?> entityType, double scale, double verticalOffset) {
        this(name, entityType, scale, verticalOffset, null);
    }

    TrophyType(String name, EntityType<?> entityType, double scale, double verticalOffset, ITrophyBehavior behavior) {
        this.name = name;
        this.entityType = entityType;
        this.scale = scale;
        this.verticalOffset = verticalOffset;
        this.behavior = behavior;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public double getScale() {
        return scale;
    }

    public double getVerticalOffset() {
        return verticalOffset;
    }

    public ITrophyBehavior getBehavior() {
        return behavior;
    }

    public static TrophyType fromEntityType(EntityType<?> type) {
        return BY_ENTITY_TYPE.get(type);
    }

    public static TrophyType fromOrdinal(int ordinal) {
        TrophyType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }

    public static TrophyType fromName(String name) {
        for (TrophyType type : values()) {
            if (type.name.equals(name)) return type;
        }
        return null;
    }
}
