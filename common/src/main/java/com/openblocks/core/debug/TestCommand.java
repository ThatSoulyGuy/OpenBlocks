package com.openblocks.core.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.openblocks.core.registry.OpenBlocksBlocks;
import com.openblocks.core.registry.OpenBlocksEntities;
import com.openblocks.core.registry.OpenBlocksItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * In-game test commands under /openblocks test.
 *
 * Usage:
 *   /openblocks test give <category>     — Give all items from a category
 *   /openblocks test blocks <category>   — Place a row of blocks from a category
 *   /openblocks test entity <type>       — Spawn a test entity
 *   /openblocks test recipe verify       — Verify all openblocks recipes
 *   /openblocks test all                 — Run all tests
 */
public final class TestCommand {

    private static final Map<String, List<RegistrySupplier<Item>>> ITEM_CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, List<RegistrySupplier<Block>>> BLOCK_CATEGORIES = new LinkedHashMap<>();

    static {
        // Item categories
        ITEM_CATEGORIES.put("elevator", List.of(
                OpenBlocksItems.ELEVATOR, OpenBlocksItems.ELEVATOR_ROTATING));
        ITEM_CATEGORIES.put("utility", List.of(
                OpenBlocksItems.SPONGE, OpenBlocksItems.LADDER, OpenBlocksItems.ROPE_LADDER,
                OpenBlocksItems.SCAFFOLDING, OpenBlocksItems.PATH, OpenBlocksItems.FLAG,
                OpenBlocksItems.HEAL, OpenBlocksItems.SKY, OpenBlocksItems.SKY_INVERTED,
                OpenBlocksItems.SPONGE_ON_A_STICK, OpenBlocksItems.TASTY_CLAY,
                OpenBlocksItems.WRENCH, OpenBlocksItems.EPIC_ERASER));
        ITEM_CATEGORIES.put("interaction", List.of(
                OpenBlocksItems.BEAR_TRAP, OpenBlocksItems.TARGET, OpenBlocksItems.FAN,
                OpenBlocksItems.SPRINKLER, OpenBlocksItems.CANNON, OpenBlocksItems.BIG_BUTTON,
                OpenBlocksItems.BIG_BUTTON_WOOD, OpenBlocksItems.VILLAGE_HIGHLIGHTER,
                OpenBlocksItems.DONATION_STATION, OpenBlocksItems.SLEEPING_BAG,
                OpenBlocksItems.SLIMALYZER, OpenBlocksItems.PEDOMETER));
        ITEM_CATEGORIES.put("tank", List.of(
                OpenBlocksItems.TANK, OpenBlocksItems.XP_DRAIN,
                OpenBlocksItems.XP_BOTTLER, OpenBlocksItems.XP_SHOWER));
        ITEM_CATEGORIES.put("automation", List.of(
                OpenBlocksItems.BLOCK_BREAKER, OpenBlocksItems.BLOCK_PLACER,
                OpenBlocksItems.VACUUM_HOPPER, OpenBlocksItems.ITEM_DROPPER,
                OpenBlocksItems.AUTO_ANVIL, OpenBlocksItems.AUTO_ENCHANTMENT_TABLE));
        ITEM_CATEGORIES.put("canvas", List.of(
                OpenBlocksItems.CANVAS, OpenBlocksItems.CANVAS_GLASS,
                OpenBlocksItems.PAINT_CAN_ITEM, OpenBlocksItems.PAINT_MIXER,
                OpenBlocksItems.PAINT_BRUSH, OpenBlocksItems.SQUEEGEE));
        ITEM_CATEGORIES.put("entity", List.of(
                OpenBlocksItems.GOLDEN_EYE, OpenBlocksItems.LUGGAGE,
                OpenBlocksItems.HANG_GLIDER, OpenBlocksItems.CARTOGRAPHER,
                OpenBlocksItems.MINI_ME));
        ITEM_CATEGORIES.put("glasses", List.of(
                OpenBlocksItems.PENCIL_GLASSES, OpenBlocksItems.CRAYON_GLASSES,
                OpenBlocksItems.TECHNICOLOR_GLASSES, OpenBlocksItems.SONIC_GLASSES));
        ITEM_CATEGORIES.put("tools", List.of(
                OpenBlocksItems.CURSOR, OpenBlocksItems.DEV_NULL));
        ITEM_CATEGORIES.put("imaginary", List.of(
                OpenBlocksItems.IMAGINARY_ITEM, OpenBlocksItems.GUIDE,
                OpenBlocksItems.DRAWING_TABLE, OpenBlocksItems.UNPREPARED_STENCIL,
                OpenBlocksItems.STENCIL));
        ITEM_CATEGORIES.put("trophy", List.of(
                OpenBlocksItems.TROPHY, OpenBlocksItems.GOLDEN_EGG,
                OpenBlocksItems.PROJECTOR));

        // Block categories
        BLOCK_CATEGORIES.put("elevator", List.of(
                OpenBlocksBlocks.ELEVATOR, OpenBlocksBlocks.ELEVATOR_ROTATING));
        BLOCK_CATEGORIES.put("utility", List.of(
                OpenBlocksBlocks.SPONGE, OpenBlocksBlocks.LADDER, OpenBlocksBlocks.ROPE_LADDER,
                OpenBlocksBlocks.SCAFFOLDING, OpenBlocksBlocks.PATH, OpenBlocksBlocks.FLAG,
                OpenBlocksBlocks.HEAL, OpenBlocksBlocks.SKY, OpenBlocksBlocks.SKY_INVERTED));
        BLOCK_CATEGORIES.put("interaction", List.of(
                OpenBlocksBlocks.BEAR_TRAP, OpenBlocksBlocks.TARGET, OpenBlocksBlocks.FAN,
                OpenBlocksBlocks.SPRINKLER, OpenBlocksBlocks.CANNON, OpenBlocksBlocks.BIG_BUTTON,
                OpenBlocksBlocks.BIG_BUTTON_WOOD, OpenBlocksBlocks.VILLAGE_HIGHLIGHTER,
                OpenBlocksBlocks.DONATION_STATION));
        BLOCK_CATEGORIES.put("tank", List.of(
                OpenBlocksBlocks.TANK, OpenBlocksBlocks.XP_DRAIN,
                OpenBlocksBlocks.XP_BOTTLER, OpenBlocksBlocks.XP_SHOWER));
        BLOCK_CATEGORIES.put("automation", List.of(
                OpenBlocksBlocks.BLOCK_BREAKER, OpenBlocksBlocks.BLOCK_PLACER,
                OpenBlocksBlocks.VACUUM_HOPPER, OpenBlocksBlocks.ITEM_DROPPER,
                OpenBlocksBlocks.AUTO_ANVIL, OpenBlocksBlocks.AUTO_ENCHANTMENT_TABLE));
        BLOCK_CATEGORIES.put("canvas", List.of(
                OpenBlocksBlocks.CANVAS, OpenBlocksBlocks.CANVAS_GLASS,
                OpenBlocksBlocks.PAINT_CAN, OpenBlocksBlocks.PAINT_MIXER));
        BLOCK_CATEGORIES.put("imaginary", List.of(
                OpenBlocksBlocks.IMAGINARY, OpenBlocksBlocks.GUIDE,
                OpenBlocksBlocks.DRAWING_TABLE));
        BLOCK_CATEGORIES.put("trophy", List.of(
                OpenBlocksBlocks.TROPHY, OpenBlocksBlocks.GOLDEN_EGG,
                OpenBlocksBlocks.PROJECTOR));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("openblocks")
                        .then(Commands.literal("test")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("give")
                                        .then(Commands.argument("category", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    ITEM_CATEGORIES.keySet().forEach(builder::suggest);
                                                    builder.suggest("all");
                                                    return builder.buildFuture();
                                                })
                                                .executes(TestCommand::giveItems)))
                                .then(Commands.literal("blocks")
                                        .then(Commands.argument("category", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    BLOCK_CATEGORIES.keySet().forEach(builder::suggest);
                                                    builder.suggest("all");
                                                    return builder.buildFuture();
                                                })
                                                .executes(TestCommand::placeBlocks)))
                                .then(Commands.literal("entity")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    builder.suggest("golden_eye");
                                                    builder.suggest("luggage");
                                                    builder.suggest("mini_me");
                                                    builder.suggest("hang_glider");
                                                    builder.suggest("cartographer");
                                                    builder.suggest("magnet");
                                                    builder.suggest("all");
                                                    return builder.buildFuture();
                                                })
                                                .executes(TestCommand::spawnEntity)))
                                .then(Commands.literal("recipe")
                                        .then(Commands.literal("verify")
                                                .executes(TestCommand::verifyRecipes)))
                                .then(Commands.literal("all")
                                        .executes(TestCommand::runAll)))
        );
    }

    private static int giveItems(CommandContext<CommandSourceStack> ctx) {
        String category = StringArgumentType.getString(ctx, "category");
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        List<String> categories = category.equals("all")
                ? new ArrayList<>(ITEM_CATEGORIES.keySet())
                : List.of(category);

        int count = 0;
        for (String cat : categories) {
            List<RegistrySupplier<Item>> items = ITEM_CATEGORIES.get(cat);
            if (items == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown category: " + cat));
                return 0;
            }
            for (RegistrySupplier<Item> supplier : items) {
                ItemStack stack = new ItemStack(supplier.get());
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                count++;
            }
        }

        int finalCount = count;
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Gave " + finalCount + " items" +
                        (category.equals("all") ? " (all categories)" : " (" + category + ")")),
                true);
        return count;
    }

    private static int placeBlocks(CommandContext<CommandSourceStack> ctx) {
        String category = StringArgumentType.getString(ctx, "category");
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        List<String> categories = category.equals("all")
                ? new ArrayList<>(BLOCK_CATEGORIES.keySet())
                : List.of(category);

        BlockPos startPos = player.blockPosition().relative(Direction.EAST, 2);
        int offset = 0;
        int count = 0;

        for (String cat : categories) {
            List<RegistrySupplier<Block>> blocks = BLOCK_CATEGORIES.get(cat);
            if (blocks == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown category: " + cat));
                return 0;
            }
            for (RegistrySupplier<Block> supplier : blocks) {
                BlockPos placePos = startPos.relative(Direction.EAST, offset);
                BlockState state = supplier.get().defaultBlockState();
                level.setBlock(placePos, state, 3);
                offset += 2;
                count++;
            }
        }

        int finalCount = count;
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Placed " + finalCount + " blocks starting east of you" +
                        (category.equals("all") ? " (all categories)" : " (" + category + ")")),
                true);
        return count;
    }

    @SuppressWarnings("unchecked")
    private static int spawnEntity(CommandContext<CommandSourceStack> ctx) {
        String typeName = StringArgumentType.getString(ctx, "type");
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        Map<String, RegistrySupplier<? extends EntityType<?>>> entityTypes = new LinkedHashMap<>();
        entityTypes.put("golden_eye", OpenBlocksEntities.GOLDEN_EYE);
        entityTypes.put("luggage", OpenBlocksEntities.LUGGAGE);
        entityTypes.put("mini_me", OpenBlocksEntities.MINI_ME);
        entityTypes.put("hang_glider", OpenBlocksEntities.HANG_GLIDER);
        entityTypes.put("cartographer", OpenBlocksEntities.CARTOGRAPHER);
        entityTypes.put("magnet", OpenBlocksEntities.MAGNET);

        List<String> toSpawn = typeName.equals("all")
                ? new ArrayList<>(entityTypes.keySet())
                : List.of(typeName);

        int count = 0;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        for (String name : toSpawn) {
            RegistrySupplier<? extends EntityType<?>> supplier = entityTypes.get(name);
            if (supplier == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + name));
                return 0;
            }
            EntityType<?> entityType = supplier.get();
            Entity entity = entityType.create(level, null, BlockPos.containing(x + count * 2, y, z + 2),
                    MobSpawnType.COMMAND, false, false);
            if (entity != null) {
                entity.moveTo(x + count * 2, y, z + 2, 0, 0);
                level.addFreshEntity(entity);
                count++;
            }
        }

        int finalCount = count;
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Spawned " + finalCount + " entities" +
                        (typeName.equals("all") ? " (all types)" : " (" + typeName + ")")),
                true);
        return count;
    }

    private static int verifyRecipes(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        Collection<RecipeHolder<?>> allRecipes = level.getRecipeManager().getRecipes();

        int total = 0;
        int valid = 0;
        List<String> broken = new ArrayList<>();

        for (RecipeHolder<?> holder : allRecipes) {
            ResourceLocation id = holder.id();
            if (!id.getNamespace().equals("openblocks")) continue;
            total++;

            ItemStack result = holder.value().getResultItem(level.registryAccess());
            if (result.isEmpty()) {
                broken.add(id.toString() + " (empty result)");
            } else {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
                if (itemId.toString().equals("minecraft:air")) {
                    broken.add(id.toString() + " (air result)");
                } else {
                    valid++;
                }
            }
        }

        int finalTotal = total;
        int finalValid = valid;
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] Recipe verification: " +
                        finalValid + "/" + finalTotal + " recipes valid"), false);

        if (!broken.isEmpty()) {
            for (String b : broken) {
                ctx.getSource().sendFailure(Component.literal("  BROKEN: " + b));
            }
        } else if (total > 0) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  All recipes produce valid outputs!"), false);
        } else {
            ctx.getSource().sendFailure(Component.literal("  No openblocks recipes found!"));
        }

        return valid;
    }

    private static int runAll(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] === Running All Tests ==="), false);

        // Give all items
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            int itemCount = 0;
            for (List<RegistrySupplier<Item>> items : ITEM_CATEGORIES.values()) {
                for (RegistrySupplier<Item> supplier : items) {
                    ItemStack stack = new ItemStack(supplier.get());
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    itemCount++;
                }
            }
            int finalItemCount = itemCount;
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  Gave " + finalItemCount + " items"), false);

            // Place all blocks
            ServerLevel level = player.serverLevel();
            BlockPos startPos = player.blockPosition().relative(Direction.EAST, 2);
            int offset = 0;
            int blockCount = 0;
            for (List<RegistrySupplier<Block>> blocks : BLOCK_CATEGORIES.values()) {
                for (RegistrySupplier<Block> supplier : blocks) {
                    BlockPos placePos = startPos.relative(Direction.EAST, offset);
                    level.setBlock(placePos, supplier.get().defaultBlockState(), 3);
                    offset += 2;
                    blockCount++;
                }
            }
            int finalBlockCount = blockCount;
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  Placed " + finalBlockCount + " blocks"), false);

            // Spawn all entities
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ() - 3;
            @SuppressWarnings("unchecked")
            RegistrySupplier<? extends EntityType<?>>[] entitySuppliers = new RegistrySupplier[] {
                    OpenBlocksEntities.GOLDEN_EYE, OpenBlocksEntities.LUGGAGE,
                    OpenBlocksEntities.MINI_ME, OpenBlocksEntities.HANG_GLIDER,
                    OpenBlocksEntities.CARTOGRAPHER, OpenBlocksEntities.MAGNET
            };
            int entityCount = 0;
            for (int i = 0; i < entitySuppliers.length; i++) {
                EntityType<?> entityType = entitySuppliers[i].get();
                Entity entity = entityType.create(level, null, BlockPos.containing(x + i * 2, y, z),
                        MobSpawnType.COMMAND, false, false);
                if (entity != null) {
                    entity.moveTo(x + i * 2, y, z, 0, 0);
                    level.addFreshEntity(entity);
                    entityCount++;
                }
            }
            int finalEntityCount = entityCount;
            ctx.getSource().sendSuccess(
                    () -> Component.literal("  Spawned " + finalEntityCount + " entities"), false);
        }

        // Verify recipes
        verifyRecipes(ctx);

        ctx.getSource().sendSuccess(
                () -> Component.literal("[OpenBlocks] === All Tests Complete ==="), false);
        return 1;
    }

    private TestCommand() {}
}
