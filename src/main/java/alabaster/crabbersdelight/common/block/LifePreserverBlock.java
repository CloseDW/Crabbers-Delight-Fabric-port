package alabaster.crabbersdelight.common.block;

import alabaster.crabbersdelight.common.entity.LifePreserverSeat;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 救生圈（对应 1.21 版 LifePreserverBlock）：
// 可贴地板/墙/天花板放置；地板状态右键可坐（生成隐形座位实体），潜行右键可直接穿到腿上。
public class LifePreserverBlock extends Block {
    public static final double WAIST_HEIGHT = 0.7;
    // 注意：1.20.1 里"贴墙方位"是 WallMountLocation（FLOOR/WALL/CEILING），属性名 face，
    // 与 1.21 的 AttachFace 语义一致；不能用 Properties.ATTACHMENT（那是 4 值的 attachment 属性）。
    public static final EnumProperty<WallMountLocation> FACE = Properties.WALL_MOUNT_LOCATION;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape FLOOR_SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.25, 0.25),
            VoxelShapes.cuboid(0.0, 0.0, 0.25, 0.25, 0.25, 0.75),
            VoxelShapes.cuboid(0.75, 0.0, 0.25, 1.0, 0.25, 0.75),
            VoxelShapes.cuboid(0.0, 0.0, 0.75, 1.0, 0.25, 1.0)
    );

    private static final VoxelShape WALL_SHAPE_SOUTH = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.25, 0.25),
            VoxelShapes.cuboid(0.0, 0.25, 0.0, 0.25, 0.75, 0.25),
            VoxelShapes.cuboid(0.75, 0.25, 0.0, 1.0, 0.75, 0.25),
            VoxelShapes.cuboid(0.0, 0.75, 0.0, 1.0, 1.0, 0.25)
    );

    private static final VoxelShape MIDDLE_SHAPE_SOUTH = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.375, 1.0, 0.25, 0.625),
            VoxelShapes.cuboid(0.0, 0.25, 0.375, 0.25, 0.75, 0.625),
            VoxelShapes.cuboid(0.75, 0.25, 0.375, 1.0, 0.75, 0.625),
            VoxelShapes.cuboid(0.0, 0.75, 0.375, 1.0, 1.0, 0.625)
    );

    private static final VoxelShape[] WALL_SHAPES_BY_HORIZONTAL_INDEX = buildRotatedShapes(WALL_SHAPE_SOUTH);
    private static final VoxelShape[] MIDDLE_SHAPES_BY_HORIZONTAL_INDEX = buildRotatedShapes(MIDDLE_SHAPE_SOUTH);

    private static VoxelShape[] buildRotatedShapes(VoxelShape southDefault) {
        VoxelShape[] shapes = new VoxelShape[4];
        VoxelShape current = southDefault;
        for (int i = 0; i < 4; i++) {
            shapes[i] = current;
            current = rotateClockwise90(current);
        }
        return shapes;
    }

    private static VoxelShape rotateClockwise90(VoxelShape shape) {
        VoxelShape result = VoxelShapes.empty();
        for (Box box : shape.getBoundingBoxes()) {
            double newMinX = 1 - box.maxZ;
            double newMaxX = 1 - box.minZ;
            double newMinZ = box.minX;
            double newMaxZ = box.maxX;
            result = VoxelShapes.union(result,
                    VoxelShapes.cuboid(newMinX, box.minY, newMinZ, newMaxX, box.maxY, newMaxZ));
        }
        return result;
    }

    private static VoxelShape shapeFor(VoxelShape[] shapesByIndex, Direction facing) {
        switch (facing) {
            case SOUTH:
                return shapesByIndex[0];
            case WEST:
                return shapesByIndex[1];
            case NORTH:
                return shapesByIndex[2];
            case EAST:
                return shapesByIndex[3];
            default:
                return shapesByIndex[0];
        }
    }

    public LifePreserverBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACE, WallMountLocation.FLOOR)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction clicked = context.getSide();
        boolean waterlogged = context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER;
        BlockState state = this.getDefaultState().with(WATERLOGGED, waterlogged);
        if (clicked.getAxis().isHorizontal()) {
            return state.with(FACE, WallMountLocation.WALL).with(FACING, clicked);
        } else if (clicked == Direction.UP) {
            return state.with(FACE, WallMountLocation.FLOOR);
        } else {
            return state.with(FACE, WallMountLocation.CEILING).with(FACING, context.getHorizontalPlayerFacing());
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACE)) {
            case WALL:
                return shapeFor(WALL_SHAPES_BY_HORIZONTAL_INDEX, state.get(FACING));
            case CEILING:
                return shapeFor(MIDDLE_SHAPES_BY_HORIZONTAL_INDEX, state.get(FACING));
            default:
                return FLOOR_SHAPE;
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            return tryEquip(world, pos, player);
        }
        if (state.get(FACE) != WallMountLocation.FLOOR) {
            return ActionResult.PASS;
        }
        return trySit(world, pos, player);
    }

    private ActionResult tryEquip(World world, BlockPos pos, PlayerEntity player) {
        if (!player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            world.removeBlock(pos, false);
            player.equipStack(EquipmentSlot.LEGS, new ItemStack(this));
            world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult trySit(World world, BlockPos pos, PlayerEntity player) {
        if (player.hasVehicle()) {
            return ActionResult.PASS;
        }
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        LifePreserverSeat seat = getSeat(world, pos);
        if (seat != null && !seat.getPassengerList().isEmpty()) {
            return ActionResult.PASS;
        }
        if (seat == null) {
            seat = new LifePreserverSeat(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            world.spawnEntity(seat);
        }

        player.startRiding(seat);
        return ActionResult.SUCCESS;
    }

    @Nullable
    private LifePreserverSeat getSeat(World world, BlockPos pos) {
        Box box = new Box(pos).expand(0.3, 0.5, 0.3);
        List<LifePreserverSeat> seats = world.getEntitiesByClass(LifePreserverSeat.class, box, seat -> true);
        return seats.isEmpty() ? null : seats.get(0);
    }

    private void clearSeat(World world, BlockPos pos) {
        LifePreserverSeat seat = getSeat(world, pos);
        if (seat != null) {
            for (Entity passenger : seat.getPassengerList()) {
                passenger.stopRiding();
            }
            seat.discard();
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            clearSeat(world, pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
