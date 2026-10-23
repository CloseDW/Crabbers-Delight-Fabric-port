package alabaster.crabbersdelight.client.mixin;

import alabaster.crabbersdelight.common.item.LifePreserverItem;
import alabaster.crabbersdelight.common.network.LifePreserverNetworking;
import alabaster.crabbersdelight.common.utils.CDDebug;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 让救生圈可以在任何容器界面里直接装备：
//   1) 右键 / Shift+左键 点击背包里的救生圈        → 从背包装备
//   2) 鼠标拿起救生圈后点击自己的护腿槽（≈拖到腿部）→ 从光标装备
//   3) 点击护甲槽本身不拦截：Shift 点击已装备的救生圈仍由原版移回背包（"快速脱下"）
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    private static final int PLAYER_LEGS_SLOT_INDEX = 37;
    private static final int ARMOR_SLOT_FIRST = 36;
    private static final int ARMOR_SLOT_LAST = 39;

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void crabbersdelight$equipLifePreserver(double mouseX, double mouseY, int button,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (this.focusedSlot == null || !(this.focusedSlot.inventory instanceof PlayerInventory)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        int slotIndex = this.focusedSlot.getIndex();
        ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();

        // 情形 2：光标上拿着救生圈，点到护腿槽 → 从光标装备
        if (slotIndex == PLAYER_LEGS_SLOT_INDEX && cursorStack.getItem() instanceof LifePreserverItem) {
            CDDebug.log("life_preserver.clickLegsWithCursor count={}", cursorStack.getCount());
            send(true);
            cir.setReturnValue(true);
            return;
        }

        // 护甲槽（36-39）本身不拦截：装备中的救生圈要能 Shift 点击脱下来
        if (slotIndex >= ARMOR_SLOT_FIRST && slotIndex <= ARMOR_SLOT_LAST) {
            return;
        }

        // 情形 1：背包里的救生圈，右键或 Shift+左键
        ItemStack stack = this.focusedSlot.getStack();
        if (!(stack.getItem() instanceof LifePreserverItem)) {
            return;
        }
        boolean rightClick = button == 1;
        boolean shiftLeftClick = button == 0 && Screen.hasShiftDown();
        if (!rightClick && !shiftLeftClick) {
            return;
        }

        CDDebug.log("life_preserver.click button={} shift={} slot={}", button, shiftLeftClick, slotIndex);
        send(false);
        cir.setReturnValue(true);
    }

    private static void send(boolean fromCursor) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(fromCursor);
        ClientPlayNetworking.send(LifePreserverNetworking.EQUIP_LIFE_PRESERVER, buf);
    }
}
