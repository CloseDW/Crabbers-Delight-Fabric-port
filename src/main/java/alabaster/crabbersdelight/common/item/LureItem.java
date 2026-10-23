package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.item.fishing.LureEffect;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 假饵（对应 1.21 版 LureItem）：放进钓具箱的假饵槽后生效
public class LureItem extends Item {
    private final LureEffect effect;

    public LureItem(Item.Settings settings, LureEffect effect) {
        super(settings);
        this.effect = effect;
    }

    public LureEffect getEffect() {
        return effect;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(descriptionKey()).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.crabbersdelight.gear.install").formatted(Formatting.DARK_GRAY));
    }

    private String descriptionKey() {
        switch (effect) {
            case BARBED:
                return "tooltip.crabbersdelight.barbed_lure.desc";
            case DOUBLE:
                return "tooltip.crabbersdelight.double_lure.desc";
            case MAGNETIC:
                return "tooltip.crabbersdelight.magnetic_lure.desc";
            case AUTOMATIC:
                return "tooltip.crabbersdelight.automatic_lure.desc";
            case STORM:
            default:
                return "tooltip.crabbersdelight.storm_lure.desc";
        }
    }
}
