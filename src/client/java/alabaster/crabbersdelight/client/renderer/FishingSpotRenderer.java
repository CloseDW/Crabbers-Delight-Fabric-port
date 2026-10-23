package alabaster.crabbersdelight.client.renderer;

import alabaster.crabbersdelight.common.fishingspot.FishingSpotEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

// 钓点可视化 —— 与 1.21 版 FishingSpotRenderer 的算法、配色、形态一致：
//   12 条黑色像素小鱼在水下绕圈游动 + 3 圈带随机镂空的白色半透明水波向外扩散，
//   受惊时鱼群向外窜并整体淡出。
// 1.20.1 侧用自定义 RenderLayer（POSITION_COLOR 三角面 + 半透明 + 关剔除 + 视角偏移层），
// 对应 1.21 的 RenderType.create(... POSITION_COLOR_SHADER ... TRANSLUCENT_TRANSPARENCY ...)。
public class FishingSpotRenderer extends EntityRenderer<FishingSpotEntity> {
    private static final float CELL_SIZE = 0.05f;
    private static final int CENTER_LIFETIME_TICKS = 90;
    private static final int CENTER_COUNT = 3;
    private static final float CENTER_SPREAD_FRACTION = 0.55f;
    private static final float CENTER_MAX_RADIUS_FRACTION = 0.85f;
    private static final float MAX_THICKNESS = 0.15f;
    private static final float BAND_ALPHA = 0.7f;

    private static final int FISH_COUNT = 12;
    private static final int FISH_CYCLE_TICKS = 240;
    private static final int FISH_VISIBLE_TICKS = 140;
    private static final int FISH_FADE_TICKS = 20;

    // 5x7 的像素小鱼（true = 染色格）
    private static final boolean[][] FISH_SHAPE = {
            {false, false, true, false, false},
            {false, true, true, true, false},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {false, false, true, false, false},
            {true, true, false, true, true},
            {true, false, false, false, true},
    };
    private static final int FISH_COLS = FISH_SHAPE.length;
    private static final int FISH_ROWS = FISH_SHAPE[0].length;

    private static final float FISH_ORBIT_SPEED = 0.09f;
    private static final float FISH_SPEED_WOBBLE_AMOUNT = 0.9f;
    private static final float FISH_RADIUS_WOBBLE_AMOUNT = 0.35f;
    private static final float FISH_VERTICAL_RANGE = 0.1f;
    private static final float FISH_MIN_DEPTH = 0.3f;
    private static final float FISH_MAX_DEPTH = 3.0f;
    private static final float FISH_ALPHA = 0.85f;
    private static final float FISH_DART_DISTANCE_MULTIPLIER = 4f;
    private static final int FISH_DART_TICKS = 12;

    // 1.20.1 里 RenderLayer.MultiPhaseParameters 是 protected，外部没法自建 RenderLayer，
    // 所以用原版现成的 DEBUG_QUADS（POSITION_COLOR + QUADS + 半透明 + 关剔除）。
    // 与 1.21 版的区别仅：三角面 → 四边形、少了 VIEW_OFFSET_Z / ITEM_ENTITY_TARGET 两项。
    private static final RenderLayer SPOT_LAYER = RenderLayer.getDebugQuads();

    public FishingSpotRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(FishingSpotEntity entity, float entityYaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, 0.05, 0.0);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(SPOT_LAYER);

        float spotRadius = entity.getRadius();
        float age = entity.getAge() + tickDelta;
        float brightness = lightBrightness(light);
        long entitySeed = entity.getId();

        float ticksRemaining = entity.getLifetimeTicks() - age;
        float ticksSinceDisturbance = entity.isDisturbed()
                ? Math.max(0f, FishingSpotEntity.EXPIRE_FADE_TICKS - ticksRemaining)
                : 0f;
        float shrinkFactor = ticksRemaining < FishingSpotEntity.EXPIRE_FADE_TICKS
                ? smoothstep(Math.max(0f, ticksRemaining) / FishingSpotEntity.EXPIRE_FADE_TICKS)
                : 1f;
        float growFactor = age < FishingSpotEntity.FADE_IN_TICKS
                ? smoothstep(age / FishingSpotEntity.FADE_IN_TICKS)
                : 1f;
        spotRadius *= shrinkFactor * growFactor;

