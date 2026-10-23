package alabaster.crabbersdelight.common;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class CDFoodValues {
    // 时长常量（刻）
    public static final int BRIEF_DURATION = 600;    // 30 秒
    public static final int SHORT_DURATION = 1200;   // 1 分钟
    public static final int MEDIUM_DURATION = 3600;  // 3 分钟
    public static final int LONG_DURATION = 6000;    // 5 分钟

    // 生/熟海鲜
    public static final FoodComponent RAW_CRAB = new FoodComponent.Builder()
            .hunger(2).saturationModifier(0.3f).meat().build();

    public static final FoodComponent COOKED_CRAB = new FoodComponent.Builder()
            .hunger(4).saturationModifier(0.8f).meat().build();

    public static final FoodComponent RAW_CLAWSTER = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.5f).meat().build();

    public static final FoodComponent COOKED_CLAWSTER = new FoodComponent.Builder()
            .hunger(7).saturationModifier(0.8f).meat().build();

    public static final FoodComponent RAW_SHRIMP = new FoodComponent.Builder()
            .hunger(1).saturationModifier(0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.3F)
            .meat().snack().build();

    public static final FoodComponent COOKED_SHRIMP = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.6f).meat().snack().build();

    public static final FoodComponent RAW_CLAM_MEAT = new FoodComponent.Builder()
            .hunger(2).saturationModifier(0.3f).meat().snack().build();

    public static final FoodComponent COOKED_CLAM_MEAT = new FoodComponent.Builder()
            .hunger(4).saturationModifier(0.8f).meat().snack().build();

    public static final FoodComponent RAW_SQUID_TENTACLES = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.5f).meat().build();

    public static final FoodComponent COOKED_SQUID_TENTACLES = new FoodComponent.Builder()
            .hunger(7).saturationModifier(0.8f).meat().build();

    public static final FoodComponent RAW_FROG_LEG = new FoodComponent.Builder()
            .hunger(2).saturationModifier(0.3f).meat().build();

    public static final FoodComponent COOKED_FROG_LEG = new FoodComponent.Builder()
            .hunger(4).saturationModifier(0.7f).meat().build();

    public static final FoodComponent FRIED_FISH = new FoodComponent.Builder()
            .hunger(5).saturationModifier(0.6f).meat().build();

    public static final FoodComponent FISH_SANDWICH = new FoodComponent.Builder()
            .hunger(10).saturationModifier(0.8f).build();

    // 饮品
    public static final FoodComponent KELP_SHAKE = new FoodComponent.Builder()
            .alwaysEdible()
            .statusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 300, 0), 1.0F).build();

    public static final FoodComponent COCONUT_MILK = new FoodComponent.Builder()
            .alwaysEdible().hunger(1).saturationModifier(0.5f).build();

    public static final FoodComponent COCONUT_PUDDING = new FoodComponent.Builder()
            .hunger(6).saturationModifier(0.5f).build();

    // 鱼片
    public static final FoodComponent COOKED_TROPICAL_FISH = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.8f).meat().snack().build();

    public static final FoodComponent COOKED_TROPICAL_FISH_SLICE = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.5f).meat().snack().build();

    public static final FoodComponent TROPICAL_FISH_SLICE = new FoodComponent.Builder()
            .hunger(1).saturationModifier(0.1f).meat().snack().build();

    public static final FoodComponent COOKED_PUFFERFISH_SLICE = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.5f).meat().snack().build();

    public static final FoodComponent PUFFERFISH_SLICE = new FoodComponent.Builder()
            .hunger(1).saturationModifier(0.1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.POISON, 300, 0), 0.3F)
            .meat().snack().build();

    public static final FoodComponent COCONUT_HALVE = new FoodComponent.Builder()
            .hunger(2).saturationModifier(0.4F).build();

    // 餐品（部分带 FD 的“滋养”效果）
    public static final FoodComponent CRAB_LEGS = new FoodComponent.Builder()
            .hunger(3).saturationModifier(0.6f).meat().build();

    public static final FoodComponent SHRIMP_SKEWER = new FoodComponent.Builder()
            .hunger(8).saturationModifier(0.8f).build();

    public static final FoodComponent SHRIMP_FRIED_RICE = new FoodComponent.Builder()
            .hunger(7).saturationModifier(0.7f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), BRIEF_DURATION, 0), 1.0F).build();

    public static final FoodComponent SURF_AND_TURF = new FoodComponent.Builder()
            .hunger(14).saturationModifier(0.8f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), LONG_DURATION, 0), 1.0F).build();

    public static final FoodComponent CLAM_BAKE = new FoodComponent.Builder()
            .hunger(13).saturationModifier(0.9f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), LONG_DURATION, 0), 1.0F).build();

    public static final FoodComponent CLAM_CHOWDER = new FoodComponent.Builder()
            .hunger(7).saturationModifier(0.6f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), MEDIUM_DURATION, 0), 1.0F).build();

    public static final FoodComponent BISQUE = new FoodComponent.Builder()
            .hunger(8).saturationModifier(0.75f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), MEDIUM_DURATION, 0), 1.0F).build();

    public static final FoodComponent SEAFOOD_GUMBO = new FoodComponent.Builder()
            .hunger(12).saturationModifier(0.9f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), MEDIUM_DURATION, 0), 1.0F).build();

    public static final FoodComponent FISH_STICK = new FoodComponent.Builder()
            .hunger(5).saturationModifier(0.6f).build();

    public static final FoodComponent CRAB_CAKES = new FoodComponent.Builder()
            .hunger(10).saturationModifier(0.6f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), SHORT_DURATION, 0), 1.0F).build();

    public static final FoodComponent STUFFED_NAUTILUS_SHELL = new FoodComponent.Builder()
            .hunger(8).saturationModifier(0.7f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), MEDIUM_DURATION, 0), 1.0F).build();

    public static final FoodComponent JAR_OF_PICKLES = new FoodComponent.Builder()
            .hunger(4).saturationModifier(0.4f).build();

    public static final FoodComponent SEA_PICKLE_JUICE = new FoodComponent.Builder()
            .hunger(2).saturationModifier(0.2f)
            .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, BRIEF_DURATION, 0), 1.0F)
            .statusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, MEDIUM_DURATION, 0), 1.0F).build();

    public static final FoodComponent SQUID_KEBAB = new FoodComponent.Builder()
            .hunger(8).saturationModifier(0.8f).build();

    public static final FoodComponent FROG_LEG_KEBAB = new FoodComponent.Builder()
            .hunger(9).saturationModifier(0.8f).build();

    public static final FoodComponent CORAL_CRUNCH = new FoodComponent.Builder()
            .hunger(4).saturationModifier(0.2f).snack()
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), SHORT_DURATION, 0), 1.0F).build();

    public static final FoodComponent CLAWSTER_FEAST = new FoodComponent.Builder()
            .hunger(10).saturationModifier(0.9f)
            .statusEffect(new StatusEffectInstance(ModEffects.NOURISHMENT.get(), LONG_DURATION, 0), 1.0F).build();

    public static final FoodComponent TURTLE_STEW = new FoodComponent.Builder()
            .hunger(7).saturationModifier(0.6f).snack()
            .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, SHORT_DURATION, 0), 1.0F).build();
}
