package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.container.CrabTrapMenu;
import alabaster.crabbersdelight.common.block.container.TackleBoxMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public final class CDModMenus {
    // 1.20.1 的新写法：ExtendedScreenHandlerType + Registry.register
    // （旧的 ScreenHandlerRegistry.registerExtended 已废弃且标记将被移除）
    public static final ScreenHandlerType<CrabTrapMenu> CRAB_TRAP_MENU = Registry.register(
            Registries.SCREEN_HANDLER, CrabbersDelightFabric.id("crab_trap_menu"),
            new ExtendedScreenHandlerType<>(CrabTrapMenu::new));

    public static final ScreenHandlerType<TackleBoxMenu> TACKLE_BOX_MENU = Registry.register(
            Registries.SCREEN_HANDLER, CrabbersDelightFabric.id("tackle_box"),
            new ExtendedScreenHandlerType<>(TackleBoxMenu::new));

    public static void register() {
    }
}
