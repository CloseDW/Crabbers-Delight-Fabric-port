package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.entity.ThrownBottledNote;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 瓶中便笺（对应 1.21 版 BottledNoteItem）：把签名便笺装进瓶子再扔出去。
// 落水 → 记入世界存档；落地 → 变回签名便笺。
public class BottledNoteItem extends Item {
    public BottledNoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (NoteData.isSigned(stack)) {
            tooltip.add(Text.translatable("item.crabbersdelight.signed_note.author", NoteData.author(stack))
                    .formatted(Formatting.GRAY));
        }
        tooltip.add(Text.translatable("item.crabbersdelight.bottled_note.desc").formatted(Formatting.GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!NoteData.isSigned(stack)) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient) {
            ThrownBottledNote projectile = new ThrownBottledNote(world, user,
                    NoteData.title(stack), NoteData.body(stack), NoteData.author(stack), NoteData.generation(stack));
            projectile.setItem(new ItemStack(CDModItems.BOTTLED_NOTE));
            projectile.setVelocity(user, user.getPitch(), user.getYaw(), -20.0f, 0.5f, 1.0f);
            world.spawnEntity(projectile);
            stack.decrement(1);
        }

        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_SPLASH_POTION_THROW,
                SoundCategory.PLAYERS, 0.5f, 0.9f);
        user.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, world.isClient);
    }
}
