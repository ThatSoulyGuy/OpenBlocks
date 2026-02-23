package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.automation.*;
import com.openblocks.imaginary.DrawingTableMenu;
import com.openblocks.interaction.DevNullMenu;
import com.openblocks.interaction.DonationStationMenu;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class OpenBlocksMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<VacuumHopperMenu>> VACUUM_HOPPER =
            MENUS.register("vacuum_hopper",
                    () -> new MenuType<>(VacuumHopperMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<ItemDropperMenu>> ITEM_DROPPER =
            MENUS.register("item_dropper",
                    () -> new MenuType<>(ItemDropperMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<AutoAnvilMenu>> AUTO_ANVIL =
            MENUS.register("auto_anvil",
                    () -> new MenuType<>(AutoAnvilMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<AutoEnchantmentTableMenu>> AUTO_ENCHANTMENT_TABLE =
            MENUS.register("auto_enchantment_table",
                    () -> new MenuType<>(AutoEnchantmentTableMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<BlockPlacerMenu>> BLOCK_PLACER =
            MENUS.register("block_placer",
                    () -> new MenuType<>(BlockPlacerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<DonationStationMenu>> DONATION_STATION =
            MENUS.register("donation_station",
                    () -> new MenuType<>(DonationStationMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<DevNullMenu>> DEV_NULL =
            MENUS.register("dev_null",
                    () -> new MenuType<>(DevNullMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistrySupplier<MenuType<DrawingTableMenu>> DRAWING_TABLE =
            MENUS.register("drawing_table",
                    () -> new MenuType<>(DrawingTableMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register() {
        MENUS.register();
    }

    private OpenBlocksMenus() {}
}
