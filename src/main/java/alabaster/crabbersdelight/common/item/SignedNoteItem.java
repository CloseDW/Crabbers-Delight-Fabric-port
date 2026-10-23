package alabaster.crabbersdelight.common.item;

import alabaster.crabbersdelight.common.block.NoteBlock;
import alabaster.crabbersdelight.common.block.entity.NoteBlockEntity;
import alabaster.crabbersdelight.common.utils.NoteClientHooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 已签名便条（对应 1.21 版 SignedNoteItem）：
// - 右键空气：打开阅读界面
// - 右键能贴的墙 / 地面：放置成"已签名"的便条方块，标题/正文/作者/代际一起带过去
// - 右键其它位置：打开阅读界面
// 1.21 版里它是 BlockItem；这里同样继承 BlockItem，否则没法放置。
public class SignedNoteItem extends BlockItem {
    public SignedNoteItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public String getTranslationKey() {
        return "item.crabbersdelight.signed_note";
    }

    // 签名后有标题就用标题当物品名（1.21 版 SignedNoteItem#getName 同样处理）
    @Override
    public Text getName(ItemStack stack) {
        String title = NoteData.title(stack);
        if (title == null || title.isBlank()) {
            return super.getName(stack);
        }
        return Text.literal(title);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient && NoteData.isSigned(stack)) {
            NoteClientHooks.openRead(NoteData.title(stack), NoteData.body(stack), NoteData.author(stack));
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        ItemStack stack = context.getStack();

        boolean canWall = canAttachToWall(context);
        boolean canFlat = canAttachFlat(context);

        if (!canWall && !canFlat) {
            if (world.isClient && NoteData.isSigned(stack)) {
                NoteClientHooks.openRead(NoteData.title(stack), NoteData.body(stack), NoteData.author(stack));
            }
            return ActionResult.success(world.isClient);
        }

        // place() 会把手上这张牌消耗掉（count 归零），所以先把内容取出来
        boolean signed = NoteData.isSigned(stack);
        String title = NoteData.title(stack);
        String body = NoteData.body(stack);
        String author = NoteData.author(stack);
        int generation = NoteData.generation(stack);

        ItemPlacementContext placeContext = new ItemPlacementContext(context);
        ActionResult result = this.place(placeContext);
        if (result.isAccepted() && !world.isClient && signed) {
            BlockPos placedPos = placeContext.getBlockPos();
            if (world.getBlockEntity(placedPos) instanceof NoteBlockEntity note) {
                note.signDirectly(title, body, author, generation);
                BlockState signedState = world.getBlockState(placedPos).with(NoteBlock.SIGNED, true);
                world.setBlockState(placedPos, signedState, Block.NOTIFY_ALL);
                note.markSynced();
            }
        }
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (!NoteData.isSigned(stack)) {
            return;
        }
        tooltip.add(Text.translatable("item.crabbersdelight.signed_note.author", NoteData.author(stack))
                .formatted(Formatting.GRAY));
        int generation = NoteData.generation(stack);
        tooltip.add(Text.translatable(NoteData.generationName(generation))
                .formatted(NoteData.generationColor(generation)));
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
