package alabaster.crabbersdelight.common.block.entity;

import alabaster.crabbersdelight.common.item.NoteData;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

// 便条方块实体（对应 1.21 版 NoteBlockEntity）：保存正文、标题、作者、代际与是否已签名。
public class NoteBlockEntity extends BlockEntity {
    private String text = "";
    private String author = "";
    private String title = "";
    private int generation = 0;
    private boolean signed = false;

    public NoteBlockEntity(BlockPos pos, BlockState state) {
        super(CDModBlockEntity.NOTE, pos, state);
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public int getGeneration() {
        return generation;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setUnsignedText(String text) {
        if (this.signed) {
            return;
        }
        this.text = NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH);
        this.markDirty();
    }

    public void signDirectly(String title, String text, String author, int generation) {
        this.text = NoteData.truncate(text, NoteData.MAX_NOTE_LENGTH);
        this.title = NoteData.truncate(title, NoteData.MAX_TITLE_LENGTH);
        this.author = author == null ? "" : author;
        this.generation = generation;
        this.signed = true;
        this.markDirty();
    }

    // 破坏方块时把内容带回物品，避免文字丢失
    public ItemStack toItemStack() {
        if (!this.signed) {
            ItemStack stack = new ItemStack(CDModItems.NOTE);
            NoteData.setText(stack, this.text);
            return stack;
        }
        ItemStack stack = new ItemStack(CDModItems.SIGNED_NOTE);
        NoteData.sign(stack, this.title, this.text, this.author, this.generation);
        if (!this.title.isBlank()) {
            stack.setCustomName(Text.literal(this.title));
        }
        return stack;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("Text", this.text);
        nbt.putString("Author", this.author);
        nbt.putString("Title", this.title);
        nbt.putInt("Generation", this.generation);
        nbt.putBoolean("Signed", this.signed);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.text = nbt.getString("Text");
        this.author = nbt.getString("Author");
        this.title = nbt.getString("Title");
        this.generation = nbt.getInt("Generation");
        this.signed = nbt.getBoolean("Signed");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void markSynced() {
        if (this.world != null) {
            BlockState state = this.getCachedState();
            this.world.updateListeners(this.pos, state, state, Block.NOTIFY_ALL);
        }
    }
}
