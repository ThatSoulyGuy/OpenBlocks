package com.openblocks.core.registry;

import com.openblocks.OpenBlocksConstants;
import com.openblocks.core.util.ColorMeta;
import com.openblocks.imaginary.ImaginaryBlockItem;
import com.openblocks.imaginary.StencilItem;
import com.openblocks.imaginary.StencilPattern;
import com.openblocks.trophy.TrophyBlockItem;
import com.openblocks.trophy.TrophyType;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class OpenBlocksCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(OpenBlocksConstants.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> OPENBLOCKS_TAB = TABS.register(
            "openblocks",
            () -> CreativeTabRegistry.create(
                    builder -> builder
                            .title(Component.translatable("itemGroup." + OpenBlocksConstants.MOD_ID))
                            .icon(() -> new ItemStack(OpenBlocksItems.ELEVATOR.get()))
                            .displayItems((params, output) -> populateItems(output))
            )
    );

    public static void register() {
        TABS.register();
    }

    private static void safeAccept(CreativeModeTab.Output output, ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            output.accept(stack);
        }
    }

    private static void populateItems(CreativeModeTab.Output output) {
        // Blocks
        safeAccept(output, new ItemStack(OpenBlocksItems.ELEVATOR.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.ELEVATOR_ROTATING.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.LADDER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.ROPE_LADDER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PATH.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SPONGE.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SCAFFOLDING.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.FLAG.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.BIG_BUTTON.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.BIG_BUTTON_WOOD.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SKY.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SKY_INVERTED.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.HEAL.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.FAN.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.BEAR_TRAP.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.TARGET.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SPRINKLER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.CANNON.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.VILLAGE_HIGHLIGHTER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.DONATION_STATION.get()));

        // Items
        safeAccept(output, new ItemStack(OpenBlocksItems.SPONGE_ON_A_STICK.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.TASTY_CLAY.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.WRENCH.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.EPIC_ERASER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SLIMALYZER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PEDOMETER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SLEEPING_BAG.get()));

        // Phase 3
        safeAccept(output, new ItemStack(OpenBlocksItems.TANK.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.XP_DRAIN.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.XP_BOTTLER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.XP_SHOWER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.GRAVE.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.BLOCK_BREAKER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.BLOCK_PLACER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.VACUUM_HOPPER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.ITEM_DROPPER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.AUTO_ANVIL.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.AUTO_ENCHANTMENT_TABLE.get()));

        // Phase 4: Entity Items
        safeAccept(output, new ItemStack(OpenBlocksItems.GOLDEN_EYE.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.LUGGAGE.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.HANG_GLIDER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.CARTOGRAPHER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.MINI_ME.get()));

        // Phase 4: Canvas/Paint
        safeAccept(output, new ItemStack(OpenBlocksItems.CANVAS.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.CANVAS_GLASS.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PAINT_CAN_ITEM.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PAINT_MIXER.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PAINT_BRUSH.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SQUEEGEE.get()));

        // Phase 7: Interaction Items
        safeAccept(output, new ItemStack(OpenBlocksItems.CURSOR.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.DEV_NULL.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PENCIL_GLASSES.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.CRAYON_GLASSES.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.TECHNICOLOR_GLASSES.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.SONIC_GLASSES.get()));

        // Phase 8: Imaginary + Guide + Drawing Table
        safeAccept(output, new ItemStack(OpenBlocksItems.GUIDE.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.DRAWING_TABLE.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.UNPREPARED_STENCIL.get()));

        // Pencil (imaginary item, no color)
        safeAccept(output, ImaginaryBlockItem.createPencil());
        safeAccept(output, ImaginaryBlockItem.createPencilInverted());

        // Crayons (imaginary item, one per color, normal + inverted)
        for (ColorMeta color : ColorMeta.values()) {
            safeAccept(output, ImaginaryBlockItem.createCrayon(color.getRgb()));
            safeAccept(output, ImaginaryBlockItem.createCrayonInverted(color.getRgb()));
        }

        // Stencil variants
        for (StencilPattern pattern : StencilPattern.values()) {
            safeAccept(output, StencilItem.createStencil(pattern));
        }

        // Phase 9: Golden Egg + Projector
        safeAccept(output, new ItemStack(OpenBlocksItems.GOLDEN_EGG.get()));
        safeAccept(output, new ItemStack(OpenBlocksItems.PROJECTOR.get()));

        // Phase 9: Trophies (one per type)
        for (TrophyType type : TrophyType.values()) {
            safeAccept(output, TrophyBlockItem.createTrophyItem(type));
        }
    }

    private OpenBlocksCreativeTabs() {}
}
