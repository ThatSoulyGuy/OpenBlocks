package com.openblocks.core.debug;

import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Map;

/**
 * Client-side debug overlay that shows IDebuggable info when
 * looking at an OpenBlocks block entity with any debug mode enabled.
 */
public final class DebugOverlay {

    private DebugOverlay() {}

    public static void register() {
        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) -> {
            if (!hasAnyDebugEnabled()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            if (!mc.getDebugOverlay().showDebugScreen()) return;

            renderOverlay(mc, graphics);
        });
    }

    private static boolean hasAnyDebugEnabled() {
        Map<DebugFeature, Boolean> states = DebugManager.get().getAllStates();
        return states.values().stream().anyMatch(b -> b);
    }

    private static void renderOverlay(Minecraft mc, GuiGraphics graphics) {
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (blockHit.getType() == HitResult.Type.MISS) return;

        BlockPos pos = blockHit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof IDebuggable debuggable)) return;

        List<String> info = debuggable.getDebugInfo();
        if (info.isEmpty()) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = screenWidth - 5;
        int y = 2;

        for (String line : info) {
            int textWidth = mc.font.width(line);
            int drawX = x - textWidth;
            graphics.fill(drawX - 2, y - 1, x + 2, y + mc.font.lineHeight + 1, 0x80000000);
            graphics.drawString(mc.font, line, drawX, y, 0x00FF00, false);
            y += mc.font.lineHeight + 2;
        }
    }
}
