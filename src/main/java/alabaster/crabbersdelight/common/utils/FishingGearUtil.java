package alabaster.crabbersdelight.common.utils;

import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public final class FishingGearUtil {
    private FishingGearUtil() {
    }

    // 1.21 用 ItemAbilities.FISHING_ROD_CAST；1.20.1 直接判 FishingRodItem
    @Nullable
    public static Hand findRodHand(net.minecraft.entity.player.PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof FishingRodItem) {
            return Hand.MAIN_HAND;
        }
        if (player.getOffHandStack().getItem() instanceof FishingRodItem) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    public static ItemStack getRodStack(net.minecraft.entity.player.PlayerEntity player, Hand hand) {
        return player.getStackInHand(hand);
    }
}
