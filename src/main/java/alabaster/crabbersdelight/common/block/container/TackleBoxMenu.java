package alabaster.crabbersdelight.common.block.container;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.registry.CDModMenus;
import alabaster.crabbersdelight.common.tags.CDModTags;
import alabaster.crabbersdelight.common.utils.CDDebug;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

// 钓具箱界面（对应 1.21 版 TackleBoxMenu）：
// 左侧 4 个专用槽（假饵 / 鱼线 / 鱼饵 ×2），右侧 3×5 通用槽，下面玩家背包 + 快捷栏。
//
// 注意槽位注册顺序：先通用槽（0–14）再专用槽（15–18），
// 这样「菜单下标」与「库存下标」一一对应，quickMove 里可以直接用库存下标定位菜单槽。
// （反过来先注册专用槽时，专用槽会占用菜单下标 0–3，而库存下标仍是 15–18，
//   shift 点击就会把物品塞进错误的槽——之前就是这个 bug。）
public class TackleBoxMenu extends ScreenHandler {
    public final TackleBoxItemHandler inventory;

    public static final Identifier LURE_SLOT_ICON = CrabbersDelightFabric.id("gui/lure_slot");
    public static final Identifier LINE_SLOT_ICON = CrabbersDelightFabric.id("gui/line_slot");
    public static final Identifier BAIT_SLOT_ICON = CrabbersDelightFabric.id("gui/bait_slot");
    private static final Identifier BLOCKS_ATLAS = new Identifier("textures/atlas/blocks.png");

    public TackleBoxMenu(int syncId, PlayerInventory playerInventory, TackleBoxItemHandler inventory) {
        super(CDModMenus.TACKLE_BOX_MENU, syncId);
        this.inventory = inventory;

        int startX = 8;
        int borderSlotSize = 18;
        int topY = 18;

        // 1) 通用槽：库存 0–14 → 菜单下标 0–14
        int generalStartX = startX + borderSlotSize * 2 + 4 + 30 + 2;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                this.addSlot(new Slot(inventory, row * 5 + column,
                        generalStartX + column * borderSlotSize, topY + row * borderSlotSize));
            }
        }

        // 2) 专用槽：库存 15–18 → 菜单下标 15–18
        // 除了 inventory.isValid 之外再显式挡一层 canInsert，避免 Slot 实现差异
        this.addSlot(new Slot(inventory, TackleBoxItemHandler.LURE_SLOT, 19, 22) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(CDModTags.FISHING_LURE);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BLOCKS_ATLAS, LURE_SLOT_ICON);
            }
        });

        this.addSlot(new Slot(inventory, TackleBoxItemHandler.LINE_SLOT, 47, 22) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(CDModTags.FISHING_LINE);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BLOCKS_ATLAS, LINE_SLOT_ICON);
            }
        });

        this.addSlot(new Slot(inventory, TackleBoxItemHandler.BAIT_SLOT_1, 19, 50) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(CDModTags.FISHING_BAIT);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BLOCKS_ATLAS, BAIT_SLOT_ICON);
            }
        });

        this.addSlot(new Slot(inventory, TackleBoxItemHandler.BAIT_SLOT_2, 47, 50) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(CDModTags.FISHING_BAIT);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BLOCKS_ATLAS, BAIT_SLOT_ICON);
            }
        });

        // 3) 玩家背包 + 快捷栏
        int startPlayerInvY = 84;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column,
                        startX + (column * borderSlotSize), startPlayerInvY + (row * borderSlotSize)));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, startX + (column * borderSlotSize), 142));
        }

        if (CDDebug.enabled()) {
            CDDebug.log("menu.open lure={} line={} bait1={} bait2={} general0={} slots={}",
                    inventory.getStack(TackleBoxItemHandler.LURE_SLOT),
                    inventory.getStack(TackleBoxItemHandler.LINE_SLOT),
                    inventory.getStack(TackleBoxItemHandler.BAIT_SLOT_1),
                    inventory.getStack(TackleBoxItemHandler.BAIT_SLOT_2),
                    inventory.getStack(0),
                    this.slots.size());
        }
    }

    public TackleBoxMenu(int syncId, PlayerInventory playerInventory, PacketByteBuf data) {
        this(syncId, playerInventory, new TackleBoxItemHandler());
    }

    // 玩家背包里的物品 shift 点击时，先尝试放进对应的专用槽，放不下再进通用格。
    // 这里返回的是库存下标，同时也是菜单下标（因为通用槽先注册）。
    private int preferredSlotFor(ItemStack stack) {
        if (stack.isIn(CDModTags.FISHING_LURE)) {
            return TackleBoxItemHandler.LURE_SLOT;
        }
        if (stack.isIn(CDModTags.FISHING_LINE)) {
            return TackleBoxItemHandler.LINE_SLOT;
        }
        if (stack.isIn(CDModTags.FISHING_BAIT)) {
            if (this.inventory.getStack(TackleBoxItemHandler.BAIT_SLOT_1).isEmpty()) {
                return TackleBoxItemHandler.BAIT_SLOT_1;
            }
            if (this.inventory.getStack(TackleBoxItemHandler.BAIT_SLOT_2).isEmpty()) {
                return TackleBoxItemHandler.BAIT_SLOT_2;
            }
            return TackleBoxItemHandler.BAIT_SLOT_1;
        }
        return -1;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack result = stack.copy();
        boolean moved;

        if (index < this.inventory.size()) {
            // 从钓具箱取出 → 进玩家背包
            moved = this.insertItem(stack, this.inventory.size(), this.slots.size(), true);
        } else {
            // 从玩家背包放进钓具箱 → 优先专用槽（用 insertItem 走 canInsert，能合并同类物品）
            int preferred = preferredSlotFor(stack);
            moved = preferred >= 0 && this.insertItem(stack, preferred, preferred + 1, false);

            if (!moved && stack.isIn(CDModTags.FISHING_BAIT)) {
                // 两个鱼饵槽都试试（第一个满了还能堆到第二个）
                moved = this.insertItem(stack, TackleBoxItemHandler.BAIT_SLOT_1,
                        TackleBoxItemHandler.BAIT_SLOT_2 + 1, false);
            }
            if (!moved) {
                // 兜底：通用格
                moved = this.insertItem(stack, 0, this.inventory.size(), false);
            }

            if (CDDebug.enabled()) {
                CDDebug.log("menu.quickMove item={} index={} preferred={} moved={} left={}",
                        stack.getItem(), index, preferred, moved, stack.getCount());
            }
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
