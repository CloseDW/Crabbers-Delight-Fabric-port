package alabaster.crabbersdelight.common.network;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.item.LifePreserverItem;
import alabaster.crabbersdelight.common.utils.CDDebug;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

// 救生圈"在背包里直接装备"的网络包（对应 1.21 版靠 equippable 数据组件实现的行为）。
// 1.20.1 没有该组件，所以由客户端拦截点击后发包，服务端代为装备：
//   fromCursor = false：从背包里找一件装备（右键 / Shift 点击）
//   fromCursor = true ：把鼠标光标上拿着的那件装备到腿上（"拖到护腿槽"的操作）
public final class LifePreserverNetworking {
    public static final Identifier EQUIP_LIFE_PRESERVER = CrabbersDelightFabric.id("equip_life_preserver");

    private LifePreserverNetworking() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(EQUIP_LIFE_PRESERVER, (server, player, handler, buf, responseSender) -> {
            boolean fromCursor = buf.readableBytes() > 0 && buf.readBoolean();
            server.execute(() -> equip(player, fromCursor));
        });
    }

    private static void equip(ServerPlayerEntity player, boolean fromCursor) {
        if (fromCursor) {
            equipFromCursor(player);
            return;
        }

        if (player.getEquippedStack(EquipmentSlot.LEGS).getItem() instanceof LifePreserverItem) {
            return;
        }

        ItemStack found = ItemStack.EMPTY;
        int foundSlot = -1;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof LifePreserverItem) {
                found = stack;
                foundSlot = i;
                break;
            }
        }
        CDDebug.log("life_preserver.equip player={} foundSlot={}", player.getName().getString(), foundSlot);
        if (foundSlot < 0) {
            return;
        }

        ItemStack previous = player.getEquippedStack(EquipmentSlot.LEGS).copy();
        ItemStack equipped = found.copyWithCount(1);
        found.decrement(1);
        player.equipStack(EquipmentSlot.LEGS, equipped);
        if (!previous.isEmpty() && !player.getInventory().insertStack(previous)) {
            player.dropItem(previous, false);
        }
        player.currentScreenHandler.syncState();
    }

    private static void equipFromCursor(ServerPlayerEntity player) {
        ScreenHandler handler = player.currentScreenHandler;
        ItemStack cursor = handler.getCursorStack();
        if (!(cursor.getItem() instanceof LifePreserverItem)) {
            return;
        }
        CDDebug.log("life_preserver.equipFromCursor player={} count={}",
                player.getName().getString(), cursor.getCount());

        ItemStack previous = player.getEquippedStack(EquipmentSlot.LEGS).copy();
        ItemStack equipped = cursor.copyWithCount(1);
        cursor.decrement(1);
        player.equipStack(EquipmentSlot.LEGS, equipped);

        if (!previous.isEmpty()) {
            if (cursor.isEmpty()) {
                handler.setCursorStack(previous);
            } else if (!player.getInventory().insertStack(previous)) {
                player.dropItem(previous, false);
            }
        }
        handler.syncState();
    }
}
