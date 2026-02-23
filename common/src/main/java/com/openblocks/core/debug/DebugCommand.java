package com.openblocks.core.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.openblocks.OpenBlocksConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;
import java.util.Map;

/**
 * Registers the /openblocks debug command for toggling debug overlays
 * and querying debuggable block entities.
 *
 * Usage:
 *   /openblocks debug                    - Show all feature states
 *   /openblocks debug <feature>          - Toggle a feature's debug mode
 *   /openblocks debug <feature> on|off   - Set explicitly
 *   /openblocks debug all on|off         - Enable/disable all
 *   /openblocks debug info <pos>         - Query IDebuggable at position
 *   /openblocks debug dump               - Dump all loaded OB block entities
 *   /openblocks debug stats              - Show mod statistics
 */
public final class DebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("openblocks")
                        .then(Commands.literal("debug")
                                .requires(src -> src.hasPermission(2))
                                .executes(DebugCommand::showStatus)
                                .then(Commands.literal("all")
                                        .then(Commands.literal("on")
                                                .executes(ctx -> setAll(ctx, true)))
                                        .then(Commands.literal("off")
                                                .executes(ctx -> setAll(ctx, false))))
                                .then(Commands.literal("info")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(DebugCommand::queryInfo)))
                                .then(Commands.literal("dump")
                                        .executes(DebugCommand::dumpAll))
                                .then(Commands.literal("stats")
                                        .executes(DebugCommand::showStats))
                                .then(Commands.literal("profile")
                                        .executes(DebugCommand::showProfile)
                                        .then(Commands.literal("reset")
                                                .executes(DebugCommand::resetProfile)))
                                .then(Commands.argument("feature", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (DebugFeature f : DebugFeature.values()) {
                                                builder.suggest(f.getId());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(DebugCommand::toggleFeature)
                                        .then(Commands.literal("on")
                                                .executes(ctx -> setFeature(ctx, true)))
                                        .then(Commands.literal("off")
                                                .executes(ctx -> setFeature(ctx, false)))))
        );
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("[OpenBlocks Debug Status]"), false);
        Map<DebugFeature, Boolean> states = DebugManager.get().getAllStates();
        for (Map.Entry<DebugFeature, Boolean> entry : states.entrySet()) {
            String status = entry.getValue() ? "ON" : "OFF";
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  " + entry.getKey().getId() + ": " + status +
                            " - " + entry.getKey().getDescription()), false);
        }
        return 1;
    }

    private static int queryInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        ServerLevel level = ctx.getSource().getLevel();

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            ctx.getSource().sendFailure(Component.literal("No block entity at " + pos.toShortString()));
            return 0;
        }

        ResourceLocation beType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Block entity at " + pos.toShortString() +
                        " (" + beType + ")"), false);

        if (be instanceof IDebuggable debuggable) {
            List<String> info = debuggable.getDebugInfo();
            for (String line : info) {
                ctx.getSource().sendSuccess(() -> Component.literal("  " + line), false);
            }
            return info.size();
        } else {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  (not IDebuggable)"), false);
            return 1;
        }
    }

    private static int dumpAll(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Dumping loaded OpenBlocks block entities..."), false);

        // Search loaded chunks around the player for OB block entities
        BlockPos center = BlockPos.containing(ctx.getSource().getPosition());
        int searchRadius = 8; // chunks
        int count = 0;

        for (int cx = -searchRadius; cx <= searchRadius; cx++) {
            for (int cz = -searchRadius; cz <= searchRadius; cz++) {
                int chunkX = (center.getX() >> 4) + cx;
                int chunkZ = (center.getZ() >> 4) + cz;
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();
                    ResourceLocation beType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
                    if (beType != null && beType.getNamespace().equals(OpenBlocksConstants.MOD_ID)) {
                        count++;
                        BlockPos pos = be.getBlockPos();
                        String posStr = pos.toShortString();
                        String typeName = beType.getPath();

                        if (be instanceof IDebuggable debuggable) {
                            List<String> info = debuggable.getDebugInfo();
                            String summary = info.isEmpty() ? "" : " - " + String.join(", ", info);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("  " + typeName + " @ " + posStr + summary), false);
                        } else {
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("  " + typeName + " @ " + posStr), false);
                        }
                    }
                }
            }
        }

        int finalCount = count;
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Found " + finalCount + " block entities in loaded chunks"), false);
        return count;
    }

    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Mod Statistics:"), false);

        // Count registered blocks
        long blockCount = BuiltInRegistries.BLOCK.keySet().stream()
                .filter(rl -> rl.getNamespace().equals(OpenBlocksConstants.MOD_ID))
                .count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Registered blocks: " + blockCount), false);

        // Count registered items
        long itemCount = BuiltInRegistries.ITEM.keySet().stream()
                .filter(rl -> rl.getNamespace().equals(OpenBlocksConstants.MOD_ID))
                .count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Registered items: " + itemCount), false);

        // Count registered block entity types
        long beTypeCount = BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().stream()
                .filter(rl -> rl.getNamespace().equals(OpenBlocksConstants.MOD_ID))
                .count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Registered block entity types: " + beTypeCount), false);

        // Count registered entity types
        long entityTypeCount = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                .filter(rl -> rl.getNamespace().equals(OpenBlocksConstants.MOD_ID))
                .count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Registered entity types: " + entityTypeCount), false);

        // Count registered sounds
        long soundCount = BuiltInRegistries.SOUND_EVENT.keySet().stream()
                .filter(rl -> rl.getNamespace().equals(OpenBlocksConstants.MOD_ID))
                .count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Registered sounds: " + soundCount), false);

        // Count registered recipes
        ServerLevel level = ctx.getSource().getLevel();
        long recipeCount = level.getRecipeManager().getRecipes().stream()
                .filter(h -> h.id().getNamespace().equals(OpenBlocksConstants.MOD_ID))
                .count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Registered recipes: " + recipeCount), false);

        // Debug features state
        long enabledCount = DebugManager.get().getAllStates().values().stream()
                .filter(b -> b).count();
        ctx.getSource().sendSuccess(
                () -> Component.literal("  Debug features enabled: " + enabledCount + "/" +
                        DebugFeature.values().length), false);

        return 1;
    }

    private static int toggleFeature(CommandContext<CommandSourceStack> ctx) {
        String featureId = StringArgumentType.getString(ctx, "feature");
        DebugFeature feature = DebugFeature.fromId(featureId);
        if (feature == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown feature: " + featureId));
            return 0;
        }
        DebugManager.get().toggle(feature);
        boolean now = DebugManager.get().isEnabled(feature);
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] " + feature.getId() + " debug: " + (now ? "ON" : "OFF")), false);
        return 1;
    }

    private static int setFeature(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        String featureId = StringArgumentType.getString(ctx, "feature");
        DebugFeature feature = DebugFeature.fromId(featureId);
        if (feature == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown feature: " + featureId));
            return 0;
        }
        DebugManager.get().setEnabled(feature, enabled);
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] " + feature.getId() + " debug: " + (enabled ? "ON" : "OFF")), false);
        return 1;
    }

    private static int showProfile(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Profiling Results:"), false);
        Map<DebugFeature, Long> results = DebugManager.get().getProfilingResults();
        if (results.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  No profiling data. Enable debug features and wait."), false);
            return 0;
        }
        for (Map.Entry<DebugFeature, Long> entry : results.entrySet()) {
            double ms = entry.getValue() / 1_000_000.0;
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  " + entry.getKey().getId() + ": " +
                            String.format("%.2f", ms) + " ms total"), false);
        }
        return 1;
    }

    private static int resetProfile(CommandContext<CommandSourceStack> ctx) {
        DebugManager.get().resetProfiling();
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Profiling data reset."), false);
        return 1;
    }

    private static int setAll(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        DebugManager.get().setAll(enabled);
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] All debug features: " + (enabled ? "ON" : "OFF")), false);
        return 1;
    }

    private DebugCommand() {}
}
