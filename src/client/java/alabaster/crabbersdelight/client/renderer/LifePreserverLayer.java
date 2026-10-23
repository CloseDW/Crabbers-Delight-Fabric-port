package alabaster.crabbersdelight.client.renderer;

import alabaster.crabbersdelight.common.block.LifePreserverBlock;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

// 穿戴救生圈时在腰部渲染物品模型（对应 1.21 版 LifePreserverLayer）
public class LifePreserverLayer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private static final float FLIP_ROTATION_DEGREES = 90f;
    private static final float RENDER_SCALE = 2.0f;
    private static final double RENDER_Y_OFFSET = 2.0 / 16.0;

    public LifePreserverLayer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        ItemStack stack = entity.getEquippedStack(EquipmentSlot.LEGS);
        if (!stack.isOf(CDModItems.LIFE_PRESERVER)) {
            return;
        }

        matrices.push();
        matrices.translate(0.0, LifePreserverBlock.WAIST_HEIGHT - RENDER_Y_OFFSET, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(FLIP_ROTATION_DEGREES));
        matrices.scale(RENDER_SCALE, RENDER_SCALE, RENDER_SCALE);

        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, entity.getWorld(), entity, 0);
        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers,
                light, OverlayTexture.DEFAULT_UV, model);

        matrices.pop();
    }
}
