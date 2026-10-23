package alabaster.crabbersdelight.integration.jei;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.registry.CDModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public class CrabTrapCategory implements IRecipeCategory<CrabTrapRecipeWrapper> {
    private static final Identifier CRAB_TRAP_LOCATION =
            CrabbersDelightFabric.id("textures/gui/jei_crab_trap.png");
    private static final int WIDTH = 79;
    private static final int HEIGHT = 39;

    private final IDrawable background;
    private final IDrawable icon;
    private final Text title;

    public CrabTrapCategory(IGuiHelper helper) {
        this.title = Text.translatable("block.crabbersdelight.crab_trap");
        this.background = helper.createDrawable(CRAB_TRAP_LOCATION, 0, 0, WIDTH, HEIGHT);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CDModItems.CRAB_TRAP));
    }

    @Override
    public RecipeType<CrabTrapRecipeWrapper> getRecipeType() {
        return JEIPlugin.CRAB_TRAP_RECIPE;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    // JEI 15.20：背景不再通过 getBackground() 提供，改为声明尺寸 + 在 createRecipeExtras 里画
    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, CrabTrapRecipeWrapper recipe, IFocusGroup focuses) {
        builder.addDrawable(this.background, 0, 0);
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    private static boolean iconPosition(double mouseX, double mouseY) {
        int iconPosX = 32;
        int iconPosY = 2;
        int iconHeight = 10;
        int iconWidth = 15;
        return iconPosX <= mouseX && mouseX < iconPosX + iconWidth
                && iconPosY <= mouseY && mouseY < iconPosY + iconHeight;
    }

    // JEI 15.20：getTooltipStrings 已废弃，改为往 ITooltipBuilder 里塞
    @Override
    public void getTooltip(ITooltipBuilder tooltip, CrabTrapRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView,
                           double mouseX, double mouseY) {
        if (!iconPosition(mouseX, mouseY)) {
            return;
        }
        int min = Config.MIN_TICKS.get();
        int max = Config.MAX_TICKS.get();
        if (max > min) {
            tooltip.add(Text.literal("Collects every " + min + "-" + max + " ticks"));
        } else {
            tooltip.add(Text.literal("Error: Minimum value is higher than maximum value!").formatted(Formatting.RED));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrabTrapRecipeWrapper recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 16).addItemStack(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 57, 16)
                .addItemStacks(Arrays.stream(recipe.getOutput().getMatchingStacks()).toList());
    }
}
