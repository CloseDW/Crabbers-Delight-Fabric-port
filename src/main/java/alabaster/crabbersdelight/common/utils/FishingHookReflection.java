package alabaster.crabbersdelight.common.utils;

import net.minecraft.entity.projectile.FishingBobberEntity;

// 钓钩私有字段访问（对应 1.21 版 FishingHookReflection）。
// 1.20.1 的 Yarn 字段名 -> 1.21 Mojmap 名：
//   caughtFish    -> biting          （是否咬钩）
//   waitCountdown -> timeUntilLured  （还有多久来鱼）
//   hookCountdown -> nibble          （咬钩后还能补竿的 tick）
//
// 这里不再用反射：反射靠字符串字段名，而字符串不会被 Loom 重映射，
// 发行版里类被改成 intermediary（net.minecraft.class_1536）、字段变成 field_23232，
// 按 "caughtFish" 去 getDeclaredField 必然 NoSuchFieldException，三个功能就静默失效。
// 改成 access widener（见 crabbersdelight.accesswidener）后字段在编译期就是 public，
// 由 Loom 一起重映射，开发环境和发行版行为一致。
public final class FishingHookReflection {
    private FishingHookReflection() {
    }

    public static boolean isBiting(FishingBobberEntity hook) {
        return hook.caughtFish;
    }

    public static void capLureTime(FishingBobberEntity hook, int maxTicks) {
        if (hook.waitCountdown > maxTicks) {
            hook.waitCountdown = maxTicks;
        }
    }

    // 延长“已上钩”窗口，给自动/倒刺饵更多补竿时间
    public static void extendNibbleTime(FishingBobberEntity hook, int minTicks) {
        if (hook.hookCountdown > 0 && hook.hookCountdown < minTicks) {
            hook.hookCountdown = minTicks;
        }
    }
}
