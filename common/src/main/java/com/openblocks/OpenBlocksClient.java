package com.openblocks;

import com.openblocks.automation.*;
import com.openblocks.canvas.PaintBrushItem;
import com.openblocks.canvas.PaintCanBlockEntity;
import com.openblocks.core.debug.DebugOverlay;
import com.openblocks.core.registry.*;
import com.openblocks.core.util.ColorMeta;
import com.openblocks.decoration.FlagBlockEntity;
import com.openblocks.decoration.FlagBlockEntityRenderer;
import com.openblocks.elevator.ElevatorBlock;
import com.openblocks.elevator.ElevatorInputHandler;
import com.openblocks.entity.renderer.*;
import com.openblocks.grave.GraveBlockEntityRenderer;
import com.openblocks.guide.GuideBlockEntityRenderer;
import com.openblocks.imaginary.DrawingTableScreen;
import com.openblocks.imaginary.ImaginaryBlockEntityRenderer;
import com.openblocks.interaction.CannonBlockEntityRenderer;
import com.openblocks.interaction.DevNullScreen;
import com.openblocks.interaction.DonationStationScreen;
import com.openblocks.goldenegg.GoldenEggBlockEntityRenderer;
import com.openblocks.projector.ProjectorBlockEntityRenderer;
import com.openblocks.tank.TankBlockEntityRenderer;
import com.openblocks.trophy.TrophyBlockEntityRenderer;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.world.item.DyeColor;

/**
 * Common client-side initialization. Called by both Fabric and NeoForge client entrypoints.
 */
public final class OpenBlocksClient {

    public static void init() {
        // Elevator: detect jump/sneak transitions for elevator teleportation
        ClientTickEvent.CLIENT_POST.register(ElevatorInputHandler::onClientTick);

        // Entity model layers
        EntityModelLayerRegistry.register(LuggageRenderer.MODEL_LAYER, LuggageModel::createBodyLayer);

        // Entity renderers
        EntityRendererRegistry.register(OpenBlocksEntities.GOLDEN_EYE, GoldenEyeRenderer::new);
        EntityRendererRegistry.register(OpenBlocksEntities.LUGGAGE, LuggageRenderer::new);
        EntityRendererRegistry.register(OpenBlocksEntities.MINI_ME, MiniMeRenderer::new);
        EntityRendererRegistry.register(OpenBlocksEntities.HANG_GLIDER, HangGliderRenderer::new);
        EntityRendererRegistry.register(OpenBlocksEntities.CARTOGRAPHER, CartographerRenderer::new);
        EntityRendererRegistry.register(OpenBlocksEntities.MAGNET, MagnetRenderer::new);

        // Block entity renderers
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.TANK.get(), TankBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.CANNON.get(), CannonBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.FLAG.get(), FlagBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.GRAVE.get(), GraveBlockEntityRenderer::new);

        // Phase 8 BERs
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.IMAGINARY.get(), ImaginaryBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.GUIDE.get(), GuideBlockEntityRenderer::new);

        // Phase 9 BERs
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.TROPHY.get(), TrophyBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.GOLDEN_EGG.get(), GoldenEggBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(OpenBlocksBlockEntities.PROJECTOR.get(), ProjectorBlockEntityRenderer::new);

        // Debug overlay
        DebugOverlay.register();

        registerColorHandlers();
        registerScreenFactories();
    }

    private static void registerScreenFactories() {
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.VACUUM_HOPPER.get(), VacuumHopperScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.ITEM_DROPPER.get(), ItemDropperScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.AUTO_ANVIL.get(), AutoAnvilScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.AUTO_ENCHANTMENT_TABLE.get(), AutoEnchantmentTableScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.BLOCK_PLACER.get(), BlockPlacerScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.DONATION_STATION.get(), DonationStationScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.DEV_NULL.get(), DevNullScreen::new);
        MenuRegistry.registerScreenFactory(OpenBlocksMenus.DRAWING_TABLE.get(), DrawingTableScreen::new);
    }

    private static void registerColorHandlers() {
        // Block color handlers

        // Elevator blocks: tint by DyeColor from blockstate
        ColorHandlerRegistry.registerBlockColors(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0) {
                        DyeColor dye = state.getValue(ElevatorBlock.COLOR);
                        return ColorMeta.fromDyeColor(dye).getRgb();
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksBlocks.ELEVATOR.get(), OpenBlocksBlocks.ELEVATOR_ROTATING.get()
        );

        // Flag block: tint by color from block entity
        ColorHandlerRegistry.registerBlockColors(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0 && level != null && pos != null) {
                        if (level.getBlockEntity(pos) instanceof FlagBlockEntity flag) {
                            return flag.getColor().getRgb();
                        }
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksBlocks.FLAG.get()
        );

        // Paint can block: tint by stored color from block entity
        ColorHandlerRegistry.registerBlockColors(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0 && level != null && pos != null) {
                        if (level.getBlockEntity(pos) instanceof PaintCanBlockEntity paintCan) {
                            return paintCan.getColor();
                        }
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksBlocks.PAINT_CAN.get()
        );

        // Item color handlers

        // Elevator block items: tint by default white (actual color set on placement)
        ColorHandlerRegistry.registerItemColors(
                (stack, tintIndex) -> tintIndex == 0 ? 0xFFFFFF : 0xFFFFFF,
                OpenBlocksItems.ELEVATOR.get(), OpenBlocksItems.ELEVATOR_ROTATING.get()
        );

        // Paint brush item: tint layer 0 by stored color
        ColorHandlerRegistry.registerItemColors(
                (stack, tintIndex) -> {
                    if (tintIndex == 1) {
                        int color = PaintBrushItem.getColor(stack);
                        return color != 0 ? color : 0xFFFFFF;
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksItems.PAINT_BRUSH.get()
        );
    }

    private OpenBlocksClient() {}
}
