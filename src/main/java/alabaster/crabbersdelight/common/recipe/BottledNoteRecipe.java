package alabaster.crabbersdelight.common.recipe;

import alabaster.crabbersdelight.common.item.NoteData;
import alabaster.crabbersdelight.common.item.SignedNoteItem;
import alabaster.crabbersdelight.common.registry.CDModItems;
import alabaster.crabbersdelight.common.registry.CDModRecipes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

// 便笺装瓶（对应 1.21 版 BottledNoteRecipe）：1 张签名便笺 + 1 个玻璃瓶 → 瓶中便笺。
public class BottledNoteRecipe extends SpecialCraftingRecipe {
    public BottledNoteRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int bottles = 0;
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
            } else if (stack.isOf(Items.GLASS_BOTTLE)) {
                bottles++;
            } else {
                return false;
            }
        }

        return !signed.isEmpty() && bottles == 1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack signed = ItemStack.EMPTY;
        int bottles = 0;

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
            } else if (stack.isOf(Items.GLASS_BOTTLE)) {
                bottles++;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (signed.isEmpty() || bottles != 1 || !NoteData.isSigned(signed)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(CDModItems.BOTTLED_NOTE);
        NoteData.sign(result, NoteData.title(signed), NoteData.body(signed),
                NoteData.author(signed), NoteData.generation(signed));
        if (!NoteData.title(signed).isBlank()) {
            result.setCustomName(Text.literal(NoteData.title(signed)));
        }
        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        return DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDModRecipes.BOTTLED_NOTE;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }
}
