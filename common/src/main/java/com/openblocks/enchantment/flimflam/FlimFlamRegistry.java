package com.openblocks.enchantment.flimflam;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Registry and weighted random selector for flim flam prank effects.
 */
public final class FlimFlamRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlimFlamRegistry.class);
    private static final Random RANDOM = new Random();
    private static final List<FlimFlamEntry> EFFECTS = new ArrayList<>();

    public record FlimFlamEntry(
            String name,
            int cost,
            int weight,
            boolean safe,
            boolean silent,
            IFlimFlamEffect effect
    ) {
        public boolean canApply(int luck) {
            return luck <= cost;
        }
    }

    public static void register(String name, int cost, int weight, boolean safe, boolean silent, IFlimFlamEffect effect) {
        EFFECTS.add(new FlimFlamEntry(name, cost, weight, safe, silent, effect));
    }

    public static void register(String name, int cost, int weight, boolean safe, IFlimFlamEffect effect) {
        register(name, cost, weight, safe, false, effect);
    }

    /**
     * Register all built-in flim flam effects.
     */
    public static void registerAll() {
        register("bane",              -125, 100, true,  new BaneFlimFlam());
        register("creepers",          -60,  50,  false, new DummyCreepersFlimFlam());
        register("effect",            -75,  75,  false, new EffectFlimFlam());
        register("encase",            -50,  50,  false, new EncaseFlimFlam());
        register("inventory-shuffle", -50,  100, true,  new InventoryShuffleFlimFlam());
        register("invisible-mobs",    -25,  10,  true,  new InvisibleMobsFlimFlam());
        register("disarm",            -50,  50,  false, new ItemDropFlimFlam());
        register("epic-lore",         -10,  100, true,  new LoreFlimFlam());
        register("mount",             -150, 25,  false, new MountFlimFlam());
        register("living-rename",     -10,  100, false, new RenameFlimFlam());
        register("sheep-dye",         -5,   50,  true,  new SheepDyeFlimFlam());
        register("skyblock",          -100, 150, false, new SkyblockFlimFlam());
        register("snowballs",         -50,  50,  false, new SnowballsFlimFlam());
        register("sound",             -5,   150, true,  true, new SoundFlimFlam());
        register("squid",             -75,  50,  true,  new SquidFlimFlam());
        register("teleport",          -100, 30,  false, new TeleportFlimFlam());
        register("useless-tool",      -125, 50,  true,  new UselessToolFlimFlam());
    }

    /**
     * Execute a random flim flam effect on the target player, weighted by luck.
     */
    public static void executeRandomEffect(ServerPlayer target, int luck) {
        List<FlimFlamEntry> candidates = new ArrayList<>();
        int totalWeight = 0;

        for (FlimFlamEntry entry : EFFECTS) {
            if (entry.canApply(luck)) {
                candidates.add(entry);
                totalWeight += entry.weight();
            }
        }

        if (candidates.isEmpty()) return;

        Collections.shuffle(candidates);

        while (!candidates.isEmpty()) {
            int selectedWeight = RANDOM.nextInt(totalWeight);
            int currentWeight = 0;
            Iterator<FlimFlamEntry> it = candidates.iterator();

            while (it.hasNext()) {
                FlimFlamEntry entry = it.next();
                currentWeight += entry.weight();
                if (selectedWeight <= currentWeight) {
                    try {
                        if (entry.effect().execute(target)) {
                            LOGGER.debug("Player {} flim-flammed with {}, luck: {}", target.getName().getString(), entry.name(), luck);
                            if (!entry.silent()) {
                                target.sendSystemMessage(
                                        net.minecraft.network.chat.Component.translatable("openblocks.flim_flammed"));
                            }
                            return;
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Error during flimflam '{}' execution", entry.name(), t);
                    }
                    totalWeight -= entry.weight();
                    it.remove();
                    break;
                }
            }
        }
    }

    private FlimFlamRegistry() {}
}
