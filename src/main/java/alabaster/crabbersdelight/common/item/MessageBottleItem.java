package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.registry.CDModItems;
import alabaster.crabbersdelight.common.utils.BottleMessages;
import alabaster.crabbersdelight.common.world.BottledNotesState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 瓶中信（对应 1.21 版 MessageBottleItem）：潜行 + 右键打开，随机抽一条留言变成签名便笺，
// 同时把空玻璃瓶还给玩家。1.21 版的"漂流瓶存档数据 / 宝藏地图"分支没有移植。
public class MessageBottleItem extends Item {
    public MessageBottleItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.crabbersdelight.message_bottle.desc").formatted(Formatting.GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!user.isSneaking()) {
            return TypedActionResult.pass(stack);
        }
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            openBottle(serverPlayer, stack);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    private void openBottle(ServerPlayerEntity player, ItemStack bottleStack) {
        ItemStack note = ItemStack.EMPTY;

        // 先看世界里有没有别的玩家扔进海里的瓶中便笺（50% 概率优先取走一条）
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            BottledNotesState state = BottledNotesState.get(serverWorld);
            java.util.List<BottledNotesState.Entry> saved = state.asList();
            if (!saved.isEmpty() && player.getRandom().nextBoolean()) {
                BottledNotesState.Entry entry = saved.get(player.getRandom().nextInt(saved.size()));
                state.remove(entry);
                note = new ItemStack(CDModItems.SIGNED_NOTE);
                NoteData.sign(note, entry.title(), entry.text(), entry.author(), entry.generation());
                if (!entry.title().isBlank()) {
                    note.setCustomName(Text.literal(entry.title()));
                }
            }
        }

        if (note.isEmpty()) {
            BottleMessages.Message message = BottleMessages.random(player.getRandom());
            String body = message.text().replace("%player%", player.getName().getString());
            note = new ItemStack(CDModItems.SIGNED_NOTE);
            NoteData.sign(note, message.title(), body, message.author(), NoteData.GENERATION_ORIGINAL);
            if (!message.title().isBlank()) {
                note.setCustomName(Text.literal(message.title()));
            }
        }

        bottleStack.decrement(1);

        ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!player.getInventory().insertStack(emptyBottle)) {
            player.dropItem(emptyBottle, false);
        }
        if (!player.getInventory().insertStack(note)) {
            player.dropItem(note, false);
        }

        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.sendMessage(Text.translatable("message.crabbersdelight.message_bottle.opened")
                .formatted(Formatting.GRAY), true);
    }
}
