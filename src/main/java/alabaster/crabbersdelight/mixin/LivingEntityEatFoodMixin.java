package alabaster.crabbersdelight.mixin;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.item.FishSize;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 鱼尺寸影响食物值：1.20.1 的食物属性是物品级的、不能按堆叠改，
// 所以在吃下之前按尺寸改写这一次进食的结果（1.21 版是直接替换该堆叠的 FOOD 组件）。
//
// 1.21 版的算法（FishSizeEvents#applyScaledFood）：
//   营养 = round(基础营养 × 倍率)
//   饱和度修饰符 = 基础饱和度修饰符 × 倍率
// 实际回复的饱和度 = 营养 × 饱和度修饰符 × 2，所以两个数值都要跟着尺寸走。
//
// 注入点说明：玩家进食走 PlayerEntity#eatFood（先加基础营养/饱和度）-> super.eatFood
// （即本 mixin 注入的 LivingEntity#eatFood），因此 HEAD 时基础值已经加过，
// 这里做的是"把已经加进去的量校正成目标值"——小于普通尺寸的鱼同样会被扣回去。
@Mixin(LivingEntity.class)
public abstract class LivingEntityEatFoodMixin {
    @Inject(method = "eatFood", at = @At("HEAD"))
    private void crabbersdelight$applyFishSizeFoodValue(World world, ItemStack stack,
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

        float multiplier = size.getMultiplier();
        int baseHunger = food.getHunger();
        float baseSaturationModifier = food.getSaturationModifier();
        int targetHunger = Math.round(baseHunger * multiplier);
        float targetSaturationModifier = baseSaturationModifier * multiplier;

        HungerManager hunger = player.getHungerManager();

        int foodLevel = MathHelper.clamp(hunger.getFoodLevel() + (targetHunger - baseHunger), 0, 20);
        hunger.setFoodLevel(foodLevel);

        float saturationDelta = targetHunger * targetSaturationModifier * 2.0F
                - baseHunger * baseSaturationModifier * 2.0F;
        float saturation = MathHelper.clamp(
                hunger.getSaturationLevel() + saturationDelta, 0.0F, (float) foodLevel);
        hunger.setSaturationLevel(saturation);
    }
}