        // ===== 鱼群 =====
        for (int fish = 0; fish < FISH_COUNT; fish++) {
            float fishOffset = (FISH_CYCLE_TICKS / (float) FISH_COUNT) * fish;
            float fishShiftedAge = age + fishOffset;
            float fishCycleAge = fishShiftedAge % FISH_CYCLE_TICKS;
            long fishCycleIteration = (long) (fishShiftedAge / FISH_CYCLE_TICKS);
            long fishSeed = entitySeed * 1000003L + fish * 613L + fishCycleIteration * 2862933555777941757L;

            if (fishCycleAge >= FISH_VISIBLE_TICKS) {
                continue;
            }

            float fishAlpha = FISH_ALPHA;
            if (fishCycleAge < FISH_FADE_TICKS) {
                fishAlpha *= smoothstep(fishCycleAge / FISH_FADE_TICKS);
            } else if (fishCycleAge > FISH_VISIBLE_TICKS - FISH_FADE_TICKS) {
                fishAlpha *= smoothstep((FISH_VISIBLE_TICKS - fishCycleAge) / FISH_FADE_TICKS);
            }
            if (entity.isDisturbed()) {
                fishAlpha *= 1f - smoothstep(ticksSinceDisturbance / FishingSpotEntity.EXPIRE_FADE_TICKS);
            }
            if (fishAlpha <= 0.02f) {
                continue;
            }

            float orbitAngleOffset = randomFloat(fishSeed, 3) * 2f * (float) Math.PI;
            float orbitRadius = spotRadius * (0.25f + randomFloat(fishSeed, 4) * 0.5f);
            float orbitDirection = randomFloat(fishSeed, 5) > 0.5f ? 1f : -1f;
            float speedWobbleFreq = 0.03f + randomFloat(fishSeed, 6) * 0.05f;
            float speedWobblePhase = randomFloat(fishSeed, 7) * 2f * (float) Math.PI;
            float radiusWobbleFreq = 0.02f + randomFloat(fishSeed, 8) * 0.04f;
            float radiusWobblePhase = randomFloat(fishSeed, 9) * 2f * (float) Math.PI;
            float verticalFreq = 0.015f + randomFloat(fishSeed, 10) * 0.025f;
            float verticalPhase = randomFloat(fishSeed, 11) * 2f * (float) Math.PI;
            float baseDepth = -(FISH_MIN_DEPTH + randomFloat(fishSeed, 12) * (FISH_MAX_DEPTH - FISH_MIN_DEPTH));

            float[] here = fishOrbitPosition(fishCycleAge, orbitAngleOffset, orbitRadius, orbitDirection,
                    speedWobbleFreq, speedWobblePhase, radiusWobbleFreq, radiusWobblePhase, verticalFreq, verticalPhase);
            float[] ahead = fishOrbitPosition(fishCycleAge + 1f, orbitAngleOffset, orbitRadius, orbitDirection,
                    speedWobbleFreq, speedWobblePhase, radiusWobbleFreq, radiusWobblePhase, verticalFreq, verticalPhase);

            if (entity.isDisturbed()) {
                float dartHere = 1f + smoothstep(ticksSinceDisturbance / FISH_DART_TICKS) * FISH_DART_DISTANCE_MULTIPLIER;
                float dartAhead = 1f + smoothstep((ticksSinceDisturbance + 1f) / FISH_DART_TICKS) * FISH_DART_DISTANCE_MULTIPLIER;
                here[0] *= dartHere;
                here[2] *= dartHere;
                ahead[0] *= dartAhead;
                ahead[2] *= dartAhead;
            }

            float facingAngle = (float) Math.atan2(ahead[2] - here[2], ahead[0] - here[0]);
            drawFish(consumer, matrix, here[0], baseDepth + here[1], here[2], facingAngle, fishAlpha);
        }

