package alabaster.crabbersdelight.mixin;

import alabaster.crabbersdelight.common.item.LifePreserverItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 让救生圈能像护腿一样被「拖进 / 点击放进」玩家界面的护腿槽。
// 1.20.1 的护腿槽只认 ArmorItem，而救生圈是 BlockItem，所以这里单独放行护腿槽（PlayerInventory 下标 37）。
@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    @Final
    public Inventory inventory;

    @Shadow
    public abstract int getIndex();

    private static final int PLAYER_LEGS_SLOT_INDEX = 37;

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void crabbersdelight$allowLifePreserverInLegsSlot(ItemStack stack,
                                                              CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof LifePreserverItem
                && this.inventory instanceof PlayerInventory
                && this.getIndex() == PLAYER_LEGS_SLOT_INDEX) {
            cir.setReturnValue(true);
        }
    }
}
