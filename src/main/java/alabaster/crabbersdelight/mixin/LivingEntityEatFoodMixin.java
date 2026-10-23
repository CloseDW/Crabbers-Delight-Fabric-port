package alabaster.crabbersdelight.mixin;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.item.FishSize;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 鱼尺寸影响饱食度：1.20.1 的食物属性是物品级的、不能按堆叠改，
// 所以在吃下之前按尺寸倍数补一份额外的饥饿值（1.21 版是直接替换该堆叠的食物属性）。
@Mixin(LivingEntity.class)
public abstract class LivingEntityEatFoodMixin {
    @Inject(method = "eatFood", at = @At("HEAD"))
    private void crabbersdelight$addFishSizeBonus(World world, ItemStack stack,
                                                  CallbackInfoReturnable<ItemStack> cir) {
        if (!((Object) this instanceof PlayerEntity player)) {
            return;
        }
        if (!Config.FISH_SIZE_ENABLED.get()) {
            return;
        }

        FishSize size = FishSizeHelper.get(stack);
        if (size == null || size == FishSize.REGULAR) {
            return;
        }

        FoodComponent food = stack.getItem().getFoodComponent();
        if (food == null) {
            return;
        }

        int extra = Math.round(food.getHunger() * (size.getMultiplier() - 1.0F));
        if (extra > 0) {
            player.getHungerManager().add(extra, 0.0F);
        }
    }
}
