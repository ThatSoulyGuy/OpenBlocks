package com.openblocks.neoforge;

import com.openblocks.automation.*;
import com.openblocks.canvas.CanvasBlockEntityRenderer;
import com.openblocks.canvas.PaintBrushItem;
import com.openblocks.canvas.PaintCanBlockEntity;
import com.openblocks.canvas.PaintClientCache;
import com.openblocks.canvas.PaintOverlayRenderer;
import com.openblocks.glyph.GlyphPlacementRenderer;
import com.openblocks.core.debug.DebugOverlay;
import com.openblocks.core.registry.*;
import com.openblocks.core.util.ColorMeta;
import com.openblocks.decoration.FlagBlockEntityRenderer;
import com.openblocks.elevator.ElevatorBlock;
import com.openblocks.elevator.ElevatorInputHandler;
import com.openblocks.elevator.ElevatorRotatingBlock;
import com.openblocks.elevator.ElevatorRotatingBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import com.openblocks.entity.renderer.*;
import com.openblocks.glyph.GlyphEntityRenderer;
import com.openblocks.goldenegg.GoldenEggBlockEntityRenderer;
import com.openblocks.grave.GraveBlockEntityRenderer;
import com.openblocks.guide.GuideBlockEntityRenderer;
import com.openblocks.imaginary.DrawingTableScreen;
import com.openblocks.imaginary.ImaginaryBlockEntityRenderer;
import com.openblocks.interaction.*;
import com.openblocks.projector.ProjectorBlockEntityRenderer;
import com.openblocks.tank.TankBlockEntityRenderer;
import com.openblocks.trophy.TrophyBlockEntityRenderer;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * NeoForge client initialization using native events for correct timing.
 * Entity/BER renderers must be registered during RegisterRenderers (before FMLClientSetupEvent).
 * Color handlers must be registered during RegisterColorHandlers events.
 */
@EventBusSubscriber(modid = "openblocks", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class OpenBlocksNeoForgeClient {

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LuggageRenderer.MODEL_LAYER, LuggageModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Entity renderers
        event.registerEntityRenderer(OpenBlocksEntities.GOLDEN_EYE.get(), GoldenEyeRenderer::new);
        event.registerEntityRenderer(OpenBlocksEntities.LUGGAGE.get(), LuggageRenderer::new);
        event.registerEntityRenderer(OpenBlocksEntities.MINI_ME.get(), MiniMeRenderer::new);
        event.registerEntityRenderer(OpenBlocksEntities.HANG_GLIDER.get(), HangGliderRenderer::new);
        event.registerEntityRenderer(OpenBlocksEntities.CARTOGRAPHER.get(), CartographerRenderer::new);
        event.registerEntityRenderer(OpenBlocksEntities.MAGNET.get(), MagnetRenderer::new);
        event.registerEntityRenderer(OpenBlocksEntities.GLYPH.get(), GlyphEntityRenderer::new);

        // Block entity renderers
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.AUTO_ENCHANTMENT_TABLE.get(), AutoEnchantmentTableBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.TANK.get(), TankBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.CANNON.get(), CannonBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.FLAG.get(), FlagBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.GRAVE.get(), GraveBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.IMAGINARY.get(), ImaginaryBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.GUIDE.get(), GuideBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.TROPHY.get(), TrophyBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.GOLDEN_EGG.get(), GoldenEggBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.PROJECTOR.get(), ProjectorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(OpenBlocksBlockEntities.CANVAS.get(), CanvasBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        // Elevator block: tint by DyeColor from blockstate
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0) {
                        DyeColor dye = state.getValue(ElevatorBlock.COLOR);
                        return ColorMeta.fromDyeColor(dye).getRgb();
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksBlocks.ELEVATOR.get()
        );

        // Elevator rotating block: tint by color from block entity
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex == 0 && level != null && pos != null) {
                        if (level.getBlockEntity(pos) instanceof ElevatorRotatingBlockEntity be) {
                            return ColorMeta.fromDyeColor(be.getColor()).getRgb();
                        }
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksBlocks.ELEVATOR_ROTATING.get()
        );

        // Paint can block: tint by stored color from block entity
        event.register(
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
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // Elevator item: tint by color stored in BLOCK_STATE data component
        event.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        BlockItemStateProperties props = stack.get(DataComponents.BLOCK_STATE);
                        if (props != null) {
                            net.minecraft.world.item.DyeColor dye = props.get(ElevatorBlock.COLOR);
                            if (dye != null) {
                                return ColorMeta.fromDyeColor(dye).getRgb();
                            }
                        }
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksItems.ELEVATOR.get()
        );

        // Elevator rotating item: tint by color stored in CUSTOM_DATA
        event.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        net.minecraft.world.item.DyeColor dye = ElevatorRotatingBlock.getItemColor(stack);
                        if (dye != null) {
                            return ColorMeta.fromDyeColor(dye).getRgb();
                        }
                    }
                    return 0xFFFFFF;
                },
                OpenBlocksItems.ELEVATOR_ROTATING.get()
        );

        // Paint brush item: tint layer 1 by stored color
        event.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 1) {
                        int color = PaintBrushItem.getColor(stack);
                        return color != 0 ? color : -1;
                    }
                    return -1;
                },
                OpenBlocksItems.PAINT_BRUSH.get()
        );
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Trophy item: render pedestal + entity via BEWLR
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return TrophyBEWLR.getInstance();
            }
        }, OpenBlocksItems.TROPHY.get());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Tick events and debug overlay — safe to register during FMLClientSetupEvent
        ClientTickEvent.CLIENT_POST.register(ElevatorInputHandler::onClientTick);
        DebugOverlay.register();

        // Paint: clear client cache on disconnect
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> PaintClientCache.clearAll());

        // Paint overlay rendering for non-canvas blocks
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent e) -> {
            if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            PaintOverlayRenderer.renderAllPaint(e.getPoseStack(), e.getCamera(), bufferSource);
            GlyphPlacementRenderer.render(e.getPoseStack(), e.getCamera(), bufferSource);
        });
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // Screen factories — must use NeoForge's native event (Architectury MenuRegistry is broken on NeoForge 1.21.1)
        event.register(OpenBlocksMenus.VACUUM_HOPPER.get(), VacuumHopperScreen::new);
        event.register(OpenBlocksMenus.ITEM_DROPPER.get(), ItemDropperScreen::new);
        event.register(OpenBlocksMenus.AUTO_ANVIL.get(), AutoAnvilScreen::new);
        event.register(OpenBlocksMenus.AUTO_ENCHANTMENT_TABLE.get(), AutoEnchantmentTableScreen::new);
        event.register(OpenBlocksMenus.BLOCK_PLACER.get(), BlockPlacerScreen::new);
        event.register(OpenBlocksMenus.DONATION_STATION.get(), DonationStationScreen::new);
        event.register(OpenBlocksMenus.DEV_NULL.get(), DevNullScreen::new);
        event.register(OpenBlocksMenus.DRAWING_TABLE.get(), DrawingTableScreen::new);
    }
}
