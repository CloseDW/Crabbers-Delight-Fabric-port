package alabaster.crabbersdelight.common.utils;

import alabaster.crabbersdelight.common.item.FishSize;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

// 鱼尺寸的读写与抽取（对应 1.21 版 CDModDataComponents.FISH_SIZE + FishSizeEvents.rollSize）。
// 1.20.1 没有数据组件，尺寸以字符串形式存在物品 NBT 的 "crabbersdelight:fish_size" 键上。
public final class FishSizeHelper {
    public static final String NBT_KEY = "crabbersdelight:fish_size";

    private static final double LUCK_PER_SIZE_STEP = 3.0;
    private static final double WEIGHT_FALLOFF_PER_STEP = 0.3;
    private static final double MIN_WEIGHT = 0.01;

    private FishSizeHelper() {
    }

    @Nullable
    public static FishSize get(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NBT_KEY)) {
            return null;
        }
        return FishSize.byName(nbt.getString(NBT_KEY));
    }

    public static void set(ItemStack stack, FishSize size) {
        if (size == FishSize.REGULAR) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                nbt.remove(NBT_KEY);
            }
            return;
        }
        stack.getOrCreateNbt().putString(NBT_KEY, size.getSerializedName());
    }

    // 幸运值越高，尺寸越偏向大号：中心档位从 REGULAR(索引 2) 开始，每 3 点幸运右移一档
    public static FishSize roll(double luck, Random random) {
        double center = 2.0 + luck / LUCK_PER_SIZE_STEP;

        FishSize[] sizes = FishSize.values();
        double[] weights = new double[sizes.length];
        double totalWeight = 0;
        for (int i = 0; i < sizes.length; i++) {
            double distance = Math.abs(i - center);
            weights[i] = Math.max(MIN_WEIGHT, 1.0 - distance * WEIGHT_FALLOFF_PER_STEP);
            totalWeight += weights[i];
        }

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < sizes.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) {
                return sizes[i];
            }
        }
        return FishSize.REGULAR;
    }

    // 只有鱼类物品会被抽尺寸；抽到 REGULAR 时不写 NBT（保持与 1.21 版一致）
    public static void rollAndApply(ItemStack stack, double luck, Random random) {
        if (!stack.isIn(ItemTags.FISHES)) {
            return;
        }
        set(stack, roll(luck, random));
    }
}
