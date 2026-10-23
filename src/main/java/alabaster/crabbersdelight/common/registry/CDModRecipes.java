package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.recipe.BottledNoteRecipe;
import alabaster.crabbersdelight.common.recipe.NoteCloningRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// 自定义配方序列化器（对应 1.21 版 CDModRecipeSerializers）
public final class CDModRecipes {
    private CDModRecipes() {
    }

    public static final RecipeSerializer<NoteCloningRecipe> NOTE_CLONING = Registry.register(
            Registries.RECIPE_SERIALIZER, CrabbersDelightFabric.id("note_cloning"),
            new SpecialRecipeSerializer<>(NoteCloningRecipe::new));

    public static final RecipeSerializer<BottledNoteRecipe> BOTTLED_NOTE = Registry.register(
            Registries.RECIPE_SERIALIZER, CrabbersDelightFabric.id("bottled_note"),
            new SpecialRecipeSerializer<>(BottledNoteRecipe::new));
    public static void register() {
        // 触发类加载
    }
}
