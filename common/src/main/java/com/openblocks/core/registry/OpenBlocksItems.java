package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.canvas.PaintBrushItem;
import com.openblocks.canvas.SqueegeeItem;
import com.openblocks.entity.*;
import com.openblocks.imaginary.ImaginaryBlockItem;
import com.openblocks.imaginary.StencilItem;
import com.openblocks.imaginary.UnpreparedStencilItem;
import com.openblocks.trophy.TrophyBlockItem;
import com.openblocks.interaction.*;
import com.openblocks.tank.TankBlockItem;
import com.openblocks.utility.EpicEraserItem;
import com.openblocks.utility.SpongeOnAStickItem;
import com.openblocks.utility.TastyClayItem;
import com.openblocks.utility.WrenchItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class OpenBlocksItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.ITEM);

    // --- Block Items ---

    public static final RegistrySupplier<Item> ELEVATOR_ROTATING = ITEMS.register("elevator_rotating",
            () -> new BlockItem(OpenBlocksBlocks.ELEVATOR_ROTATING.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ELEVATOR = ITEMS.register("elevator",
            () -> new BlockItem(OpenBlocksBlocks.ELEVATOR.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LADDER = ITEMS.register("ladder",
            () -> new BlockItem(OpenBlocksBlocks.LADDER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ROPE_LADDER = ITEMS.register("rope_ladder",
            () -> new BlockItem(OpenBlocksBlocks.ROPE_LADDER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> PATH = ITEMS.register("path",
            () -> new BlockItem(OpenBlocksBlocks.PATH.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SPONGE = ITEMS.register("sponge",
            () -> new BlockItem(OpenBlocksBlocks.SPONGE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SCAFFOLDING = ITEMS.register("scaffolding",
            () -> new BlockItem(OpenBlocksBlocks.SCAFFOLDING.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> FLAG = ITEMS.register("flag",
            () -> new BlockItem(OpenBlocksBlocks.FLAG.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BIG_BUTTON = ITEMS.register("big_button",
            () -> new BlockItem(OpenBlocksBlocks.BIG_BUTTON.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BIG_BUTTON_WOOD = ITEMS.register("big_button_wood",
            () -> new BlockItem(OpenBlocksBlocks.BIG_BUTTON_WOOD.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SKY = ITEMS.register("sky",
            () -> new BlockItem(OpenBlocksBlocks.SKY.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SKY_INVERTED = ITEMS.register("sky_inverted",
            () -> new BlockItem(OpenBlocksBlocks.SKY_INVERTED.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> HEAL = ITEMS.register("heal",
            () -> new BlockItem(OpenBlocksBlocks.HEAL.get(), new Item.Properties()));

    // --- Phase 2: Interaction Block Items ---

    public static final RegistrySupplier<Item> FAN = ITEMS.register("fan",
            () -> new BlockItem(OpenBlocksBlocks.FAN.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BEAR_TRAP = ITEMS.register("bear_trap",
            () -> new BlockItem(OpenBlocksBlocks.BEAR_TRAP.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> TARGET = ITEMS.register("target",
            () -> new BlockItem(OpenBlocksBlocks.TARGET.get(), new Item.Properties()));

    // --- Phase 2: Complex Block Items ---

    public static final RegistrySupplier<Item> SPRINKLER = ITEMS.register("sprinkler",
            () -> new BlockItem(OpenBlocksBlocks.SPRINKLER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> CANNON = ITEMS.register("cannon",
            () -> new BlockItem(OpenBlocksBlocks.CANNON.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> VILLAGE_HIGHLIGHTER = ITEMS.register("village_highlighter",
            () -> new BlockItem(OpenBlocksBlocks.VILLAGE_HIGHLIGHTER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> DONATION_STATION = ITEMS.register("donation_station",
            () -> new BlockItem(OpenBlocksBlocks.DONATION_STATION.get(), new Item.Properties()));

    // --- Standalone Items ---

    public static final RegistrySupplier<Item> SPONGE_ON_A_STICK = ITEMS.register("sponge_on_a_stick",
            () -> new SpongeOnAStickItem(new Item.Properties().stacksTo(1).durability(256)));

    public static final RegistrySupplier<Item> TASTY_CLAY = ITEMS.register("tasty_clay",
            () -> new TastyClayItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.1f)
                            .alwaysEdible()
                            .build())));

    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> EPIC_ERASER = ITEMS.register("epic_eraser",
            () -> new EpicEraserItem(new Item.Properties().stacksTo(1).durability(15)));

    // --- Phase 2: Standalone Items ---

    public static final RegistrySupplier<Item> SLIMALYZER = ITEMS.register("slimalyzer",
            () -> new SlimalyzerItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> PEDOMETER = ITEMS.register("pedometer",
            () -> new PedometerItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> SLEEPING_BAG = ITEMS.register("sleeping_bag",
            () -> new SleepingBagItem(new Item.Properties().stacksTo(1)));

    // --- Phase 3: Tank ---

    public static final RegistrySupplier<Item> TANK = ITEMS.register("tank",
            () -> new TankBlockItem(OpenBlocksBlocks.TANK.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> XP_DRAIN = ITEMS.register("xp_drain",
            () -> new BlockItem(OpenBlocksBlocks.XP_DRAIN.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> XP_BOTTLER = ITEMS.register("xp_bottler",
            () -> new BlockItem(OpenBlocksBlocks.XP_BOTTLER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> XP_SHOWER = ITEMS.register("xp_shower",
            () -> new BlockItem(OpenBlocksBlocks.XP_SHOWER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> GRAVE = ITEMS.register("grave",
            () -> new BlockItem(OpenBlocksBlocks.GRAVE.get(), new Item.Properties()));

    // --- Phase 3: Automation ---

    public static final RegistrySupplier<Item> BLOCK_BREAKER = ITEMS.register("block_breaker",
            () -> new BlockItem(OpenBlocksBlocks.BLOCK_BREAKER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> BLOCK_PLACER = ITEMS.register("block_placer",
            () -> new BlockItem(OpenBlocksBlocks.BLOCK_PLACER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> VACUUM_HOPPER = ITEMS.register("vacuum_hopper",
            () -> new BlockItem(OpenBlocksBlocks.VACUUM_HOPPER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ITEM_DROPPER = ITEMS.register("item_dropper",
            () -> new BlockItem(OpenBlocksBlocks.ITEM_DROPPER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> AUTO_ANVIL = ITEMS.register("auto_anvil",
            () -> new BlockItem(OpenBlocksBlocks.AUTO_ANVIL.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> AUTO_ENCHANTMENT_TABLE = ITEMS.register("auto_enchantment_table",
            () -> new BlockItem(OpenBlocksBlocks.AUTO_ENCHANTMENT_TABLE.get(), new Item.Properties()));

    // --- Phase 4: Entity Items ---

    public static final RegistrySupplier<Item> GOLDEN_EYE = ITEMS.register("golden_eye",
            () -> new GoldenEyeItem(new Item.Properties().stacksTo(1).durability(100)));

    public static final RegistrySupplier<Item> LUGGAGE = ITEMS.register("luggage",
            () -> new LuggageItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> HANG_GLIDER = ITEMS.register("hang_glider",
            () -> new HangGliderItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> CARTOGRAPHER = ITEMS.register("cartographer",
            () -> new CartographerItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> MINI_ME = ITEMS.register("mini_me",
            () -> new MiniMeItem(new Item.Properties().stacksTo(1)));

    // --- Phase 4: Canvas/Paint Items ---

    public static final RegistrySupplier<Item> CANVAS = ITEMS.register("canvas",
            () -> new BlockItem(OpenBlocksBlocks.CANVAS.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> CANVAS_GLASS = ITEMS.register("canvas_glass",
            () -> new BlockItem(OpenBlocksBlocks.CANVAS_GLASS.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> PAINT_CAN_ITEM = ITEMS.register("paint_can",
            () -> new BlockItem(OpenBlocksBlocks.PAINT_CAN.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> PAINT_MIXER = ITEMS.register("paint_mixer",
            () -> new BlockItem(OpenBlocksBlocks.PAINT_MIXER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().stacksTo(1).durability(24)));

    public static final RegistrySupplier<Item> SQUEEGEE = ITEMS.register("squeegee",
            () -> new SqueegeeItem(new Item.Properties().stacksTo(1)));

    // --- Phase 7: Cursor + /dev/null ---

    public static final RegistrySupplier<Item> CURSOR = ITEMS.register("cursor",
            () -> new CursorItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> DEV_NULL = ITEMS.register("dev_null",
            () -> new DevNullItem(new Item.Properties().stacksTo(1)));

    // --- Phase 7: Glasses ---

    public static final RegistrySupplier<Item> PENCIL_GLASSES = ITEMS.register("pencil_glasses",
            () -> new PencilGlassesItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> CRAYON_GLASSES = ITEMS.register("crayon_glasses",
            () -> new CrayonGlassesItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> TECHNICOLOR_GLASSES = ITEMS.register("technicolor_glasses",
            () -> new TechnicolorGlassesItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> SONIC_GLASSES = ITEMS.register("sonic_glasses",
            () -> new SonicGlassesItem(new Item.Properties().stacksTo(1)));

    // --- Phase 8: Imaginary ---

    public static final RegistrySupplier<Item> IMAGINARY_ITEM = ITEMS.register("imaginary",
            () -> new ImaginaryBlockItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> GUIDE = ITEMS.register("guide",
            () -> new BlockItem(OpenBlocksBlocks.GUIDE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> DRAWING_TABLE = ITEMS.register("drawing_table",
            () -> new BlockItem(OpenBlocksBlocks.DRAWING_TABLE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> UNPREPARED_STENCIL = ITEMS.register("unprepared_stencil",
            () -> new UnpreparedStencilItem(new Item.Properties()));

    public static final RegistrySupplier<Item> STENCIL = ITEMS.register("stencil",
            () -> new StencilItem(new Item.Properties()));

    // --- Phase 9: Trophy ---

    public static final RegistrySupplier<Item> TROPHY = ITEMS.register("trophy",
            () -> new TrophyBlockItem(OpenBlocksBlocks.TROPHY.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> GOLDEN_EGG = ITEMS.register("golden_egg",
            () -> new BlockItem(OpenBlocksBlocks.GOLDEN_EGG.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> PROJECTOR = ITEMS.register("projector",
            () -> new BlockItem(OpenBlocksBlocks.PROJECTOR.get(), new Item.Properties()));

    public static void register() {
        ITEMS.register();
    }

    private OpenBlocksItems() {}
}
