package alabaster.crabbersdelight.common.block;

import alabaster.crabbersdelight.common.block.entity.FishPlaqueBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;


// 鱼牌（对应 1.21 版 FishPlaqueBlock）：挂在墙上，把鱼桶里的生物"钉"在牌子上展示；空手右键取下。
public class FishPlaqueBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // 贴墙的一块薄板（按朝向区分）
    private static final VoxelShape SHAPE_NORTH = VoxelShapes.cuboid(0.0, 0.1875, 0.9375, 1.0, 0.8125, 1.0);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.cuboid(0.0, 0.1875, 0.0, 1.0, 0.8125, 0.0625);
    private static final VoxelShape SHAPE_WEST = VoxelShapes.cuboid(0.9375, 0.1875, 0.0, 1.0, 0.8125, 1.0);
    private static final VoxelShape SHAPE_EAST = VoxelShapes.cuboid(0.0, 0.1875, 0.0, 0.0625, 0.8125, 1.0);

    public FishPlaqueBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            case SOUTH:
                return SHAPE_SOUTH;
            case WEST:
                return SHAPE_WEST;
            case EAST:
                return SHAPE_EAST;
            default:
                return SHAPE_NORTH;
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FishPlaqueBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(net.minecraft.item.ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        return world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, facing);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                net.minecraft.world.WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient
                && world.getBlockEntity(pos) instanceof FishPlaqueBlockEntity plaque && plaque.hasFish()) {
            ItemStack bucket = plaque.getSourceBucket().isEmpty()
                    ? new ItemStack(net.minecraft.item.Items.WATER_BUCKET) : plaque.getSourceBucket().copy();
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bucket));
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof FishPlaqueBlockEntity plaque)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);

        // 拿着鱼桶：挂上去
        if (stack.getItem() instanceof EntityBucketItem bucketItem) {
            EntityType<?> type = getEntityTypeFromBucket(bucketItem);
            if (type == null || plaque.hasFish()) {
                return ActionResult.PASS;
            }
            NbtCompound data = stack.getSubNbt("BucketVariantTag");
            plaque.setFishData(type, data == null ? new NbtCompound() : data.copy(), stack.copyWithCount(1));
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        // 空手：取回鱼桶
        if (stack.isEmpty() && plaque.hasFish()) {
            ItemStack bucket = plaque.getSourceBucket().isEmpty()
                    ? new ItemStack(net.minecraft.item.Items.WATER_BUCKET) : plaque.getSourceBucket().copy();
            if (!player.getInventory().insertStack(bucket)) {
                player.dropItem(bucket, false);
            }
            plaque.clearFish();
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    // 1.20.1 的 EntityBucketItem 没有公开 getter，这里通过 accesswidener 直接读字段。
    // 注意：不能用反射 + Yarn 字段名 —— 正式环境里字段名是混淆后的 field_xxxxx，反射必然失败。
    @Nullable
    private static EntityType<?> getEntityTypeFromBucket(EntityBucketItem bucket) {
        return bucket.entityType;
    }
}
