package alabaster.crabbersdelight.common.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 海螺（对应 1.21 版 ConchItem）：右键贴耳边听海，按玩家幸运值输出 5 档之一的海象预兆。
public class ConchItem extends Item {
    private static final int COOLDOWN_TICKS = 100;

    public ConchItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient) {
            LuckTier tier = LuckTier.fromLuck(user.getLuck());
            user.sendMessage(tier.randomMessage(user.getRandom()), true);
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.crabbersdelight.conch.desc").formatted(Formatting.GRAY));
    }

    private enum LuckTier {
        VERY_BAD("very_bad", 3),
        BAD("bad", 3),
        NEUTRAL("neutral", 3),
        GOOD("good", 3),
        VERY_GOOD("very_good", 3);

        private final String keySuffix;
        private final int messageCount;

        LuckTier(String keySuffix, int messageCount) {
            this.keySuffix = keySuffix;
            this.messageCount = messageCount;
        }

        Text randomMessage(Random random) {
            int index = random.nextInt(messageCount) + 1;
            return Text.translatable("message.crabbersdelight.conch." + keySuffix + "." + index)
                    .formatted(Formatting.AQUA, Formatting.ITALIC);
        }

        static LuckTier fromLuck(float luck) {
            if (luck <= -0.5F) {
                return VERY_BAD;
            } else if (luck < -0.05F) {
                return BAD;
            } else if (luck <= 0.05F) {
                return NEUTRAL;
            } else if (luck < 0.5F) {
                return GOOD;
            } else {
                return VERY_GOOD;
            }
        }
    }
}
