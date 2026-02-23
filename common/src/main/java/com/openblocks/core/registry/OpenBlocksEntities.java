package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.entity.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class OpenBlocksEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<GoldenEyeEntity>> GOLDEN_EYE =
            ENTITIES.register("golden_eye", () ->
                    EntityType.Builder.<GoldenEyeEntity>of(GoldenEyeEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(8)
                            .build("golden_eye"));

    public static final RegistrySupplier<EntityType<LuggageEntity>> LUGGAGE =
            ENTITIES.register("luggage", () ->
                    EntityType.Builder.<LuggageEntity>of(LuggageEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("luggage"));

    public static final RegistrySupplier<EntityType<MiniMeEntity>> MINI_ME =
            ENTITIES.register("mini_me", () ->
                    EntityType.Builder.<MiniMeEntity>of(MiniMeEntity::new, MobCategory.CREATURE)
                            .sized(0.3f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("mini_me"));

    public static final RegistrySupplier<EntityType<HangGliderEntity>> HANG_GLIDER =
            ENTITIES.register("hang_glider", () ->
                    EntityType.Builder.<HangGliderEntity>of(HangGliderEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("hang_glider"));

    public static final RegistrySupplier<EntityType<CartographerEntity>> CARTOGRAPHER =
            ENTITIES.register("cartographer", () ->
                    EntityType.Builder.<CartographerEntity>of(CartographerEntity::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)
                            .clientTrackingRange(64)
                            .updateInterval(8)
                            .build("cartographer"));

    public static final RegistrySupplier<EntityType<MagnetEntity>> MAGNET =
            ENTITIES.register("magnet", () ->
                    EntityType.Builder.<MagnetEntity>of(MagnetEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("magnet"));

    public static void register() {
        ENTITIES.register();
    }

    private OpenBlocksEntities() {}
}
