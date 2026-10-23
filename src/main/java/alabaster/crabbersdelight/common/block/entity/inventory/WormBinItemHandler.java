package alabaster.crabbersdelight.common.block.entity.inventory;

import alabaster.crabbersdelight.common.block.entity.WormBinBlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

// 蚯蚓箱：0 号槽放堆肥原料，1 号槽攒蚯蚓（对应 1.21 版的 WormBinItemHandler）
public class WormBinItemHandler extends SimpleInventory {
    public static final int FUEL_SLOT = 0;
    public static final int WORM_SLOT = 1;

    public WormBinItemHandler() {
        super(2);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == FUEL_SLOT) {
            return WormBinBlockEntity.isValidFuel(stack);
        }
        return false;
    }

    // 同样需要带槽位的序列化，否则原料/蚯蚓位置会错乱
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
