package alabaster.crabbersdelight.common.block.entity;

import alabaster.crabbersdelight.common.block.entity.inventory.WormBinItemHandler;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// 蚯蚓箱逻辑（对应 1.21 版 WormBinBlockEntity）：
// 往 0 号槽塞一份可堆肥物，每 1200 tick 消耗一份，产 1~2 条蚯蚓（按该物品在原版堆肥桶里的成肥概率换算）。
public class WormBinBlockEntity extends BlockEntity {
    public static final int DEGRADE_TIME = 1200;
    public static final int MAX_WORMS = 16;
    private static final float PARTICLE_CHANCE = 0.1f;

    private final WormBinItemHandler handler = new WormBinItemHandler() {
        @Override
        public void markDirty() {
            super.markDirty();
            WormBinBlockEntity.this.markDirty();
        }
    };

    private int progress = 0;

    public WormBinBlockEntity(BlockPos pos, BlockState state) {
        super(CDModBlockEntity.WORM_BIN, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, WormBinBlockEntity be) {
        ItemStack fuel = be.handler.getStack(WormBinItemHandler.FUEL_SLOT);
        boolean changed = false;

        if (fuel.isEmpty()) {
            be.progress = 0;
        } else {
            be.progress++;
            if (be.progress >= DEGRADE_TIME) {
                be.progress = 0;

                float chance = getCompostChance(fuel);
                int yield = 1 + Math.round(chance * 2);

                ItemStack currentWorms = be.handler.getStack(WormBinItemHandler.WORM_SLOT);
                int newCount = Math.min(MAX_WORMS, currentWorms.getCount() + yield);
                be.handler.setStack(WormBinItemHandler.WORM_SLOT, new ItemStack(CDModItems.WORM, newCount));

                fuel.decrement(1);
                changed = true;

                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            10, 0.25, 0.15, 0.25, 0.0);
                }
            }
        }

        if (!fuel.isEmpty() && world instanceof ServerWorld serverWorld
                && world.getRandom().nextFloat() < PARTICLE_CHANCE) {
            serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.PODZOL.getDefaultState()),
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    2, 0.2, 0.05, 0.2, 0.0);
        }

        if (changed) {
            be.markDirty();
        }
    }

    public static boolean isValidFuel(ItemStack stack) {
        return getCompostChanceOrNegative(stack) >= 0f;
    }

    private static float getCompostChance(ItemStack stack) {
        float chance = getCompostChanceOrNegative(stack);
        return chance >= 0f ? chance : 0f;
    }

    // 1.20.1 用原版堆肥桶的静态表（1.21 版改成了 NeoForge 的 data map）
    private static float getCompostChanceOrNegative(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1f;
        }
        float chance = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getFloat(stack.getItem());
        return chance > 0f ? chance : -1f;
    }

    public boolean insertFuel(ItemStack stack) {
        if (!isValidFuel(stack) || !handler.getStack(WormBinItemHandler.FUEL_SLOT).isEmpty()) {
            return false;
        }
        handler.setStack(WormBinItemHandler.FUEL_SLOT, stack.copyWithCount(1));
        progress = 0;
        stack.decrement(1);
        markDirty();
        return true;
    }

    public ItemStack ejectFuel() {
        ItemStack fuel = handler.getStack(WormBinItemHandler.FUEL_SLOT);
        if (fuel.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack ejected = fuel.copy();
        handler.setStack(WormBinItemHandler.FUEL_SLOT, ItemStack.EMPTY);
        progress = 0;
        markDirty();
        return ejected;
    }

    public ItemStack collectWorms() {
        ItemStack worms = handler.getStack(WormBinItemHandler.WORM_SLOT);
        if (worms.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack collected = worms.copy();
        handler.setStack(WormBinItemHandler.WORM_SLOT, ItemStack.EMPTY);
        markDirty();
        return collected;
    }

    public WormBinItemHandler getHandler() {
        return handler;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("Inventory", this.handler.toNbtList());
        nbt.putInt("Progress", progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Inventory")) {
            this.handler.readNbtList(nbt.getList("Inventory", 10));
        }
        progress = nbt.getInt("Progress");
    }
}
