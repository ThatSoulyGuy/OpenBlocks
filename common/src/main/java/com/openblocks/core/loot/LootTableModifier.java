package com.openblocks.core.loot;

import com.openblocks.core.config.OpenBlocksConfig;
import com.openblocks.core.registry.OpenBlocksItems;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Modifies vanilla loot tables to inject OpenBlocks items.
 * Currently adds Technicolor Glasses to dungeon/temple/mineshaft/stronghold chests.
 */
public final class LootTableModifier {

    private static final float TECHNICOLOR_GLASSES_CHANCE = 0.05f;

    private LootTableModifier() {}

    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin) return;
            if (!OpenBlocksConfig.Loot.technicolorGlasses) return;

            String path = key.location().getPath();
            if (isDungeonChest(path)) {
                context.addPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(TECHNICOLOR_GLASSES_CHANCE))
                        .add(LootItem.lootTableItem(OpenBlocksItems.TECHNICOLOR_GLASSES.get())));
            }
        });
    }

    private static boolean isDungeonChest(String path) {
        return path.contains("simple_dungeon")
                || path.contains("desert_pyramid")
                || path.contains("jungle_temple")
                || path.contains("mineshaft")
                || path.contains("stronghold_corridor")
                || path.contains("stronghold_crossing");
    }
}