        // ===== 扩散水波 =====
        for (int center = 0; center < CENTER_COUNT; center++) {
            float centerOffset = (CENTER_LIFETIME_TICKS / (float) CENTER_COUNT) * center;
            float shiftedAge = age + centerOffset;
            float centerAge = shiftedAge % CENTER_LIFETIME_TICKS;
            float progress = centerAge / CENTER_LIFETIME_TICKS;
            long cycleIteration = (long) (shiftedAge / CENTER_LIFETIME_TICKS);
            long positionSeed = entitySeed * 1000003L + center * 97L + cycleIteration * 6364136223846793005L;

            float centerAngle = randomFloat(positionSeed, 1) * 2f * (float) Math.PI;
            float centerDist = randomFloat(positionSeed, 2) * spotRadius * CENTER_SPREAD_FRACTION;
            float centerX = (float) Math.cos(centerAngle) * centerDist;
            float centerZ = (float) Math.sin(centerAngle) * centerDist;

            long waveSeed = positionSeed * 2862933555777941757L;
            float waveMaxRadius = spotRadius * CENTER_MAX_RADIUS_FRACTION;

            renderWaveBand(consumer, matrix, centerX, centerZ, waveMaxRadius, progress, brightness, waveSeed);
        }

        matrices.pop();
    }

    private static void renderWaveBand(VertexConsumer consumer, Matrix4f matrix, float centerX, float centerZ,
                                       float maxRadius, float progress, float brightness, long seed) {
        float eased = smoothstep(progress);
        float currentRadius = eased * maxRadius;
        float thickness = eased * MAX_THICKNESS;
        float innerRadius = Math.max(0f, currentRadius - thickness / 2f);
        float outerRadius = currentRadius + thickness / 2f;
        if (outerRadius <= CELL_SIZE) {
            return;
        }

        int gridExtent = (int) Math.ceil(outerRadius / CELL_SIZE) + 1;
        float innerSq = innerRadius * innerRadius;
        float outerSq = outerRadius * outerRadius;

        for (int gx = -gridExtent; gx <= gridExtent; gx++) {
            for (int gz = -gridExtent; gz <= gridExtent; gz++) {
                float cellCenterX = gx * CELL_SIZE;
                float cellCenterZ = gz * CELL_SIZE;
                float distSq = cellCenterX * cellCenterX + cellCenterZ * cellCenterZ;

                if (distSq < innerSq || distSq > outerSq) {
                    continue;
                }
                if (progress > cellDropoutThreshold(gx, gz, seed)) {
                    continue;
                }

                drawSquare(consumer, matrix, centerX + cellCenterX, centerZ + cellCenterZ, brightness);
            }
        }
    }

    private static float cellDropoutThreshold(int gx, int gz, long seed) {
        long combined = gx * 2654435761L + gz * 40503L + seed * 6364136223846793005L;
        int hash = (int) ((combined ^ (combined >>> 33)) & 0x7FFFFFFFL);
        return (hash % 10000) / 10000f;
    }

    private static float randomFloat(long seed, int salt) {
        long combined = seed * 2862933555777941757L + salt * 3935559000370003845L;
        int hash = (int) ((combined ^ (combined >>> 33)) & 0x7FFFFFFFL);
        return (hash % 10000) / 10000f;
    }

    private static float[] fishOrbitPosition(float time, float angleOffset, float baseRadius, float direction,
                                             float speedWobbleFreq, float speedWobblePhase,
                                             float radiusWobbleFreq, float radiusWobblePhase,
                                             float verticalFreq, float verticalPhase) {
        float speedWobble = FISH_SPEED_WOBBLE_AMOUNT * (float) Math.sin(time * speedWobbleFreq + speedWobblePhase);
        float angle = angleOffset + (time * FISH_ORBIT_SPEED + speedWobble) * direction;
        float radiusWobble = 1f + FISH_RADIUS_WOBBLE_AMOUNT * (float) Math.sin(time * radiusWobbleFreq + radiusWobblePhase);
        float radius = baseRadius * radiusWobble;
        float verticalOffset = FISH_VERTICAL_RANGE * (float) Math.sin(time * verticalFreq + verticalPhase);
        return new float[]{(float) Math.cos(angle) * radius, verticalOffset, (float) Math.sin(angle) * radius};
    }

    private static void drawFish(VertexConsumer consumer, Matrix4f matrix, float centerX, float centerY,
                                 float centerZ, float facingAngle, float alpha) {
        float cos = (float) Math.cos(facingAngle);
        float sin = (float) Math.sin(facingAngle);
        float colCenterOffset = (FISH_COLS - 1) / 2f;
        float rowCenterOffset = (FISH_ROWS - 1) / 2f;

        for (int col = 0; col < FISH_COLS; col++) {
            for (int row = 0; row < FISH_ROWS; row++) {
                if (!FISH_SHAPE[col][row]) {
                    continue;
                }
                float localX = (colCenterOffset - col) * CELL_SIZE;
                float localY = (rowCenterOffset - row) * CELL_SIZE;
                float half = CELL_SIZE / 2f;
                float x0 = localX - half;
                float x1 = localX + half;
                float y0 = localY - half;
                float y1 = localY + half;

                fishVertex(consumer, matrix, centerX, centerY, centerZ, cos, sin, x0, y0, alpha);
                fishVertex(consumer, matrix, centerX, centerY, centerZ, cos, sin, x1, y0, alpha);
                fishVertex(consumer, matrix, centerX, centerY, centerZ, cos, sin, x1, y1, alpha);
                fishVertex(consumer, matrix, centerX, centerY, centerZ, cos, sin, x0, y1, alpha);
            }
        }
    }

    private static void fishVertex(VertexConsumer consumer, Matrix4f matrix, float centerX, float centerY,
                                   float centerZ, float cos, float sin, float localX, float localY, float alpha) {
        float rotatedX = localX * cos;
        float rotatedZ = localX * sin;
        consumer.vertex(matrix, centerX + rotatedX, centerY + localY, centerZ + rotatedZ)
                .color(0f, 0f, 0f, alpha)
                .next();
    }

    private static void drawSquare(VertexConsumer consumer, Matrix4f matrix, float centerX, float centerZ,
                                   float brightness) {
        float half = CELL_SIZE / 2f;
        float x0 = centerX - half;
        float x1 = centerX + half;
        float z0 = centerZ - half;
        float z1 = centerZ + half;

        bandVertex(consumer, matrix, x0, 0f, z0, brightness);
        bandVertex(consumer, matrix, x1, 0f, z0, brightness);
        bandVertex(consumer, matrix, x1, 0f, z1, brightness);
        bandVertex(consumer, matrix, x0, 0f, z1, brightness);
    }

    private static void bandVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                                   float brightness) {
        consumer.vertex(matrix, x, y, z)
                .color(brightness, brightness, brightness, BAND_ALPHA)
                .next();
    }

    private static float smoothstep(float x) {
        float clamped = Math.max(0f, Math.min(1f, x));
        return clamped * clamped * (3f - 2f * clamped);
    }

    private static float lightBrightness(int packedLight) {
        int skyLight = (packedLight >> 20) & 0xF;
        int blockLight = (packedLight >> 4) & 0xF;
        return Math.max(0.35f, Math.max(skyLight, blockLight) / 15f);
    }

    @Override
    public Identifier getTexture(FishingSpotEntity entity) {
        return null;
    }

    // 与 1.21 版一致：无视原版默认渲染距离/视锥裁剪。
    // 实际可见距离由实体的同步范围决定（clientTrackingRange(6) ≈ 96 格）。
    @Override
    public boolean shouldRender(FishingSpotEntity entity, net.minecraft.client.render.Frustum frustum,
                                double x, double y, double z) {
        return true;
    }
}
