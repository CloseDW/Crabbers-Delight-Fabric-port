package alabaster.crabbersdelight.client.mixin;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.item.LureItem;
import alabaster.crabbersdelight.common.item.TackleBoxItem;
import alabaster.crabbersdelight.common.item.fishing.LureEffect;
import alabaster.crabbersdelight.common.utils.CDDebug;
import alabaster.crabbersdelight.common.utils.TackleBoxProximity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

// 假饵外观（对应 1.21 版 LureAwareFishingHookRenderer）：
// 1.20.1 的 FishingBobberEntityRenderer 把浮标贴图写死成私有静态的 RenderLayer，
// 覆写 getTexture() 不会生效（render 里根本没用它）。所以这里只做两件事：
//   1) render 开头按"钓钩主人装了什么假饵"算出该用的图层；
//   2) 把画浮标四边形的那一次 getBuffer（ordinal 0）换成假饵贴图；
// 钓鱼线的 getBuffer（ordinal 1）保持原样，线的物理与渲染逻辑完全复用原版。
//
// 前提：客户端能读到钓具箱内容物 —— 放在地上的钓具箱由 TackleBoxBlockEntity 主动同步，
// 详见 TackleBoxBlockEntity#toInitialChunkDataNbt / #toUpdatePacket。
// 排查用日志走 CDDebug（config/crabbersdelight-common.toml → [debug] debugLogging）。
@Mixin(FishingBobberEntityRenderer.class)
public abstract class FishingBobberRendererMixin {
    @Unique
    private static final RenderLayer CD_BARBED_LAYER = cutout("fishing_hook_barbed");
    @Unique
    private static final RenderLayer CD_DOUBLE_LAYER = cutout("fishing_hook_double");
    @Unique
    private static final RenderLayer CD_MAGNETIC_LAYER = cutout("fishing_hook_magnetic");
    @Unique
    private static final RenderLayer CD_AUTOMATIC_LAYER = cutout("fishing_hook_automatic");
    @Unique
    private static final RenderLayer CD_STORM_LAYER = cutout("fishing_hook_storm");

    // 每次 render 开头算一次，getBuffer 重定向里直接用；避免每帧多次做 3x3x3 的方块实体搜索。
    @Unique
    private static RenderLayer crabbersdelight$currentLayer;
    @Unique
    private static String crabbersdelight$currentName = "VANILLA";

    // 按 玩家 + 世界时间 缓存，避免逐帧读取钓具箱 NBT。
    @Unique
    private static UUID crabbersdelight$cachedOwner;
    @Unique
    private static long crabbersdelight$cachedTick = Long.MIN_VALUE;
    @Unique
    private static RenderLayer crabbersdelight$cachedLayer;

    @Unique
    private static boolean crabbersdelight$loggedFirstRender;
    @Unique
    private static boolean crabbersdelight$loggedFirstBuffer;
    @Unique
    private static String crabbersdelight$lastLookup;

