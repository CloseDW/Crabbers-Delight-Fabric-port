package alabaster.crabbersdelight.common.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// 玩家扔进海里的瓶中便笺（对应 1.21 版 BottledNoteSavedData）：
// 1.20.1 用 PersistentState 存到主世界的 data 目录里，最多排队 500 条，先进先出。
public class BottledNotesState extends PersistentState {
    public record Entry(String title, String text, String author, int generation) {
    }

    private static final String ID = "crabbersdelight_bottled_notes";
    private static final int MAX_QUEUED = 500;

    private final Deque<Entry> queue = new ArrayDeque<>();

    public static BottledNotesState get(ServerWorld world) {
        ServerWorld overworld = world.getServer().getOverworld();
        return overworld.getPersistentStateManager()
                .getOrCreate(BottledNotesState::fromNbt, BottledNotesState::new, ID);
    }

    private static BottledNotesState fromNbt(NbtCompound nbt) {
        BottledNotesState state = new BottledNotesState();
        NbtList list = nbt.getList("Notes", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            state.queue.addLast(new Entry(entry.getString("Title"), entry.getString("Text"),
                    entry.getString("Author"), entry.getInt("Generation")));
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Entry entry : this.queue) {
            NbtCompound tag = new NbtCompound();
            tag.putString("Title", entry.title());
            tag.putString("Text", entry.text());
            tag.putString("Author", entry.author());
            tag.putInt("Generation", entry.generation());
            list.add(tag);
        }
        nbt.put("Notes", list);
        return nbt;
    }

    public void addNote(String title, String text, String author, int generation) {
        this.queue.addLast(new Entry(title, text, author, generation));
        while (this.queue.size() > MAX_QUEUED) {
            this.queue.pollFirst();
        }
        this.markDirty();
    }

    public List<Entry> asList() {
        return new ArrayList<>(this.queue);
    }

    public void remove(Entry entry) {
        if (this.queue.remove(entry)) {
            this.markDirty();
        }
    }

    public int size() {
        return this.queue.size();
    }
}
