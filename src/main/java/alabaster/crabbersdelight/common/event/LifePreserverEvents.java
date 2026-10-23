package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.block.LifePreserverBlock;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

// 救生圈的浮力（对应 1.21 版的 LifePreserverEvents）：
// 1.21 用 NeoForge 的全局实体 tick 事件；1.20.1 没有等价事件、且装备中的物品也不会走 inventoryTick，
// 所以改为服务端每 tick 扫一遍世界里的生物 + 客户端给本地玩家补一份（保证手感即时）。
public class LifePreserverEvents {
    private static final int MAX_SURFACE_SEARCH = 8;
    private static final double DEADZONE = 0.05;
    private static final double LIFT_FACTOR = 0.15;
    private static final double MAX_LIFT = 0.08;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                for (Entity entity : world.iterateEntities()) {
                    if (entity instanceof LivingEntity living) {
                        applyBuoyancy(living);
                    }
                }
            }
        });
    }

    public static void applyBuoyancy(LivingEntity entity) {
        if (!entity.getEquippedStack(EquipmentSlot.LEGS).isOf(CDModItems.LIFE_PRESERVER)) {
            return;
        }
        if (!entity.isTouchingWater()) {
            return;
        }

        double waistY = entity.getY() + LifePreserverBlock.WAIST_HEIGHT;
        double surfaceY = findWaterSurfaceY(entity);
        double diff = surfaceY - waistY;

        if (diff > DEADZONE) {
            double lift = Math.min(diff * LIFT_FACTOR, MAX_LIFT);
            Vec3d motion = entity.getVelocity();
            entity.setVelocity(motion.x, motion.y + lift, motion.z);
        }
    }

    private static double findWaterSurfaceY(LivingEntity entity) {
        World world = entity.getWorld();
        BlockPos.Mutable cursor = entity.getBlockPos().mutableCopy();
        double lastWaterTop = cursor.getY();

        for (int i = 0; i < MAX_SURFACE_SEARCH; i++) {
            FluidState fluid = world.getFluidState(cursor);
            if (!fluid.isIn(FluidTags.WATER)) {
                break;
            }
            lastWaterTop = cursor.getY() + fluid.getHeight(world, cursor);
            cursor.move(Direction.UP);
        }
        return lastWaterTop;
    }
}
