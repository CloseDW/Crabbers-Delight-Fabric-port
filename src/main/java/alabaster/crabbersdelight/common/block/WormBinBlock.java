package alabaster.crabbersdelight.common.block;

import alabaster.crabbersdelight.common.block.entity.WormBinBlockEntity;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// 蚯蚓箱（对应 1.21 版 WormBinBlock）：空手右键收蚯蚓，潜行空手右键取回堆肥原料，手持可堆肥物右键投料。
public class WormBinBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 0.125),
            VoxelShapes.cuboid(0.0, 0.0, 0.875, 1.0, 1.0, 1.0),
            VoxelShapes.cuboid(0.0, 0.0, 0.125, 0.125, 1.0, 0.875),
            VoxelShapes.cuboid(0.875, 0.0, 0.125, 1.0, 1.0, 0.875),
            VoxelShapes.cuboid(0.125, 0.0, 0.125, 0.875, 0.875, 0.875)
    );

    public WormBinBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WormBinBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, CDModBlockEntity.WORM_BIN, WormBinBlockEntity::serverTick);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof WormBinBlockEntity wormBin) {
                ItemScatterer.spawn(world, pos, wormBin.getHandler());
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof WormBinBlockEntity wormBin)) {
            return ActionResult.PASS;
        }

        // 手持可堆肥物 → 投料
        if (!stack.isEmpty()) {
            return wormBin.insertFuel(stack) ? ActionResult.CONSUME : ActionResult.PASS;
        }

        // 空手：潜行取回原料，否则收蚯蚓
        ItemStack result = player.isSneaking() ? wormBin.ejectFuel() : wormBin.collectWorms();
        if (!result.isEmpty() && !player.getInventory().insertStack(result)) {
            player.dropItem(result, false);
        }
        return ActionResult.success(false);
    }
}
