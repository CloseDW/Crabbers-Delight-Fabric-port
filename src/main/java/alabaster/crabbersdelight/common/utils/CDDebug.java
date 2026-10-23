package alabaster.crabbersdelight.common.utils;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.Config;

// 调试日志开关：配置里 [debug] debugLogging = true 时才会输出，前缀 [CD-DEBUG] 方便过滤。
public final class CDDebug {
    private CDDebug() {
    }

    public static boolean enabled() {
        try {
            return Config.DEBUG_LOGGING.get();
        } catch (Exception e) {
            return false;
        }
    }

    public static void log(String format, Object... args) {
        if (enabled()) {
            CrabbersDelightFabric.LOGGER.info("[CD-DEBUG] " + format, args);
        }
    }
}
