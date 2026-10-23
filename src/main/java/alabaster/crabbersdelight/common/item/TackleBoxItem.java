package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.block.entity.TackleBoxBlockEntity;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.utils.CDDebug;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// 钓具箱物品（对应 1.21 版 TackleBoxItem）：
// 1.21 用 DataComponent 携带内容物，1.20.1 改成物品 NBT（"handler" 列表），放下时写回方块实体。
public class TackleBoxItem extends BlockItem {
    public static final String CONTENTS_KEY = "handler";

    public TackleBoxItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    public static ItemStack saveTileToItem(TackleBoxBlockEntity tackleBox) {
        ItemStack stack = new ItemStack(tackleBox.getCachedState().getBlock().asItem());
        boolean hasItems = false;
        for (int i = 0; i < tackleBox.handler.size(); i++) {
            if (!tackleBox.handler.getStack(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }
        if (hasItems) {
            stack.getOrCreateNbt().put(CONTENTS_KEY, tackleBox.handler.toNbtList());
        }
        return stack;
    }

    public static List<ItemStack> readContents(ItemStack stack) {
        List<ItemStack> contents = new ArrayList<>();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(CONTENTS_KEY)) {
            return contents;
        }
        NbtList list = nbt.getList(CONTENTS_KEY, 10);
        TackleBoxItemHandler temp = new TackleBoxItemHandler();
        temp.readNbtList(list);
        for (int i = 0; i < temp.size(); i++) {
            contents.add(temp.getStack(i));
        }
        return contents;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        List<ItemStack> contents = readContents(context.getStack());
        ActionResult result = super.useOnBlock(context);
        World world = context.getWorld();
        if (result.isAccepted() && !world.isClient && !contents.isEmpty()) {
            BlockPos placedPos = context.getBlockPos().offset(context.getSide());
            TackleBoxBlockEntity tackleBox = world.getBlockEntity(placedPos) instanceof TackleBoxBlockEntity box
                    ? box : null;
            // 兜底：点击的是可替换方块（草、雪层等）时方块会落在被点击的位置
            if (tackleBox == null && world.getBlockEntity(context.getBlockPos()) instanceof TackleBoxBlockEntity box2) {
                tackleBox = box2;
                placedPos = context.getBlockPos();
            }
            if (tackleBox != null) {
                for (int i = 0; i < contents.size() && i < tackleBox.handler.size(); i++) {
                    tackleBox.handler.setStack(i, contents.get(i));
                }
                tackleBox.markDirty();
                CDDebug.log("box.place pos={} restored={}", placedPos.toShortString(), describe(contents));
            } else {
                CDDebug.log("box.place FAILED to find BE at {} (clicked {})",
                        placedPos.toShortString(), context.getBlockPos().toShortString());
            }
        }
        return result;
    }

    private static String describe(List<ItemStack> contents) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            if (!contents.get(i).isEmpty()) {
                sb.append(i).append('=').append(net.minecraft.registry.Registries.ITEM.getId(contents.get(i).getItem())).append(' ');
            }
        }
        return sb.length() == 0 ? "empty" : sb.toString().trim();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        List<ItemStack> items = readContents(stack);
        if (!items.isEmpty()) {
            addSlotLine(tooltip, items, TackleBoxItemHandler.LURE_SLOT, "item.crabbersdelight.tackle_box.lure");
            addSlotLine(tooltip, items, TackleBoxItemHandler.LINE_SLOT, "item.crabbersdelight.tackle_box.line");
            addBaitLine(tooltip, items);
        }
        tooltip.add(Text.translatable("tooltip.crabbersdelight.tackle_box.hint").formatted(Formatting.DARK_GRAY));
    }

    private static void addSlotLine(List<Text> tooltip, List<ItemStack> items, int slot, String translationKey) {
        if (slot >= items.size()) {
            return;
        }
        ItemStack item = items.get(slot);
        if (!item.isEmpty()) {
            tooltip.add(Text.translatable(translationKey, item.getName()).formatted(Formatting.GRAY));
        }
    }

    private static void addBaitLine(List<Text> tooltip, List<ItemStack> items) {
        List<String> baitNames = new ArrayList<>();
        for (int slot : new int[]{TackleBoxItemHandler.BAIT_SLOT_1, TackleBoxItemHandler.BAIT_SLOT_2}) {
            if (slot < items.size() && !items.get(slot).isEmpty()) {
                baitNames.add(items.get(slot).getName().getString());
            }
        }
        if (!baitNames.isEmpty()) {
            tooltip.add(Text.translatable("item.crabbersdelight.tackle_box.bait", String.join(", ", baitNames))
                    .formatted(Formatting.GRAY));
        }
    }
}
