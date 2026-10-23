package alabaster.crabbersdelight.common.fishingspot;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.registry.CDModEntities;
import alabaster.crabbersdelight.common.utils.CDDebug;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 钓点管理（对应 1.21 版 FishingSpotManager）：定期在玩家附近的水面生成钓点，有船/涉水/生物扰动就提前消失。
public class FishingSpotManager {
    private static final int SPAWN_SEARCH_RADIUS = 32;
    private static final int CANDIDATE_COUNT = 5;
    private static final float POINTS_FOR_CERTAIN_SPAWN = 10f;
    private static final float RAIN_SPAWN_MULTIPLIER = 1.75f;
    private static final int SCORE_SCAN_RADIUS = 4;
    private static final int SHORE_CLEARANCE_RADIUS = 5;
    private static final double SPOT_QUERY_PADDING = 8;
    private static final double DISTURBANCE_SCAN_RADIUS = 48;
    private static final double AREA_DENSITY_RADIUS = 64;

    public static void register() {
        CDDebug.log("fishing_spot manager registered");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                long time = world.getTime();
                if (time % 20 == 0) {
                    checkDisturbance(world);
                    int interval = Math.max(20, Config.FISHING_SPOT_SPAWN_INTERVAL.get());
                    if (time % interval == 0) {
                        for (PlayerEntity player : world.getPlayers()) {
                            trySpawnNearPlayer(world, player);
                        }
                    }
                }
            }
        });
    }

    @Nullable
    public static FishingSpotEntity findSpotAt(World world, BlockPos pos) {
        Box box = new Box(pos).expand(SPOT_QUERY_PADDING);
        List<FishingSpotEntity> nearby = world.getEntitiesByClass(FishingSpotEntity.class, box, spot -> true);
        for (FishingSpotEntity spot : nearby) {
            if (spot.contains(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) {
                return spot;
            }
        }
        return null;
    }

    private static String lastFailure = null;

    public static String getLastFailure() {
        return lastFailure;
    }

    private static void trySpawnNearPlayer(ServerWorld world, PlayerEntity player) {
        forceSpawnNear(world, player, false);
    }

    // 生成一个钓点（/cddebug spot 用 force=true 走螺旋搜索、跳过随机与密度限制）
    // 返回生成出来的实体或 null，失败原因见 getLastFailure()
    @Nullable
    public static FishingSpotEntity forceSpawnNear(ServerWorld world, PlayerEntity player) {
        return forceSpawnNear(world, player, true);
    }

    @Nullable
    public static FishingSpotEntity forceSpawnNear(ServerWorld world, PlayerEntity player, boolean force) {
        lastFailure = null;
        BlockPos playerPos = player.getBlockPos();
        BlockPos bestSurfacePos = null;
        int bestPoints = -1;

        if (force) {
            bestSurfacePos = spiralSearch(world, playerPos);
            if (bestSurfacePos != null) {
                bestPoints = computePoints(world, bestSurfacePos);
            }
        } else {
            for (int i = 0; i < CANDIDATE_COUNT; i++) {
                int x = playerPos.getX() + world.getRandom().nextInt(SPAWN_SEARCH_RADIUS * 2) - SPAWN_SEARCH_RADIUS;
                int z = playerPos.getZ() + world.getRandom().nextInt(SPAWN_SEARCH_RADIUS * 2) - SPAWN_SEARCH_RADIUS;
                BlockPos surfacePos = findWaterSurface(world, x, z);
                if (surfacePos == null) {
                    continue;
                }
                if (findSpotAt(world, surfacePos) != null) {
                    continue;
                }
                if (!hasShoreClearance(world, surfacePos)) {
                    continue;
                }

                int points = computePoints(world, surfacePos);
                if (points > bestPoints) {
                    bestPoints = points;
                    bestSurfacePos = surfacePos;
                }
            }
        }

        if (bestSurfacePos == null || bestPoints <= 0) {
            if (lastFailure == null) {
                lastFailure = "附近找不到合格水面（要整片水域、5 格内无岸，且深度≥2 或有海草/海带）";
            }
            CDDebug.log("spot.attempt player={} pos={} -> {}",
                    player.getName().getString(), playerPos.toShortString(), lastFailure);
            return null;
        }

        if (!force) {
            float spawnChance = Math.min(1f, bestPoints / POINTS_FOR_CERTAIN_SPAWN);
            if (world.hasRain(bestSurfacePos)) {
                spawnChance = Math.min(1f, spawnChance * RAIN_SPAWN_MULTIPLIER);
            }
            if (world.getRandom().nextFloat() >= spawnChance) {
                lastFailure = "随机概率未通过（评分 " + bestPoints + "，成功率 " + String.format("%.0f%%", spawnChance * 100) + "）";
                CDDebug.log("spot.attempt pos={} -> {}", bestSurfacePos.toShortString(), lastFailure);
                return null;
            }

            int nearby = countNearbySpots(world, bestSurfacePos);
            if (nearby >= Config.FISHING_SPOT_MAX_PER_AREA.get()) {
                lastFailure = "64 格内已有 " + nearby + " 个钓点（上限 " + Config.FISHING_SPOT_MAX_PER_AREA.get() + "）";
                CDDebug.log("spot.attempt pos={} -> {}", bestSurfacePos.toShortString(), lastFailure);
                return null;
            }
        }

        int radius = Math.min(2 + bestPoints / 4, 5);
        int lifespanTicks = 2400 + bestPoints * 400;

        FishingSpotEntity spot = CDModEntities.FISHING_SPOT.create(world);
        if (spot == null) {
            lastFailure = "EntityType.create 返回 null（实体注册异常）";
            CDDebug.log("spot.attempt -> {}", lastFailure);
            return null;
        }
        spot.refreshPositionAndAngles(bestSurfacePos.getX() + 0.5, bestSurfacePos.getY() + 1,
                bestSurfacePos.getZ() + 0.5, 0f, 0f);
        spot.setRadius(radius);
        spot.setLifetimeTicks(lifespanTicks);
        world.spawnEntity(spot);
        CDDebug.log("spot.SPAWNED pos={} radius={} lifetime={} points={}",
                bestSurfacePos.toShortString(), radius, lifespanTicks, bestPoints);
        return spot;
    }

    // 从玩家脚下向外画螺旋，找第一个"有水、没钓点、周围空旷、评分>0"的水面
    @Nullable
    private static BlockPos spiralSearch(ServerWorld world, BlockPos center) {
        for (int r = 2; r <= 24; r += 2) {
            for (int a = 0; a < 12; a++) {
                double angle = Math.PI * 2 * a / 12.0;
                int x = center.getX() + (int) Math.round(Math.cos(angle) * r);
                int z = center.getZ() + (int) Math.round(Math.sin(angle) * r);
                BlockPos surfacePos = findWaterSurface(world, x, z);
                if (surfacePos == null) {
                    lastFailure = "半径 " + r + " 格内没有水面";
                    continue;
                }
                if (findSpotAt(world, surfacePos) != null) {
                    lastFailure = "该位置已有钓点";
                    continue;
                }
                if (!hasShoreClearance(world, surfacePos)) {
                    lastFailure = "半径 " + r + " 处水面 5 格内有岸/浅滩";
                    continue;
                }
                if (computePoints(world, surfacePos) <= 0) {
                    lastFailure = "半径 " + r + " 处水太浅且无海草/海带（评分 0）";
                    continue;
                }
                return surfacePos;
            }
        }
        return null;
    }

    private static int countNearbySpots(ServerWorld world, BlockPos pos) {
        Box box = new Box(pos).expand(AREA_DENSITY_RADIUS);
        return world.getEntitiesByClass(FishingSpotEntity.class, box, spot -> true).size();
    }

    @Nullable
    public static BlockPos findWaterSurface(ServerWorld world, int x, int z) {
        int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        for (int y = topY; y > world.getBottomY(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            FluidState fluid = world.getFluidState(pos);
            FluidState fluidAbove = world.getFluidState(pos.up());
            if (fluid.isIn(FluidTags.WATER) && !fluidAbove.isIn(FluidTags.WATER)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean hasShoreClearance(ServerWorld world, BlockPos surfacePos) {
        int radiusSquared = SHORE_CLEARANCE_RADIUS * SHORE_CLEARANCE_RADIUS;
        for (int dx = -SHORE_CLEARANCE_RADIUS; dx <= SHORE_CLEARANCE_RADIUS; dx++) {
            for (int dz = -SHORE_CLEARANCE_RADIUS; dz <= SHORE_CLEARANCE_RADIUS; dz++) {
                if (dx * dx + dz * dz > radiusSquared) {
                    continue;
                }
                if (!world.getFluidState(surfacePos.add(dx, 0, dz)).isIn(FluidTags.WATER)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int computePoints(ServerWorld world, BlockPos surfacePos) {
        int points = 0;

        for (int dx = -SCORE_SCAN_RADIUS; dx <= SCORE_SCAN_RADIUS; dx++) {
            for (int dz = -SCORE_SCAN_RADIUS; dz <= SCORE_SCAN_RADIUS; dz++) {
                Block block = world.getBlockState(surfacePos.add(dx, 0, dz)).getBlock();
                if (block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS
                        || block == Blocks.KELP || block == Blocks.KELP_PLANT) {
                    points += 1;
                }
            }
        }

        int depth = 0;
        BlockPos.Mutable cursor = surfacePos.mutableCopy();
        while (world.getFluidState(cursor).isIn(FluidTags.WATER) && depth < 10) {
            depth++;
            cursor.move(0, -1, 0);
        }
        if (depth >= 4) {
            points += 3;
        } else if (depth >= 2) {
            points += 1;
        }

        return points;
    }

    private static void checkDisturbance(ServerWorld world) {
        for (PlayerEntity player : world.getPlayers()) {
            Box scanBox = new Box(player.getBlockPos()).expand(DISTURBANCE_SCAN_RADIUS);
            List<FishingSpotEntity> spots = world.getEntitiesByClass(FishingSpotEntity.class, scanBox, spot -> true);
            for (FishingSpotEntity spot : spots) {
                Box box = new Box(
                        spot.getX() - spot.getRadius(), spot.getY() - 2, spot.getZ() - spot.getRadius(),
                        spot.getX() + spot.getRadius() + 1, spot.getY() + 2, spot.getZ() + spot.getRadius() + 1
                );
                List<Entity> nearby = world.getEntitiesByClass(Entity.class, box, entity -> true);
                for (Entity entity : nearby) {
                    if (entity instanceof BoatEntity) {
                        spot.disturb();
                        break;
                    }
                    if (entity instanceof PlayerEntity p && p.isTouchingWater()) {
                        spot.disturb();
                        break;
                    }
                    if (entity instanceof LivingEntity living && !(entity instanceof PlayerEntity)
                            && living.isTouchingWater() && !(entity instanceof FishEntity)) {
                        spot.disturb();
                        break;
                    }
                }
            }
        }
    }
}
