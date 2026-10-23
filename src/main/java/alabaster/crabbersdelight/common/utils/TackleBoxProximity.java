package alabaster.crabbersdelight.common.utils;

import alabaster.crabbersdelight.common.block.entity.TackleBoxBlockEntity;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.item.TackleBoxItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

// 找玩家能用的钓具箱（对应 1.21 版 TackleBoxProximity）：
// 顺序为主手 → 副手 → 快捷栏 → 3 格范围内最近的已放置钓具箱。
public final class TackleBoxProximity {
    private static final int SEARCH_RADIUS = 3;

    private TackleBoxProximity() {
    }

    public interface TackleBoxAccess {
        ItemStack getSlot(int index);

        void setSlot(int index, ItemStack stack);

        String describe();
    }

    // 容错：先看专用槽，专用槽空的时候才去 15 个通用格里找第一个匹配的物品。
    // （否则玩家把假饵/鱼线/鱼饵塞进通用格后，装备系统会读不到、表现为"完全没效果"）
    public static int findSlot(TackleBoxAccess box, int preferredSlot, java.util.function.Predicate<ItemStack> matcher) {
        if (box == null) {
            return -1;
        }
        if (!box.getSlot(preferredSlot).isEmpty() && matcher.test(box.getSlot(preferredSlot))) {
            return preferredSlot;
        }
        for (int i = 0; i < TackleBoxItemHandler.GENERAL_SLOTS; i++) {
            ItemStack stack = box.getSlot(i);
            if (!stack.isEmpty() && matcher.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack getSlot(TackleBoxAccess box, int slot) {
        return box == null || slot < 0 ? ItemStack.EMPTY : box.getSlot(slot);
    }

    public static TackleBoxAccess find(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof TackleBoxItem) {
            return heldAccess(player.getMainHandStack(), "held(main)");
        }
        if (player.getOffHandStack().getItem() instanceof TackleBoxItem) {
            return heldAccess(player.getOffHandStack(), "held(off)");
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.getItem() instanceof TackleBoxItem) {
                return heldAccess(stack, "held(hotbar " + slot + ")");
            }
        }
        return findClosestPlaced(player);
    }

    private static TackleBoxAccess findClosestPlaced(PlayerEntity player) {
        BlockPos center = player.getBlockPos();
        TackleBoxBlockEntity closest = null;
        double closestDistSqr = Double.MAX_VALUE;
        BlockPos closestPos = null;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (player.getWorld().getBlockEntity(pos) instanceof TackleBoxBlockEntity tackleBox) {
                        double distSqr = pos.getSquaredDistance(center);
                        if (distSqr < closestDistSqr) {
                            closestDistSqr = distSqr;
                            closest = tackleBox;
                            closestPos = pos.toImmutable();
                        }
                    }
                }
            }
        }

        if (closest == null || closestPos == null) {
            return null;
        }
        BlockPos finalPos = closestPos;
        TackleBoxBlockEntity finalBox = closest;
        return placedAccess(finalBox, "placed(" + finalPos.toShortString() + ")");
    }

    // 手持的钓具箱：内容物存在物品 NBT 里（1.21 是数据组件）
    private static TackleBoxAccess heldAccess(ItemStack holder, String source) {
        return new TackleBoxAccess() {
            @Override
            public ItemStack getSlot(int index) {
                TackleBoxItemHandler handler = read(holder);
                return index < handler.size() ? handler.getStack(index) : ItemStack.EMPTY;
            }

            @Override
            public void setSlot(int index, ItemStack stack) {
                TackleBoxItemHandler handler = read(holder);
                if (index >= handler.size()) {
                    return;
                }
                handler.setStack(index, stack);
                write(holder, handler);
            }

            @Override
            public String describe() {
                return source;
            }
        };
    }

    private static TackleBoxAccess placedAccess(TackleBoxBlockEntity tackleBox, String source) {
        return new TackleBoxAccess() {
            @Override
            public ItemStack getSlot(int index) {
                return index < tackleBox.handler.size() ? tackleBox.handler.getStack(index) : ItemStack.EMPTY;
            }

            @Override
            public void setSlot(int index, ItemStack stack) {
                if (index < tackleBox.handler.size()) {
                    tackleBox.handler.setStack(index, stack);
                }
            }

            @Override
            public String describe() {
                return source;
            }
        };
    }

    private static TackleBoxItemHandler read(ItemStack holder) {
        TackleBoxItemHandler handler = new TackleBoxItemHandler();
        NbtCompound nbt = holder.getNbt();
        if (nbt != null && nbt.contains(TackleBoxItem.CONTENTS_KEY)) {
            handler.readNbtList(nbt.getList(TackleBoxItem.CONTENTS_KEY, 10));
        }
        return handler;
    }

    private static void write(ItemStack holder, TackleBoxItemHandler handler) {
        boolean hasItems = false;
        for (int i = 0; i < handler.size(); i++) {
            if (!handler.getStack(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }
        NbtCompound nbt = holder.getOrCreateNbt();
        if (hasItems) {
            nbt.put(TackleBoxItem.CONTENTS_KEY, handler.toNbtList());
        } else {
            nbt.remove(TackleBoxItem.CONTENTS_KEY);
        }
    }
}
