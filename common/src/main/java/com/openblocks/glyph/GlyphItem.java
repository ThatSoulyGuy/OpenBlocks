package com.openblocks.glyph;

import com.openblocks.core.registry.OpenBlocksEntities;
import com.openblocks.core.registry.OpenBlocksItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GlyphItem extends Item {

    // Characters in the basic texture sheet (copied from FontRenderer / old ItemGlyph)
    public static final char[] ALMOST_ASCII = ("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5" +
            "\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207" +
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea" +
            "\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff" +
            "\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf" +
            "\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555" +
            "\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a" +
            "\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a" +
            "\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6" +
            "\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0" +
            "\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000").toCharArray();

    // Characters shown in creative tab (digits only)
    public static final char[] DISPLAY_CHARS = "0123456789".toCharArray();

    private static final char DEFAULT_CHAR = '?';

    public GlyphItem(Properties properties) {
        super(properties);
    }

    public static int findCharIndex(char ch) {
        for (int i = 0; i < ALMOST_ASCII.length; i++) {
            if (ALMOST_ASCII[i] == ch) return i;
        }
        return findCharIndex(DEFAULT_CHAR);
    }

    public static char getChar(int index) {
        if (index >= 0 && index < ALMOST_ASCII.length) {
            return ALMOST_ASCII[index];
        }
        return DEFAULT_CHAR;
    }

    public static int getCharIndex(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains("Char") ? tag.getInt("Char") : 0;
    }

    public static ItemStack createGlyph(int charIndex) {
        ItemStack stack = new ItemStack(OpenBlocksItems.GLYPH.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("Char", charIndex);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Set custom model data for per-character texture overrides
        // Digit 0-9 → model data 1-10
        char ch = getChar(charIndex);
        if (ch >= '0' && ch <= '9') {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(ch - '0' + 1));
        }
        return stack;
    }

    public static ItemStack createGlyphForDisplay(char ch) {
        return createGlyph(findCharIndex(ch));
    }

    @Override
    public Component getName(ItemStack stack) {
        int index = getCharIndex(stack);
        char ch = getChar(index);
        if (ch != 0 && !Character.isWhitespace(ch)) {
            return Component.translatable("item.openblocks.glyph.with_char", String.valueOf(ch));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int index = getCharIndex(stack);
        char ch = getChar(index);
        String name = Character.getName(ch);
        if (name != null) {
            tooltipComponents.add(Component.literal(String.format("U+%04X (%s)", (int) ch, name)));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction face = context.getClickedFace();

        // Only horizontal faces (walls)
        if (face == Direction.UP || face == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockPos hangPos = pos.relative(face);
        ItemStack stack = context.getItemInHand();

        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(hangPos, face, stack)) {
            return InteractionResult.PASS;
        }

        // Calculate UV offset from click location
        Vec3 click = context.getClickLocation();
        double relX = click.x - pos.getX();
        double relY = click.y - pos.getY();
        double relZ = click.z - pos.getZ();

        double u, v;
        v = relY;
        switch (face) {
            case NORTH, SOUTH -> u = relX;
            case WEST, EAST -> u = relZ;
            default -> { return InteractionResult.PASS; }
        }

        byte offsetX = (byte) Mth.clamp((int) (u * 16), 0, 15);
        byte offsetY = (byte) Mth.clamp((int) (v * 16), 0, 15);

        if (!level.isClientSide()) {
            GlyphEntity entity = new GlyphEntity(OpenBlocksEntities.GLYPH.get(), level);
            entity.setup(hangPos, face, getCharIndex(stack), offsetX, offsetY);

            if (entity.checkSurface()) {
                level.addFreshEntity(entity);
                level.playSound(null, hangPos, SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);

                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
