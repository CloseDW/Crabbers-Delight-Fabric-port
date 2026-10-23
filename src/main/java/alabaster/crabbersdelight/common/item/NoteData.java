package alabaster.crabbersdelight.common.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;

// 便条内容（对应 1.21 版的 NoteContent / SignedNoteContent 两个数据组件）：
// 1.20.1 没有数据组件，这里用物品 NBT 存。
//   未签名的便条：crabbersdelight:note_text  -> 纯文本
//   已签名的便条：crabbersdelight:note       -> {title, text, author, generation}
public final class NoteData {
    public static final String UNSIGNED_TEXT_KEY = "crabbersdelight:note_text";
    public static final String SIGNED_KEY = "crabbersdelight:note";

    public static final int MAX_NOTE_LENGTH = 2048;
    public static final int MAX_TITLE_LENGTH = 32;

    public static final int GENERATION_ORIGINAL = 0;
    public static final int GENERATION_COPY = 1;
    public static final int GENERATION_COPY_OF_COPY = 2;
    public static final int GENERATION_TATTERED = 3;

    private NoteData() {
    }

    public static String getText(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt == null ? "" : nbt.getString(UNSIGNED_TEXT_KEY);
    }

    public static void setText(ItemStack stack, String text) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (text == null || text.isEmpty()) {
            nbt.remove(UNSIGNED_TEXT_KEY);
        } else {
            nbt.putString(UNSIGNED_TEXT_KEY, truncate(text, MAX_NOTE_LENGTH));
        }
    }

    public static boolean isSigned(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(SIGNED_KEY);
    }

    public static NbtCompound getSigned(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt == null ? new NbtCompound() : nbt.getCompound(SIGNED_KEY);
    }

    public static void sign(ItemStack stack, String title, String text, String author, int generation) {
        NbtCompound content = new NbtCompound();
        content.putString("title", truncate(title, MAX_TITLE_LENGTH));
        content.putString("text", truncate(text, MAX_NOTE_LENGTH));
        content.putString("author", author == null ? "" : author);
        content.putInt("generation", generation);
        stack.getOrCreateNbt().put(SIGNED_KEY, content);
    }

    public static String title(ItemStack stack) {
        return getSigned(stack).getString("title");
    }

    public static String body(ItemStack stack) {
        return getSigned(stack).getString("text");
    }

    public static String author(ItemStack stack) {
        return getSigned(stack).getString("author");
    }

    public static int generation(ItemStack stack) {
        return getSigned(stack).getInt("generation");
    }

    public static String generationName(int generation) {
        return switch (generation) {
            case GENERATION_COPY -> "gui.crabbersdelight.note.generation.copy";
            case GENERATION_COPY_OF_COPY -> "gui.crabbersdelight.note.generation.copy_of_copy";
            case GENERATION_TATTERED -> "gui.crabbersdelight.note.generation.tattered";
            default -> "gui.crabbersdelight.note.generation.original";
        };
    }

    public static Formatting generationColor(int generation) {
        return switch (generation) {
            case GENERATION_COPY -> Formatting.YELLOW;
            case GENERATION_COPY_OF_COPY -> Formatting.GOLD;
            case GENERATION_TATTERED -> Formatting.RED;
            default -> Formatting.GRAY;
        };
    }

    public static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() > max ? text.substring(0, max) : text;
    }
}
