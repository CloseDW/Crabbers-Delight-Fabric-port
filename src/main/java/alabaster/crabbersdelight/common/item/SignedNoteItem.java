package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.utils.NoteClientHooks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 已签名便条（对应 1.21 版 SignedNoteItem）：右键阅读，物品名（自定义名）显示标题，
// tooltip 显示作者与"原版/副本/破损"等代际信息。
public class SignedNoteItem extends Item {
    public SignedNoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient && NoteData.isSigned(stack)) {
            NoteClientHooks.openRead(NoteData.title(stack), NoteData.body(stack), NoteData.author(stack));
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (!NoteData.isSigned(stack)) {
            return;
        }
        tooltip.add(Text.translatable("item.crabbersdelight.signed_note.author", NoteData.author(stack))
                .formatted(Formatting.GRAY));
        int generation = NoteData.generation(stack);
        tooltip.add(Text.translatable(NoteData.generationName(generation))
                .formatted(NoteData.generationColor(generation)));
    }
}
