package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class OpenBlocksSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> ELEVATOR_ACTIVATE = SOUNDS.register("elevator_activate",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "elevator_activate")));

    public static final RegistrySupplier<SoundEvent> BEAR_TRAP_CLOSE = SOUNDS.register("bear_trap_close",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "bear_trap_close")));

    public static final RegistrySupplier<SoundEvent> BEAR_TRAP_OPEN = SOUNDS.register("bear_trap_open",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "bear_trap_open")));

    public static final RegistrySupplier<SoundEvent> CANNON_FIRE = SOUNDS.register("cannon_fire",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OpenBlocksConstants.MOD_ID, "cannon_fire")));

    public static void register() {
        SOUNDS.register();
    }

    private OpenBlocksSounds() {}
}