    @Unique
    private static RenderLayer cutout(String name) {
        return RenderLayer.getEntityCutout(
                new Identifier(CrabbersDelightFabric.MOD_ID, "textures/entity/" + name + ".png"));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void crabbersdelight$resolveLureLayer(FishingBobberEntity entity, float yaw, float tickDelta,
                                                  MatrixStack matrices, VertexConsumerProvider buffers, int light,
                                                  CallbackInfo ci) {
        crabbersdelight$currentLayer = crabbersdelight$resolve(entity);

        if (!crabbersdelight$loggedFirstRender) {
            crabbersdelight$loggedFirstRender = true;
            PlayerEntity owner = entity.getPlayerOwner();
            CDDebug.log("lure.mixin active, first render owner={}",
                    owner == null ? "null" : owner.getName().getString());
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;",
                    ordinal = 0))
    private VertexConsumer crabbersdelight$useLureTexture(VertexConsumerProvider provider, RenderLayer original) {
        RenderLayer resolved = crabbersdelight$currentLayer;

        if (!crabbersdelight$loggedFirstBuffer) {
            crabbersdelight$loggedFirstBuffer = true;
            // lineStripArg 必须为 false：true 说明重定向打到了钓线那次 getBuffer，浮标不会变
            CDDebug.log("lure.buffer first call lineStripArg={} resolved={}",
                    original == RenderLayer.getLineStrip(),
                    resolved == null ? "VANILLA" : crabbersdelight$currentName);
        }

        return provider.getBuffer(resolved == null ? original : resolved);
    }

    @Unique
    private static RenderLayer crabbersdelight$resolve(FishingBobberEntity hook) {
        PlayerEntity owner = hook.getPlayerOwner();
        if (owner == null) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long tick = client.world == null ? Long.MIN_VALUE : client.world.getTime();
        if (crabbersdelight$cachedOwner != null
                && crabbersdelight$cachedTick == tick
                && crabbersdelight$cachedOwner.equals(owner.getUuid())) {
            return crabbersdelight$cachedLayer;
        }

        RenderLayer layer = crabbersdelight$lookup(owner);
        crabbersdelight$cachedOwner = owner.getUuid();
        crabbersdelight$cachedTick = tick;
        crabbersdelight$cachedLayer = layer;
        return layer;
    }

    @Unique
    private static RenderLayer crabbersdelight$lookup(PlayerEntity player) {
        TackleBoxProximity.TackleBoxAccess box = TackleBoxProximity.find(player);
        if (box == null) {
            crabbersdelight$report(player, "NONE", -1, ItemStack.EMPTY, null);
            return null;
        }
        int slot = TackleBoxProximity.findSlot(box, TackleBoxItemHandler.LURE_SLOT,
                stack -> stack.getItem() instanceof LureItem);
        if (slot < 0) {
            crabbersdelight$report(player, box.describe(), -1, ItemStack.EMPTY, null);
            return null;
        }
        ItemStack lure = box.getSlot(slot);
        LureEffect effect = ((LureItem) lure.getItem()).getEffect();
        crabbersdelight$report(player, box.describe(), slot, lure, effect);

        return switch (effect) {
            case BARBED -> CD_BARBED_LAYER;
            case DOUBLE -> CD_DOUBLE_LAYER;
            case MAGNETIC -> CD_MAGNETIC_LAYER;
            case AUTOMATIC -> CD_AUTOMATIC_LAYER;
            case STORM -> CD_STORM_LAYER;
        };
    }

    // 只在"识别结果发生变化"时打一条，避免每 tick 刷屏
    @Unique
    private static void crabbersdelight$report(PlayerEntity player, String box, int slot, ItemStack lure,
                                               LureEffect effect) {
        String lureId = lure.isEmpty() ? "-" : Registries.ITEM.getId(lure.getItem()).toString();
        String state = box + "|slot=" + slot + "|lure=" + lureId + "|effect=" + (effect == null ? "-" : effect.name());
        if (state.equals(crabbersdelight$lastLookup)) {
            return;
        }
        crabbersdelight$lastLookup = state;

        if (effect == null) {
            // 放在地上的钓具箱如果没同步到客户端，这里会看到 box=placed(...) 但槽位是空的
            CDDebug.log("lure.lookup player={} box={} slot={} -> no lure; main={} off={} boxInInventory={}",
                    player.getName().getString(), box, slot,
                    idOf(player.getMainHandStack()), idOf(player.getOffHandStack()),
                    findBoxInInventory(player));
            return;
        }
        crabbersdelight$currentName = effect.name();
        CDDebug.log("lure.lookup player={} box={} slot={} lure={} -> {}",
                player.getName().getString(), box, slot, lureId, effect.name());
    }

    @Unique
    private static String findBoxInInventory(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).getItem() instanceof TackleBoxItem) {
                return (i < 9 ? "hotbar" : "backpack") + "[" + i + "]";
            }
        }
        return "none";
    }

    @Unique
    private static String idOf(ItemStack stack) {
        return stack.isEmpty() ? "empty" : Registries.ITEM.getId(stack.getItem()).toString();
    }
}
