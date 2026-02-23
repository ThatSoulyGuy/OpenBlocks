package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.automation.*;
import com.openblocks.canvas.CanvasBlockEntity;
import com.openblocks.canvas.PaintCanBlockEntity;
import com.openblocks.canvas.PaintMixerBlockEntity;
import com.openblocks.guide.GuideBlockEntity;
import com.openblocks.goldenegg.GoldenEggBlockEntity;
import com.openblocks.projector.ProjectorBlockEntity;
import com.openblocks.imaginary.DrawingTableBlockEntity;
import com.openblocks.imaginary.ImaginaryBlockEntity;
import com.openblocks.trophy.TrophyBlockEntity;
import com.openblocks.decoration.FlagBlockEntity;
import com.openblocks.elevator.ElevatorRotatingBlockEntity;
import com.openblocks.grave.GraveBlockEntity;
import com.openblocks.interaction.*;
import com.openblocks.tank.TankBlockEntity;
import com.openblocks.tank.XpBottlerBlockEntity;
import com.openblocks.tank.XpDrainBlockEntity;
import com.openblocks.tank.XpShowerBlockEntity;
import com.openblocks.utility.HealBlockEntity;
import com.openblocks.utility.SkyBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class OpenBlocksBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<ElevatorRotatingBlockEntity>> ELEVATOR_ROTATING =
            BLOCK_ENTITIES.register("elevator_rotating",
                    () -> BlockEntityType.Builder.of(ElevatorRotatingBlockEntity::new,
                            OpenBlocksBlocks.ELEVATOR_ROTATING.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<FlagBlockEntity>> FLAG =
            BLOCK_ENTITIES.register("flag",
                    () -> BlockEntityType.Builder.of(FlagBlockEntity::new,
                            OpenBlocksBlocks.FLAG.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<BigButtonBlockEntity>> BIG_BUTTON =
            BLOCK_ENTITIES.register("big_button",
                    () -> BlockEntityType.Builder.of(BigButtonBlockEntity::new,
                            OpenBlocksBlocks.BIG_BUTTON.get(),
                            OpenBlocksBlocks.BIG_BUTTON_WOOD.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<SkyBlockEntity>> SKY =
            BLOCK_ENTITIES.register("sky",
                    () -> BlockEntityType.Builder.of(SkyBlockEntity::new,
                            OpenBlocksBlocks.SKY.get(),
                            OpenBlocksBlocks.SKY_INVERTED.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<HealBlockEntity>> HEAL =
            BLOCK_ENTITIES.register("heal",
                    () -> BlockEntityType.Builder.of(HealBlockEntity::new,
                            OpenBlocksBlocks.HEAL.get()).build(null));

    // --- Phase 2: Interaction Block Entities ---

    public static final RegistrySupplier<BlockEntityType<FanBlockEntity>> FAN =
            BLOCK_ENTITIES.register("fan",
                    () -> BlockEntityType.Builder.of(FanBlockEntity::new,
                            OpenBlocksBlocks.FAN.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<BearTrapBlockEntity>> BEAR_TRAP =
            BLOCK_ENTITIES.register("bear_trap",
                    () -> BlockEntityType.Builder.of(BearTrapBlockEntity::new,
                            OpenBlocksBlocks.BEAR_TRAP.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<TargetBlockEntity>> TARGET =
            BLOCK_ENTITIES.register("target",
                    () -> BlockEntityType.Builder.of(TargetBlockEntity::new,
                            OpenBlocksBlocks.TARGET.get()).build(null));

    // --- Phase 2: Complex Block Entities ---

    public static final RegistrySupplier<BlockEntityType<SprinklerBlockEntity>> SPRINKLER =
            BLOCK_ENTITIES.register("sprinkler",
                    () -> BlockEntityType.Builder.of(SprinklerBlockEntity::new,
                            OpenBlocksBlocks.SPRINKLER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<CannonBlockEntity>> CANNON =
            BLOCK_ENTITIES.register("cannon",
                    () -> BlockEntityType.Builder.of(CannonBlockEntity::new,
                            OpenBlocksBlocks.CANNON.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<VillageHighlighterBlockEntity>> VILLAGE_HIGHLIGHTER =
            BLOCK_ENTITIES.register("village_highlighter",
                    () -> BlockEntityType.Builder.of(VillageHighlighterBlockEntity::new,
                            OpenBlocksBlocks.VILLAGE_HIGHLIGHTER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<DonationStationBlockEntity>> DONATION_STATION =
            BLOCK_ENTITIES.register("donation_station",
                    () -> BlockEntityType.Builder.of(DonationStationBlockEntity::new,
                            OpenBlocksBlocks.DONATION_STATION.get()).build(null));

    // --- Phase 3: Tank ---

    public static final RegistrySupplier<BlockEntityType<TankBlockEntity>> TANK =
            BLOCK_ENTITIES.register("tank",
                    () -> BlockEntityType.Builder.of(TankBlockEntity::new,
                            OpenBlocksBlocks.TANK.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<XpDrainBlockEntity>> XP_DRAIN =
            BLOCK_ENTITIES.register("xp_drain",
                    () -> BlockEntityType.Builder.of(XpDrainBlockEntity::new,
                            OpenBlocksBlocks.XP_DRAIN.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<XpBottlerBlockEntity>> XP_BOTTLER =
            BLOCK_ENTITIES.register("xp_bottler",
                    () -> BlockEntityType.Builder.of(XpBottlerBlockEntity::new,
                            OpenBlocksBlocks.XP_BOTTLER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<XpShowerBlockEntity>> XP_SHOWER =
            BLOCK_ENTITIES.register("xp_shower",
                    () -> BlockEntityType.Builder.of(XpShowerBlockEntity::new,
                            OpenBlocksBlocks.XP_SHOWER.get()).build(null));

    // --- Phase 3: Grave ---

    public static final RegistrySupplier<BlockEntityType<GraveBlockEntity>> GRAVE =
            BLOCK_ENTITIES.register("grave",
                    () -> BlockEntityType.Builder.of(GraveBlockEntity::new,
                            OpenBlocksBlocks.GRAVE.get()).build(null));

    // --- Phase 3: Automation ---

    public static final RegistrySupplier<BlockEntityType<BlockBreakerBlockEntity>> BLOCK_BREAKER =
            BLOCK_ENTITIES.register("block_breaker",
                    () -> BlockEntityType.Builder.of(BlockBreakerBlockEntity::new,
                            OpenBlocksBlocks.BLOCK_BREAKER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<BlockPlacerBlockEntity>> BLOCK_PLACER =
            BLOCK_ENTITIES.register("block_placer",
                    () -> BlockEntityType.Builder.of(BlockPlacerBlockEntity::new,
                            OpenBlocksBlocks.BLOCK_PLACER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<VacuumHopperBlockEntity>> VACUUM_HOPPER =
            BLOCK_ENTITIES.register("vacuum_hopper",
                    () -> BlockEntityType.Builder.of(VacuumHopperBlockEntity::new,
                            OpenBlocksBlocks.VACUUM_HOPPER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<ItemDropperBlockEntity>> ITEM_DROPPER =
            BLOCK_ENTITIES.register("item_dropper",
                    () -> BlockEntityType.Builder.of(ItemDropperBlockEntity::new,
                            OpenBlocksBlocks.ITEM_DROPPER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<AutoAnvilBlockEntity>> AUTO_ANVIL =
            BLOCK_ENTITIES.register("auto_anvil",
                    () -> BlockEntityType.Builder.of(AutoAnvilBlockEntity::new,
                            OpenBlocksBlocks.AUTO_ANVIL.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<AutoEnchantmentTableBlockEntity>> AUTO_ENCHANTMENT_TABLE =
            BLOCK_ENTITIES.register("auto_enchantment_table",
                    () -> BlockEntityType.Builder.of(AutoEnchantmentTableBlockEntity::new,
                            OpenBlocksBlocks.AUTO_ENCHANTMENT_TABLE.get()).build(null));

    // --- Phase 4: Canvas/Paint ---

    public static final RegistrySupplier<BlockEntityType<CanvasBlockEntity>> CANVAS =
            BLOCK_ENTITIES.register("canvas",
                    () -> BlockEntityType.Builder.of(CanvasBlockEntity::new,
                            OpenBlocksBlocks.CANVAS.get(),
                            OpenBlocksBlocks.CANVAS_GLASS.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<PaintCanBlockEntity>> PAINT_CAN =
            BLOCK_ENTITIES.register("paint_can",
                    () -> BlockEntityType.Builder.of(PaintCanBlockEntity::new,
                            OpenBlocksBlocks.PAINT_CAN.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<PaintMixerBlockEntity>> PAINT_MIXER =
            BLOCK_ENTITIES.register("paint_mixer",
                    () -> BlockEntityType.Builder.of(PaintMixerBlockEntity::new,
                            OpenBlocksBlocks.PAINT_MIXER.get()).build(null));

    // --- Phase 8: Imaginary + Guide ---

    public static final RegistrySupplier<BlockEntityType<ImaginaryBlockEntity>> IMAGINARY =
            BLOCK_ENTITIES.register("imaginary",
                    () -> BlockEntityType.Builder.of(ImaginaryBlockEntity::new,
                            OpenBlocksBlocks.IMAGINARY.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<GuideBlockEntity>> GUIDE =
            BLOCK_ENTITIES.register("guide",
                    () -> BlockEntityType.Builder.of(GuideBlockEntity::new,
                            OpenBlocksBlocks.GUIDE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<DrawingTableBlockEntity>> DRAWING_TABLE =
            BLOCK_ENTITIES.register("drawing_table",
                    () -> BlockEntityType.Builder.of(DrawingTableBlockEntity::new,
                            OpenBlocksBlocks.DRAWING_TABLE.get()).build(null));

    // --- Phase 9: Trophy + Golden Egg ---

    public static final RegistrySupplier<BlockEntityType<TrophyBlockEntity>> TROPHY =
            BLOCK_ENTITIES.register("trophy",
                    () -> BlockEntityType.Builder.of(TrophyBlockEntity::new,
                            OpenBlocksBlocks.TROPHY.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<GoldenEggBlockEntity>> GOLDEN_EGG =
            BLOCK_ENTITIES.register("golden_egg",
                    () -> BlockEntityType.Builder.of(GoldenEggBlockEntity::new,
                            OpenBlocksBlocks.GOLDEN_EGG.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<ProjectorBlockEntity>> PROJECTOR =
            BLOCK_ENTITIES.register("projector",
                    () -> BlockEntityType.Builder.of(ProjectorBlockEntity::new,
                            OpenBlocksBlocks.PROJECTOR.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }

    private OpenBlocksBlockEntities() {}
}
