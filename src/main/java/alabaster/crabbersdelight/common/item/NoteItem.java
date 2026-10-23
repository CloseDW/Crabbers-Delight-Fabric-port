package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.block.entity.NoteBlockEntity;
import alabaster.crabbersdelight.common.utils.NoteClientHooks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// 空白便条（对应 1.21 版 NoteItem）：既是普通物品也是方块物品。
// - 右键空气：打开编辑界面
// - 右键能贴的墙 / 地面：放置成便条方块，正文随物品带过去
// - 右键其它位置：打开编辑界面
public class NoteItem extends BlockItem {
    public NoteItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public String getTranslationKey() {
        return "item.crabbersdelight.note";
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            NoteClientHooks.openEdit(hand, NoteData.getText(stack));
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        boolean canWall = canAttachToWall(context);
        boolean canFlat = canAttachFlat(context);

        if (!canWall && !canFlat) {
            if (world.isClient) {
                NoteClientHooks.openEdit(context.getHand(), NoteData.getText(context.getStack()));
            }
            return ActionResult.success(world.isClient);
        }

        ItemPlacementContext placeContext = new ItemPlacementContext(context);
        ActionResult result = this.place(placeContext);
        if (result.isAccepted() && !world.isClient) {
            BlockPos placedPos = placeContext.getBlockPos();
            if (world.getBlockEntity(placedPos) instanceof NoteBlockEntity note) {
                note.setUnsignedText(NoteData.getText(context.getStack()));
                note.markSynced();
            }
        }
        return result;
    }

    private static boolean canAttachToWall(ItemUsageContext context) {
        Direction face = context.getSide();
        if (!face.getAxis().isHorizontal()) {
            return false;
        }
        BlockPos wallPos = context.getBlockPos();
        return context.getWorld().getBlockState(wallPos).isSideSolidFullSquare(context.getWorld(), wallPos, face);
    }

    private static boolean canAttachFlat(ItemUsageContext context) {
        if (context.getSide() != Direction.UP) {
            return false;
        }
        BlockPos pos = context.getBlockPos();
        return context.getWorld().getBlockState(pos).isSideSolidFullSquare(context.getWorld(), pos, Direction.UP);
    }
}
