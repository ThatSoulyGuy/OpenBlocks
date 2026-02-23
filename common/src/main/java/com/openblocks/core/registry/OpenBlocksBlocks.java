package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.canvas.*;
import com.openblocks.decoration.FlagBlock;
import com.openblocks.decoration.PathBlock;
import com.openblocks.elevator.ElevatorBlock;
import com.openblocks.elevator.ElevatorRotatingBlock;
import com.openblocks.automation.*;
import com.openblocks.goldenegg.GoldenEggBlock;
import com.openblocks.grave.GraveBlock;
import com.openblocks.guide.GuideBlock;
import com.openblocks.projector.ProjectorBlock;
import com.openblocks.imaginary.DrawingTableBlock;
import com.openblocks.imaginary.ImaginaryBlock;
import com.openblocks.trophy.TrophyBlock;
import com.openblocks.interaction.*;
import com.openblocks.tank.TankBlock;
import com.openblocks.tank.XpBottlerBlock;
import com.openblocks.tank.XpDrainBlock;
import com.openblocks.tank.XpShowerBlock;
import com.openblocks.utility.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class OpenBlocksBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.BLOCK);

    // --- Elevator ---

    public static final RegistrySupplier<Block> ELEVATOR_ROTATING = BLOCKS.register("elevator_rotating",
            () -> new ElevatorRotatingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .strength(2.0f)
                    .sound(SoundType.WOOL)));

    // --- Phase 1: Simple Blocks ---

    public static final RegistrySupplier<Block> ELEVATOR = BLOCKS.register("elevator",
            () -> new ElevatorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .strength(2.0f)
                    .sound(SoundType.WOOL)));

    public static final RegistrySupplier<Block> LADDER = BLOCKS.register("ladder",
            () -> new JadedLadderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0f)
                    .sound(SoundType.LADDER)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> ROPE_LADDER = BLOCKS.register("rope_ladder",
            () -> new RopeLadderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(0.4f)
                    .sound(SoundType.LADDER)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> PATH = BLOCKS.register("path",
            () -> new PathBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> SPONGE = BLOCKS.register("sponge",
            () -> new OpenBlocksSpongeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.6f)
                    .sound(SoundType.GRASS)));

    public static final RegistrySupplier<Block> SCAFFOLDING = BLOCKS.register("scaffolding",
            () -> new OpenBlocksScaffoldingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.1f)
                    .sound(SoundType.SCAFFOLDING)
                    .noOcclusion()));

    // --- Phase 1: Blocks with Block Entities ---

    public static final RegistrySupplier<Block> FLAG = BLOCKS.register("flag",
            () -> new FlagBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .instabreak()
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> BIG_BUTTON = BLOCKS.register("big_button",
            () -> new BigButtonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5f)
                    .sound(SoundType.STONE)
                    .noOcclusion(), false));

    public static final RegistrySupplier<Block> BIG_BUTTON_WOOD = BLOCKS.register("big_button_wood",
            () -> new BigButtonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(0.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion(), true));

    public static final RegistrySupplier<Block> SKY = BLOCKS.register("sky",
            () -> new SkyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f)
                    .requiresCorrectToolForDrops(), false));

    public static final RegistrySupplier<Block> SKY_INVERTED = BLOCKS.register("sky_inverted",
            () -> new SkyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f)
                    .requiresCorrectToolForDrops(), true));

    public static final RegistrySupplier<Block> HEAL = BLOCKS.register("heal",
            () -> new HealBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(2.0f)));

    // --- Phase 2: Interaction Blocks ---

    public static final RegistrySupplier<Block> FAN = BLOCKS.register("fan",
            () -> new FanBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)));

    public static final RegistrySupplier<Block> BEAR_TRAP = BLOCKS.register("bear_trap",
            () -> new BearTrapBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> TARGET = BLOCKS.register("target",
            () -> new TargetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .strength(0.5f)
                    .sound(SoundType.WOOL)));

    public static final RegistrySupplier<Block> SPRINKLER = BLOCKS.register("sprinkler",
            () -> new SprinklerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> CANNON = BLOCKS.register("cannon",
            () -> new CannonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)));

    public static final RegistrySupplier<Block> VILLAGE_HIGHLIGHTER = BLOCKS.register("village_highlighter",
            () -> new VillageHighlighterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f)
                    .sound(SoundType.STONE)));

    public static final RegistrySupplier<Block> DONATION_STATION = BLOCKS.register("donation_station",
            () -> new DonationStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f)
                    .sound(SoundType.METAL)));

    // --- Phase 3: Tank ---

    public static final RegistrySupplier<Block> TANK = BLOCKS.register("tank",
            () -> new TankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> XP_DRAIN = BLOCKS.register("xp_drain",
            () -> new XpDrainBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f)
                    .sound(SoundType.METAL)));

    public static final RegistrySupplier<Block> XP_BOTTLER = BLOCKS.register("xp_bottler",
            () -> new XpBottlerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f)
                    .sound(SoundType.METAL)));

    public static final RegistrySupplier<Block> XP_SHOWER = BLOCKS.register("xp_shower",
            () -> new XpShowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f)
                    .sound(SoundType.METAL)));

    // --- Phase 3: Grave ---

    public static final RegistrySupplier<Block> GRAVE = BLOCKS.register("grave",
            () -> new GraveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f, 2000.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    // --- Phase 3: Automation ---

    public static final RegistrySupplier<Block> BLOCK_BREAKER = BLOCKS.register("block_breaker",
            () -> new BlockBreakerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> BLOCK_PLACER = BLOCKS.register("block_placer",
            () -> new BlockPlacerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> VACUUM_HOPPER = BLOCKS.register("vacuum_hopper",
            () -> new VacuumHopperBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> ITEM_DROPPER = BLOCKS.register("item_dropper",
            () -> new ItemDropperBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> AUTO_ANVIL = BLOCKS.register("auto_anvil",
            () -> new AutoAnvilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 1200.0f)
                    .sound(SoundType.ANVIL)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> AUTO_ENCHANTMENT_TABLE = BLOCKS.register("auto_enchantment_table",
            () -> new AutoEnchantmentTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // --- Phase 4: Canvas/Paint ---

    public static final RegistrySupplier<Block> CANVAS = BLOCKS.register("canvas",
            () -> new CanvasBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .strength(1.0f)
                    .sound(SoundType.WOOL)));

    public static final RegistrySupplier<Block> CANVAS_GLASS = BLOCKS.register("canvas_glass",
            () -> new CanvasGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> PAINT_CAN = BLOCKS.register("paint_can",
            () -> new PaintCanBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> PAINT_MIXER = BLOCKS.register("paint_mixer",
            () -> new PaintMixerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .sound(SoundType.STONE)));

    // --- Phase 8: Imaginary ---

    public static final RegistrySupplier<Block> IMAGINARY = BLOCKS.register("imaginary",
            () -> new ImaginaryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.0f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .noLootTable()
                    .isViewBlocking((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)));

    public static final RegistrySupplier<Block> GUIDE = BLOCKS.register("guide",
            () -> new GuideBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 9)
                    .noOcclusion()));

    public static final RegistrySupplier<Block> DRAWING_TABLE = BLOCKS.register("drawing_table",
            () -> new DrawingTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    // --- Phase 9: Trophy ---

    public static final RegistrySupplier<Block> TROPHY = BLOCKS.register("trophy",
            () -> new TrophyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    // --- Phase 9: Golden Egg ---

    public static final RegistrySupplier<Block> GOLDEN_EGG = BLOCKS.register("golden_egg",
            () -> new GoldenEggBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 5)
                    .noOcclusion()));

    // --- Phase 9: Projector ---

    public static final RegistrySupplier<Block> PROJECTOR = BLOCKS.register("projector",
            () -> new ProjectorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(ProjectorBlock.ACTIVE) ? 10 : 0)
                    .noOcclusion()));

    public static void register() {
        BLOCKS.register();
    }

    private OpenBlocksBlocks() {}
}
