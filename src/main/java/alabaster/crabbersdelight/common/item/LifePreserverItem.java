package alabaster.crabbersdelight.common.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 救生圈物品（对应 1.21 版 LifePreserverItem）：右键直接穿到腿上；浮力逻辑见 LifePreserverEvents。
public class LifePreserverItem extends BlockItem {
    public LifePreserverItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);
        ItemStack currentLegs = user.getEquippedStack(EquipmentSlot.LEGS);

        if (currentLegs.isOf(this)) {
            return TypedActionResult.pass(held);
        }
        if (EnchantmentHelper.hasBindingCurse(currentLegs) && !user.getAbilities().creativeMode) {
            return TypedActionResult.fail(held);
        }

        if (!world.isClient) {
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        user.equipStack(EquipmentSlot.LEGS, new ItemStack(this));
        if (!user.getAbilities().creativeMode) {
            held.decrement(1);
        }
        if (!currentLegs.isEmpty() && !user.getInventory().insertStack(currentLegs)) {
            user.dropItem(currentLegs, false);
        }

        return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.crabbersdelight.life_preserver.desc").formatted(Formatting.GRAY));
    }
}
