package alabaster.crabbersdelight.common.network;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.NoteBlock;
import alabaster.crabbersdelight.common.block.entity.NoteBlockEntity;
import alabaster.crabbersdelight.common.item.NoteData;
import alabaster.crabbersdelight.common.item.NoteItem;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

// 便条的网络同步（对应 1.21 版 NoteNetworking）：
// 客户端（client 源集的 NoteClientNetwork）把文本发过来，这里在服务端校验后写进物品 NBT / 方块实体。
public final class NoteNetworking {
    public static final Identifier NOTE_EDIT = CrabbersDelightFabric.id("note_edit");
    public static final Identifier NOTE_SIGN = CrabbersDelightFabric.id("note_sign");
    public static final Identifier NOTE_BLOCK_EDIT = CrabbersDelightFabric.id("note_block_edit");
    public static final Identifier NOTE_BLOCK_SIGN = CrabbersDelightFabric.id("note_block_sign");

    private static final double MAX_BLOCK_DISTANCE_SQ = 64.0;

    private NoteNetworking() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(NOTE_EDIT, (server, player, handler, buf, responseSender) -> {
            boolean mainHand = buf.readBoolean();
            String text = buf.readString(NoteData.MAX_NOTE_LENGTH);
            server.execute(() -> applyEdit(player, mainHand, text));
        });

        ServerPlayNetworking.registerGlobalReceiver(NOTE_SIGN, (server, player, handler, buf, responseSender) -> {
            boolean mainHand = buf.readBoolean();
            String title = buf.readString(NoteData.MAX_TITLE_LENGTH);
            String text = buf.readString(NoteData.MAX_NOTE_LENGTH);
            server.execute(() -> applySign(player, mainHand, title, text));
        });

        ServerPlayNetworking.registerGlobalReceiver(NOTE_BLOCK_EDIT, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String text = buf.readString(NoteData.MAX_NOTE_LENGTH);
            server.execute(() -> applyBlockEdit(player, pos, text));
        });

        ServerPlayNetworking.registerGlobalReceiver(NOTE_BLOCK_SIGN, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String title = buf.readString(NoteData.MAX_TITLE_LENGTH);
            String text = buf.readString(NoteData.MAX_NOTE_LENGTH);
            server.execute(() -> applyBlockSign(player, pos, title, text));
        });
    }

    private static void applyEdit(ServerPlayerEntity player, boolean mainHand, String text) {
        Hand hand = mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof NoteItem)) {
            return;
        }
        NoteData.setText(stack, NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH));
        player.currentScreenHandler.syncState();
    }

    private static void applySign(ServerPlayerEntity player, boolean mainHand, String title, String text) {
        Hand hand = mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof NoteItem)) {
            return;
        }

        String safeTitle = NoteData.truncate(title, NoteData.MAX_TITLE_LENGTH);
        ItemStack signed = new ItemStack(CDModItems.SIGNED_NOTE);
        NoteData.sign(signed, safeTitle, text, player.getGameProfile().getName(), NoteData.GENERATION_ORIGINAL);
        if (!safeTitle.isBlank()) {
            signed.setCustomName(Text.literal(safeTitle));
        }

        if (stack.getCount() > 1) {
            stack.decrement(1);
            if (!player.getInventory().insertStack(signed)) {
                player.dropItem(signed, false);
            }
        } else {
            player.setStackInHand(hand, signed);
        }
    }

    private static void applyBlockEdit(ServerPlayerEntity player, BlockPos pos, String text) {
        if (!withinReach(player, pos)) {
            return;
        }
        if (!(player.getWorld().getBlockEntity(pos) instanceof NoteBlockEntity note) || note.isSigned()) {
            return;
        }
        note.setUnsignedText(text);
        note.markSynced();
    }

    private static void applyBlockSign(ServerPlayerEntity player, BlockPos pos, String title, String text) {
        if (!withinReach(player, pos)) {
            return;
        }
        if (!(player.getWorld().getBlockEntity(pos) instanceof NoteBlockEntity note) || note.isSigned()) {
            return;
        }

        note.signDirectly(NoteData.truncate(title, NoteData.MAX_TITLE_LENGTH), text,
                player.getGameProfile().getName(), NoteData.GENERATION_ORIGINAL);

        BlockState state = note.getCachedState();
        if (state.contains(NoteBlock.SIGNED)) {
            BlockState signedState = state.with(NoteBlock.SIGNED, true);
            player.getWorld().setBlockState(pos, signedState, Block.NOTIFY_ALL);
            player.getWorld().updateListeners(pos, signedState, signedState, Block.NOTIFY_ALL);
        }
    }

    private static boolean withinReach(ServerPlayerEntity player, BlockPos pos) {
        return player.getBlockPos().getSquaredDistance(pos) <= MAX_BLOCK_DISTANCE_SQ;
    }
}
