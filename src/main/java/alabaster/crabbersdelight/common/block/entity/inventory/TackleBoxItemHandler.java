package alabaster.crabbersdelight.common.block.entity.inventory;

import alabaster.crabbersdelight.common.tags.CDModTags;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

// 钓具箱库存（对应 1.21 版 TackleBoxItemHandler）：
// 0-14 通用槽，15 假饵，16 鱼线，17-18 鱼饵。
public class TackleBoxItemHandler extends SimpleInventory {
    public static final int GENERAL_SLOTS = 15;
    public static final int LURE_SLOT = 15;
    public static final int LINE_SLOT = 16;
    public static final int BAIT_SLOT_1 = 17;
    public static final int BAIT_SLOT_2 = 18;
    public static final int SLOTS = 19;

    public TackleBoxItemHandler() {
        super(SLOTS);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == LURE_SLOT) {
            return stack.isIn(CDModTags.FISHING_LURE);
        }
        if (slot == LINE_SLOT) {
            return stack.isIn(CDModTags.FISHING_LINE);
        }
        if (slot == BAIT_SLOT_1 || slot == BAIT_SLOT_2) {
            return stack.isIn(CDModTags.FISHING_BAIT);
        }
        return true;
    }

    // 带上槽位下标的序列化：SimpleInventory 自带的 toNbtList 按顺序写、会跳过空格子，
    // 会让假饵/鱼线/鱼饵在重新加载（或拆装箱子）后跑到 0、1、2 号通用格。
    @Override
    public NbtList toNbtList() {
        NbtList list = new NbtList();
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            NbtCompound entry = new NbtCompound();
            entry.putInt("Slot", i);
            stack.writeNbt(entry);
            list.add(entry);
        }
        return list;
    }

    @Override
    public void readNbtList(NbtList list) {
        clear();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < size()) {
                setStack(slot, ItemStack.fromNbt(entry));
            }
        }
    }
}
