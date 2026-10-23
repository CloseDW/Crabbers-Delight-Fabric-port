package alabaster.crabbersdelight.common.block;

import alabaster.crabbersdelight.common.block.entity.NoteBlockEntity;
import alabaster.crabbersdelight.common.utils.NoteClientHooks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

// 便条方块（对应 1.21 版 NoteBlock）：可以贴在墙上，也可以平铺在地上，方块实体里存正文；
// 签名后进入 signed=true 状态、不可再编辑。
public class NoteBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty SIGNED = BooleanProperty.of("signed");
    public static final BooleanProperty FLAT = BooleanProperty.of("flat");

    private static final VoxelShape SHAPE_NORTH = VoxelShapes.cuboid(3 / 16.0, 2 / 16.0, 15 / 16.0, 13 / 16.0, 14 / 16.0, 1.0);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.cuboid(3 / 16.0, 2 / 16.0, 0.0, 13 / 16.0, 14 / 16.0, 1 / 16.0);
    private static final VoxelShape SHAPE_WEST = VoxelShapes.cuboid(15 / 16.0, 2 / 16.0, 3 / 16.0, 1.0, 14 / 16.0, 13 / 16.0);
    private static final VoxelShape SHAPE_EAST = VoxelShapes.cuboid(0.0, 2 / 16.0, 3 / 16.0, 1 / 16.0, 14 / 16.0, 13 / 16.0);
    private static final VoxelShape SHAPE_FLAT_NS = VoxelShapes.cuboid(3 / 16.0, 0.0, 2 / 16.0, 13 / 16.0, 1 / 16.0, 14 / 16.0);
    private static final VoxelShape SHAPE_FLAT_EW = VoxelShapes.cuboid(2 / 16.0, 0.0, 3 / 16.0, 14 / 16.0, 1 / 16.0, 13 / 16.0);

    public NoteBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(SIGNED, false)
                .with(FLAT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SIGNED, FLAT);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(FLAT)) {
            Direction facing = state.get(FACING);
            return (facing == Direction.EAST || facing == Direction.WEST) ? SHAPE_FLAT_EW : SHAPE_FLAT_NS;
        }
        return switch (state.get(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NoteBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flat = context.getSide() == Direction.UP;
        return this.getDefaultState()
                .with(FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(FLAT, flat);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(FLAT)) {
            BlockPos below = pos.down();
            return world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP);
        }
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        return world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, facing);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(FLAT) ? direction == Direction.DOWN : direction == state.get(FACING).getOpposite()) {
            if (!state.canPlaceAt(world, pos)) {
                return Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient
                && world.getBlockEntity(pos) instanceof NoteBlockEntity note) {
            ItemStack dropItem = note.toItemStack();
            if (!dropItem.isEmpty()) {
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropItem));
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof NoteBlockEntity note)) {
            return ActionResult.PASS;
        }
        if (world.isClient) {
            if (note.isSigned()) {
                NoteClientHooks.openRead(note.getTitle(), note.getText(), note.getAuthor());
            } else {
                NoteClientHooks.openBlockEdit(pos, note.getText());
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }

    // 供物品放置后写入正文用
    public static void applyText(World world, BlockPos pos, String text) {
        if (world.getBlockEntity(pos) instanceof NoteBlockEntity note) {
            note.setUnsignedText(text);
            world.updateListeners(pos, note.getCachedState(), note.getCachedState(), Block.NOTIFY_ALL);
        }
    }


}
