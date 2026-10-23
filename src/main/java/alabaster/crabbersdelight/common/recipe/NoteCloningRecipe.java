package alabaster.crabbersdelight.common.recipe;

import alabaster.crabbersdelight.common.item.NoteData;
import alabaster.crabbersdelight.common.item.NoteItem;
import alabaster.crabbersdelight.common.item.SignedNoteItem;
import alabaster.crabbersdelight.common.registry.CDModItems;
import alabaster.crabbersdelight.common.registry.CDModRecipes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

// 便条复制（对应 1.21 版 NoteCloningRecipe）：
// 1 张签名便笺 + N 张空白便条 → N 张副本，代际递增（原版→副本→副本的副本→破损，破损不能再复制）。
public class NoteCloningRecipe extends SpecialCraftingRecipe {
    public NoteCloningRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int blanks = 0;
        ItemStack signed = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof SignedNoteItem) {
                if (!signed.isEmpty()) {
                    return false;
                }
                signed = stack;
            } else if (stack.getItem() instanceof NoteItem) {
                blanks++;
            } else {
                return false;
            }
        }

        return !signed.isEmpty() && blanks > 0 && nextGeneration(signed) >= 0;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        int blanks = 0;
        ItemStack signed = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof SignedNoteItem) {
                if (!signed.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                signed = stack;
            } else if (stack.getItem() instanceof NoteItem) {
                blanks++;
            } else {
                return ItemStack.EMPTY;
            }
        }

        int generation = nextGeneration(signed);
        if (signed.isEmpty() || blanks < 1 || generation < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(CDModItems.SIGNED_NOTE, blanks);
        NoteData.sign(result, NoteData.title(signed), NoteData.body(signed), NoteData.author(signed), generation);
        if (!NoteData.title(signed).isBlank()) {
            result.setCustomName(Text.literal(NoteData.title(signed)));
        }
        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        DefaultedList<ItemStack> remaining = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof SignedNoteItem) {
                remaining.set(i, stack.copyWithCount(1));
                break;
            }
        }
        return remaining;
    }

    private static int nextGeneration(ItemStack signed) {
        if (!NoteData.isSigned(signed)) {
            return -1;
        }
        return switch (NoteData.generation(signed)) {
            case NoteData.GENERATION_ORIGINAL -> NoteData.GENERATION_COPY;
            case NoteData.GENERATION_COPY -> NoteData.GENERATION_COPY_OF_COPY;
            case NoteData.GENERATION_COPY_OF_COPY -> NoteData.GENERATION_TATTERED;
            default -> -1;
        };
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDModRecipes.NOTE_CLONING;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }
}
