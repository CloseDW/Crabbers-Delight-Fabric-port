package alabaster.crabbersdelight.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vectorwing.farmersdelight.common.block.OrganicCompostBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

// 蚯蚓（对应 1.21 版 WormItem）：对农夫乐事的有机肥堆右键，推进一格堆肥进度；满了就变成沃土。
public class WormItem extends Item {
    private static final int BONEMEAL_USE_EVENT = 1505;

    public WormItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof OrganicCompostBlock compostBlock)) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            int stage = state.get(OrganicCompostBlock.COMPOSTING);
            int maxStage = compostBlock.getMaxCompostingStage();

            BlockState advanced = stage >= maxStage
                    ? ModBlocks.RICH_SOIL.get().getDefaultState()
                    : state.with(OrganicCompostBlock.COMPOSTING, stage + 1);

            world.setBlockState(pos, advanced, Block.NOTIFY_ALL);
            world.syncWorldEvent(BONEMEAL_USE_EVENT, pos, 0);
            context.getStack().decrement(1);
        }

        return ActionResult.success(world.isClient);
    }
}
