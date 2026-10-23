package alabaster.crabbersdelight.client.network;

import alabaster.crabbersdelight.common.item.NoteData;
import alabaster.crabbersdelight.common.network.NoteNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

// 客户端 → 服务端的便条数据包（client 源集，避免主源集引用客户端专用 API）。
public final class NoteClientNetwork {
    private NoteClientNetwork() {
    }

    public static void sendEdit(Hand hand, String text) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(hand == Hand.MAIN_HAND);
        buf.writeString(NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH));
        ClientPlayNetworking.send(NoteNetworking.NOTE_EDIT, buf);
    }

    public static void sendSign(Hand hand, String title, String text) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(hand == Hand.MAIN_HAND);
        buf.writeString(NoteData.truncate(title, NoteData.MAX_TITLE_LENGTH));
        buf.writeString(NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH));
        ClientPlayNetworking.send(NoteNetworking.NOTE_SIGN, buf);
    }

    public static void sendBlockEdit(BlockPos pos, String text) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeString(NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH));
        ClientPlayNetworking.send(NoteNetworking.NOTE_BLOCK_EDIT, buf);
    }

    public static void sendBlockSign(BlockPos pos, String title, String text) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeString(NoteData.truncate(title, NoteData.MAX_TITLE_LENGTH));
        buf.writeString(NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH));
        ClientPlayNetworking.send(NoteNetworking.NOTE_BLOCK_SIGN, buf);
    }
}
