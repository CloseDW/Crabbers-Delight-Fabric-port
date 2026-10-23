package alabaster.crabbersdelight.common.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 鱼线（对应 1.21 版 FishingLineItem）
public class FishingLineItem extends Item {
    public FishingLineItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.crabbersdelight.fishing_line.desc").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.crabbersdelight.gear.install").formatted(Formatting.DARK_GRAY));
    }
}
