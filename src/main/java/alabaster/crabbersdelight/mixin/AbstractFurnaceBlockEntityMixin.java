package alabaster.crabbersdelight.mixin;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.item.FishSize;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 烧制时把鱼尺寸带过去（对应 1.21 版 AbstractFurnaceBlockEntityMixin）。
// 1.20.1 的入口是 craftRecipe（1.21 是 burn）；输入槽 0、输出槽 2。
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    @Unique
    private static FishSize crabbersdelight$capturedSize;

    @Inject(method = "craftRecipe", at = @At("HEAD"))
    private static void crabbersdelight$captureInputSize(DynamicRegistryManager registryManager,
                                                         Recipe<?> recipe,
                                                         DefaultedList<ItemStack> slots, int count,
                                                         CallbackInfoReturnable<Boolean> cir) {
        crabbersdelight$capturedSize = FishSizeHelper.get(slots.get(0));
    }

    @Inject(method = "craftRecipe", at = @At("RETURN"))
    private static void crabbersdelight$applyOutputSize(DynamicRegistryManager registryManager,
                                                        Recipe<?> recipe,
                                                        DefaultedList<ItemStack> slots, int count,
                                                        CallbackInfoReturnable<Boolean> cir) {
        FishSize size = crabbersdelight$capturedSize;
        crabbersdelight$capturedSize = null;

        if (size == null || !cir.getReturnValue() || !Config.FISH_SIZE_ENABLED.get()) {
            return;
        }

        ItemStack output = slots.get(2);
        if (output.isEmpty() || !output.isIn(ItemTags.FISHES)) {
            return;
        }
        FishSizeHelper.set(output, size);
    }
}
