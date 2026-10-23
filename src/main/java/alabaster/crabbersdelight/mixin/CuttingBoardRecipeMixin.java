package alabaster.crabbersdelight.mixin;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.item.FishSize;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;

import java.util.List;

// 砧板：切大鱼出的成品更多（对应 1.21 版 CuttingBoardRecipeMixin）。
// 例：巨型鱼 ×2.0 份、小型鱼 ×0.5（最少 1 份）。
@Mixin(CuttingBoardRecipe.class)
public abstract class CuttingBoardRecipeMixin {

    @Inject(method = "rollResults", at = @At("RETURN"))
    private void crabbersdelight$scaleByFishSize(Random random, int fortuneLevel, RecipeWrapper input,
                                                 CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!Config.FISH_SIZE_ENABLED.get() || input == null) {
            return;
        }

        FishSize size = FishSizeHelper.get(input.getStack(0));
        if (size == null || size == FishSize.REGULAR) {
            return;
        }

        for (ItemStack stack : cir.getReturnValue()) {
            if (stack.isEmpty()) {
                continue;
            }
            int scaled = Math.max(1, Math.round(stack.getCount() * size.getMultiplier()));
            stack.setCount(Math.min(scaled, stack.getMaxCount()));
        }
    }
}
