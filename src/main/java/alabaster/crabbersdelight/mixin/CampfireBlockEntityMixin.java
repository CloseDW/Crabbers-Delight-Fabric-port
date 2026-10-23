package alabaster.crabbersdelight.mixin;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.item.FishSize;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 营火烤鱼也保留鱼尺寸（对应 1.21 版 AbstractCookingRecipeMixin 里的营火分支）。
// 1.20.1 营火的成品由 CampfireBlockEntity 内部的 method_17504(Inventory, World, CampfireBlockEntity) 生成。
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Unique
    private static FishSize crabbersdelight$capturedSize;

    @Inject(method = "method_17504", at = @At("HEAD"))
    private static void crabbersdelight$captureInputSize(Inventory inventory, World world,
                                                         CampfireCookingRecipe recipe,
                                                         CallbackInfoReturnable<ItemStack> cir) {
        crabbersdelight$capturedSize = FishSizeHelper.get(inventory.getStack(0));
    }

    @Inject(method = "method_17504", at = @At("RETURN"))
    private static void crabbersdelight$applyOutputSize(Inventory inventory, World world,
                                                        CampfireCookingRecipe recipe,
                                                        CallbackInfoReturnable<ItemStack> cir) {
        FishSize size = crabbersdelight$capturedSize;
        crabbersdelight$capturedSize = null;

        if (size == null || !Config.FISH_SIZE_ENABLED.get()) {
            return;
        }

        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty() || !result.isIn(ItemTags.FISHES)) {
            return;
        }
        FishSizeHelper.set(result, size);
    }
}
