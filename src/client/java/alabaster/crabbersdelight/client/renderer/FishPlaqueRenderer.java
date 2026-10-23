package alabaster.crabbersdelight.client.renderer;

import alabaster.crabbersdelight.common.block.FishPlaqueBlock;
import alabaster.crabbersdelight.common.block.entity.FishPlaqueBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// 鱼牌渲染（对应 1.21 版 FishPlaqueRenderer）：
// 把桶里的生物实体"钉"在牌子上显示。变换矩阵（位移/旋转/缩放）照搬 1.21 版，
// 常见原版鱼和本模组螃蟹有专门的微调表；水母/河豚那种特殊摆放没有移植（走默认位置）。
public class FishPlaqueRenderer implements BlockEntityRenderer<FishPlaqueBlockEntity> {

    // 调试用：游戏内 /cdplaque set <实体id> <yaw> <roll> <pitch> <upDown> <leftRight> <frontBack>
    // 覆盖下面这张表，改完立刻生效、不用重新编译。
    public record MobOverride(float yaw, float roll, float pitch, float upDown, float leftRight, float frontBack) {
        static final MobOverride NONE = new MobOverride(0f, 0f, 0f, 0f, 0f, 0f);
    }

    public static final Map<String, MobOverride> TUNED_OVERRIDES = new java.util.concurrent.ConcurrentHashMap<>();

    private static final Map<String, MobOverride> MOB_OVERRIDES = Map.ofEntries(
            Map.entry("minecraft:cod", new MobOverride(0f, 0f, 0f, 0.0625f, -0.0625f, -0.25f)),
            Map.entry("minecraft:salmon", new MobOverride(0f, 0f, 0f, 0.0625f, 0f, -0.1875f)),
            Map.entry("minecraft:tropical_fish", new MobOverride(0f, 0f, 0f, 0.0625f, -0.0625f, -0.25f)),
            Map.entry("minecraft:squid", new MobOverride(90f, -90f, 0f, 0.125f, -0.125f, -0.125f)),
            Map.entry("minecraft:glow_squid", new MobOverride(90f, -90f, 0f, 0.125f, -0.125f, -0.125f)),
            Map.entry("minecraft:tadpole", new MobOverride(0f, 180f, 0f, -0.125f, -0.0625f, -0.08f)),
            // 螃蟹（按玩家实测逐步微调）：
            //   yaw  = -90 → 水平面内顺时针 90°（+Y 旋转俯视是逆时针，所以顺时针用负值）
            //   roll =  90 → 垂直面内顺时针 90°
            //   pitch= 180 → 再"从后向前"翻 180°
            //   frontBack = -0.35 → 比原版位置往外挪 0.1 格
            Map.entry("crabbersdelight:crab", new MobOverride(-90f, 90f, 180f, 0.0f, -0.0625f, -0.35f))
    );

    private record CachedFish(Identifier typeId, Entity entity) {
    }

    private final EntityRenderDispatcher entityRenderDispatcher;
    private final Map<Long, CachedFish> cache = new HashMap<>();

    public FishPlaqueRenderer(BlockEntityRendererFactory.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(FishPlaqueBlockEntity plaque, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        long key = plaque.getPos().asLong();
        if (!plaque.hasFish() || plaque.getWorld() == null) {
            cache.remove(key);
            return;
        }

        Entity entity = getOrCreateEntity(plaque, key);
        if (entity == null) {
            return;
        }

        Direction facing = plaque.getCachedState().get(FishPlaqueBlock.FACING);
        String entityKey = String.valueOf(plaque.getEntityTypeId());
        MobOverride override = TUNED_OVERRIDES.getOrDefault(entityKey,
                MOB_OVERRIDES.getOrDefault(entityKey, MobOverride.NONE));

        // 河豚单独处理（照 1.21 版 renderSpecial）：鼓成球、只做 Y 轴旋转，肚皮朝下
        if (entity instanceof net.minecraft.entity.passive.PufferfishEntity puffer) {
            puffer.setPuffState(2);
            float pufferYaw = -facing.asRotation();
            float pufferScale = 0.53125f * 1.5f;

            matrices.push();
            matrices.translate(0.5, 0.0, 0.5);
            matrices.translate(facing.getOffsetX() * -0.35f, 0.175, facing.getOffsetZ() * -0.35f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(pufferYaw));
            matrices.translate(0, -0.02f, 0);
            matrices.scale(pufferScale, pufferScale, pufferScale);

            puffer.setHeadYaw(0);
            puffer.setBodyYaw(0);
            this.entityRenderDispatcher.render(puffer, 0.0, 0.0, 0.0, 0.0f, 0.0f, matrices, vertexConsumers, light);
            matrices.pop();
            return;
        }

        float scale = 0.53125f;
        float maxDim = Math.max(entity.getWidth(), entity.getHeight());
        if (maxDim > 1.0f) {
            scale /= maxDim;
        }

        float stepMultiplier = 0.25f;
        Vec3d offset = new Vec3d(
                facing.getOffsetX() * stepMultiplier,
                -scale,
                facing.getOffsetZ() * stepMultiplier
        );

        float yDegrees = -facing.asRotation() + 90f + 180f + override.yaw();
        Direction right = facing.rotateYClockwise();

        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.translate(-offset.x, -offset.y, -offset.z);

        if (override.upDown() != 0f) {
            matrices.translate(0, override.upDown(), 0);
        }
        if (override.leftRight() != 0f) {
            matrices.translate(right.getOffsetX() * override.leftRight(), 0, right.getOffsetZ() * override.leftRight());
        }
        if (override.frontBack() != 0f) {
            matrices.translate(facing.getOffsetX() * override.frontBack(), 0, facing.getOffsetZ() * override.frontBack());
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yDegrees));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90f + override.roll()));
        if (override.pitch() != 0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(override.pitch()));
        }
        matrices.translate(-0.125f, 0, 0.125f);
        matrices.translate(0, -0.06f, 0);
        matrices.translate(0, -0.10f, 0);
        matrices.scale(scale * 1.5f, scale * 1.5f, scale * 1.5f);

        entity.setHeadYaw(0);
        entity.setBodyYaw(0);

        // 注意：这里传 0f（不是 tickDelta）。实体没有 tick，如果传真实 tickDelta，
        // 每 tick 内动画进度会在 0→1 之间跳变、到下一 tick 又归零，看起来就是"抽搐"。
        // 1.21 版同样是传 0f 把动画冻结住。
        this.entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 0.0f, matrices, vertexConsumers, light);
        matrices.pop();
    }

    @Nullable
    private Entity getOrCreateEntity(FishPlaqueBlockEntity plaque, long key) {
        EntityType<?> type = plaque.getEntityType();
        if (type == null) {
            cache.remove(key);
            return null;
        }

        Identifier typeId = plaque.getEntityTypeId();
        CachedFish cached = cache.get(key);
        if (cached != null && cached.typeId().equals(typeId)) {
            return cached.entity();
        }

        Entity entity = type.create(plaque.getWorld());
        if (entity == null) {
            return null;
        }
        NbtCompound data = plaque.getEntityData();
        if (!data.isEmpty()) {
            try {
                entity.readNbt(data);
            } catch (Exception ignored) {
            }
        }
        cache.put(key, new CachedFish(typeId, entity));
        return entity;
    }
}
