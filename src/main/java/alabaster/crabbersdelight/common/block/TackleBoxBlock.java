package alabaster.crabbersdelight.common.block;

import alabaster.crabbersdelight.common.block.entity.TackleBoxBlockEntity;
import alabaster.crabbersdelight.common.item.TackleBoxItem;
import alabaster.crabbersdelight.common.utils.CDDebug;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

// 钓具箱（对应 1.21 版 TackleBoxBlock）：右键开界面，潜行空手右键整个搬走（内容物一起带走）。
public class TackleBoxBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_NORTH_SOUTH = VoxelShapes.cuboid(0.0, 0.0, 0.25, 1.0, 0.5, 0.75);
    private static final VoxelShape SHAPE_EAST_WEST = VoxelShapes.cuboid(0.25, 0.0, 0.0, 0.75, 0.5, 1.0);

    public TackleBoxBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FACING).getAxis() == Direction.Axis.X ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TackleBoxBlockEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        if (builder.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof TackleBoxBlockEntity tackleBox) {
            return Collections.singletonList(TackleBoxItem.saveTileToItem(tackleBox));
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof TackleBoxBlockEntity tackleBox) {
            return TackleBoxItem.saveTileToItem(tackleBox);
        }
        return super.getPickStack(world, pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof TackleBoxBlockEntity tackleBox)) {
            return ActionResult.SUCCESS;
        }

        ItemStack held = player.getStackInHand(hand);
        // 潜行 + 空手 + 主手：整箱搬走
        if (player.isSneaking() && held.isEmpty() && hand == Hand.MAIN_HAND) {
            ItemStack boxItem = TackleBoxItem.saveTileToItem(tackleBox);
            CDDebug.log("box.pickup pos={} contents={}", pos.toShortString(), boxItem.getNbt());
            world.removeBlock(pos, false);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1F) * 2F);
            if (!player.getInventory().insertStack(boxItem)) {
                player.dropItem(boxItem, false);
            }
            return ActionResult.SUCCESS;
        }

        CDDebug.log("box.open pos={} slots={}", pos.toShortString(), contents(tackleBox));
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(tackleBox);
        }
        return ActionResult.SUCCESS;
    }

    private static String contents(TackleBoxBlockEntity tackleBox) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tackleBox.handler.size(); i++) {
            ItemStack stack = tackleBox.handler.getStack(i);
            if (!stack.isEmpty()) {
                sb.append(i).append('=').append(net.minecraft.registry.Registries.ITEM.getId(stack.getItem())).append(' ');
            }
        }
        return sb.length() == 0 ? "empty" : sb.toString().trim();
    }
}
