package alabaster.crabbersdelight.common.utils;

import net.minecraft.entity.projectile.FishingBobberEntity;

import java.lang.reflect.Field;

// 钓钩私有字段反射（对应 1.21 版 FishingHookReflection）。
// 1.20.1 的 Yarn 字段名与 1.21 Mojmap 不同：
//   biting            -> caughtFish（布尔）
//   timeUntilLured    -> waitCountdown（在此之前还要等多久才来鱼）
//   nibble / 咬钩窗口 -> hookCountdown（已上钩后还能补竿的剩余 tick）
public class FishingHookReflection {
    private static Field caughtFishField;
    private static Field hookCountdownField;
    private static Field waitCountdownField;

    static {
        caughtFishField = findField("caughtFish", "biting");
        hookCountdownField = findField("hookCountdown", "nibble");
        waitCountdownField = findField("waitCountdown", "timeUntilLured");
    }

    private static Field findField(String... names) {
        for (String name : names) {
            try {
                Field field = FishingBobberEntity.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    public static boolean isBiting(FishingBobberEntity hook) {
        if (caughtFishField == null) {
            return false;
        }
        try {
            return caughtFishField.getBoolean(hook);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static void capLureTime(FishingBobberEntity hook, int maxTicks) {
        if (waitCountdownField == null) {
            return;
        }
        try {
            int current = waitCountdownField.getInt(hook);
            if (current > maxTicks) {
                waitCountdownField.setInt(hook, maxTicks);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    // 延长“已上钩”窗口，给自动/倒刺饵更多补竿时间
    public static void extendNibbleTime(FishingBobberEntity hook, int minTicks) {
        if (hookCountdownField == null) {
            return;
        }
        try {
            int current = hookCountdownField.getInt(hook);
            if (current > 0 && current < minTicks) {
                hookCountdownField.setInt(hook, minTicks);
            }
        } catch (IllegalAccessException ignored) {
        }
    }
}
