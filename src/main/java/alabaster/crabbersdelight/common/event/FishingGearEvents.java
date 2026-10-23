package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.tags.CDModTags;
import alabaster.crabbersdelight.common.fishingspot.FishingSpotEntity;
import alabaster.crabbersdelight.common.fishingspot.FishingSpotManager;
import alabaster.crabbersdelight.common.item.LureItem;
import alabaster.crabbersdelight.common.item.fishing.LureEffect;
import alabaster.crabbersdelight.common.utils.FishingGearUtil;
import alabaster.crabbersdelight.common.utils.FishingHookReflection;
import alabaster.crabbersdelight.common.utils.CDDebug;
import alabaster.crabbersdelight.common.utils.TackleBoxProximity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// 钓鱼装备的持续型效果（对应 1.21 版 FishingLureEvents / FishingLineEvents）：
// - 鱼线：钓钩出手时速度 ×1.5（甩得更远）
// - 磁吸饵：幸运 +2；风暴饵：下雨 +3、雷暴 +6
// - 自动饵：上钩自动收线；倒刺饵：延长上钩窗口并把浮标压在水下
public class FishingGearEvents {
    private static final double LINE_DISTANCE_MULTIPLIER = 1.5;
    private static final double MAGNETIC_LUCK_BONUS = 2.0;
    private static final double STORM_LUCK_BONUS_RAIN = 3.0;
    private static final double STORM_LUCK_BONUS_THUNDER = 6.0;
    private static final int BARBED_MIN_NIBBLE_TICKS = 60;
    private static final double BARBED_SUBMERGED_DEPTH = 0.15;

    private static final UUID MAGNETIC_LUCK_ID =
            UUID.nameUUIDFromBytes("crabbersdelight:magnetic_lure_luck".getBytes(StandardCharsets.UTF_8));
    private static final UUID STORM_LUCK_ID =
            UUID.nameUUIDFromBytes("crabbersdelight:storm_lure_luck".getBytes(StandardCharsets.UTF_8));
    private static final UUID SPOT_LUCK_ID =
            UUID.nameUUIDFromBytes("crabbersdelight:fishing_spot_luck".getBytes(StandardCharsets.UTF_8));
    private static final UUID OUTSIDE_SPOT_LUCK_ID =
            UUID.nameUUIDFromBytes("crabbersdelight:outside_fishing_spot_luck_penalty".getBytes(StandardCharsets.UTF_8));

    private static final double SPOT_LUCK_BONUS = 6.0;
    private static final int SPOT_MAX_LURE_TICKS = 60;
    private static final int SPOT_TWINKLE_COUNT = 8;
    private static final double SPOT_TWINKLE_HEIGHT_OFFSET = 0.5;
    private static final double SPOT_TWINKLE_SPREAD = 0.3;

    private static final java.util.Set<Integer> HOOKS_IN_SPOT = new java.util.HashSet<>();

    public static void register() {
        // 鱼线：钩子刚被添加到世界时加速（1.21 用 EntityJoinLevelEvent）
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof FishingBobberEntity hook)) {
                return;
            }
            PlayerEntity player = hook.getPlayerOwner();
            if (player == null) {
                return;
            }
            TackleBoxProximity.TackleBoxAccess box = TackleBoxProximity.find(player);
            if (box == null || box.getSlot(TackleBoxItemHandler.LINE_SLOT).isEmpty()) {
                return;
            }
            Vec3d velocity = hook.getVelocity();
            hook.setVelocity(velocity.multiply(LINE_DISTANCE_MULTIPLIER));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        TackleBoxProximity.TackleBoxAccess box = TackleBoxProximity.find(player);
        LureEffect effect = activeEffect(box);

        if (CDDebug.enabled() && player.age % 20 == 0 && (box != null || player.fishHook != null)) {
            CDDebug.log("gear.tick player={} box={} lure={} line={} bait1={} bait2={} hook={} effect={}",
                    player.getName().getString(),
                    box == null ? "NONE" : box.describe(),
                    box == null ? "-" : slotId(box.getSlot(TackleBoxItemHandler.LURE_SLOT)),
                    box == null ? "-" : slotId(box.getSlot(TackleBoxItemHandler.LINE_SLOT)),
                    box == null ? "-" : slotId(box.getSlot(TackleBoxItemHandler.BAIT_SLOT_1)),
                    box == null ? "-" : slotId(box.getSlot(TackleBoxItemHandler.BAIT_SLOT_2)),
                    player.fishHook != null,
                    effect);
        }

        updateMagneticLuck(player, effect == LureEffect.MAGNETIC);
        updateStormLuck(player, effect == LureEffect.STORM);

        FishingBobberEntity hook = player.fishHook;

        // 钓点：钓点内 +6 幸运、缩短来鱼时间并冒一次星；钓点外钓鱼按配置扣幸运
        FishingSpotEntity spot = hook == null ? null
                : FishingSpotManager.findSpotAt(player.getWorld(), hook.getBlockPos());
        boolean inSpot = spot != null;
        updateSpotLuck(player, hook != null, inSpot);
        if (inSpot && hook != null) {
            FishingHookReflection.capLureTime(hook, SPOT_MAX_LURE_TICKS);
            if (hook.isTouchingWater() && HOOKS_IN_SPOT.add(hook.getId())) {
                spawnSpotTwinkle(player, hook);
            }
        } else if (hook != null) {
            HOOKS_IN_SPOT.remove(hook.getId());
        }

        if (hook == null) {
            return;
        }

        if (effect == LureEffect.AUTOMATIC && FishingHookReflection.isBiting(hook)) {
            tryAutoRetrieve(player);
        }

        if (effect == LureEffect.BARBED && FishingHookReflection.isBiting(hook)) {
            FishingHookReflection.extendNibbleTime(hook, BARBED_MIN_NIBBLE_TICKS);
            keepBobberSubmerged(hook);
        }
    }

    public static LureEffect activeEffect(TackleBoxProximity.TackleBoxAccess box) {
        if (box == null) {
            return null;
        }
        int slot = TackleBoxProximity.findSlot(box, TackleBoxItemHandler.LURE_SLOT,
                s -> s.getItem() instanceof LureItem);
        if (slot < 0) {
            return null;
        }
        return ((LureItem) box.getSlot(slot).getItem()).getEffect();
    }

    public static int activeLineSlot(TackleBoxProximity.TackleBoxAccess box) {
        return TackleBoxProximity.findSlot(box, TackleBoxItemHandler.LINE_SLOT,
                s -> s.isIn(CDModTags.FISHING_LINE));
    }

    private static String slotId(ItemStack stack) {
        return stack.isEmpty() ? "empty" : net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
    }

    private static void tryAutoRetrieve(ServerPlayerEntity player) {
        Hand rodHand = FishingGearUtil.findRodHand(player);
        if (rodHand == null || player.fishHook == null) {
            return;
        }

        FishingBobberEntity hook = player.fishHook;
        ItemStack rod = player.getStackInHand(rodHand);
        int damage = hook.use(rod);
        EquipmentSlot slot = rodHand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        rod.damage(damage, player, p -> p.sendEquipmentBreakStatus(slot));

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F,
                0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static void updateSpotLuck(ServerPlayerEntity player, boolean isFishing, boolean inSpot) {
        EntityAttributeInstance luck = player.getAttributeInstance(EntityAttributes.GENERIC_LUCK);
        if (luck == null) {
            return;
        }

        boolean hasBonus = luck.getModifier(SPOT_LUCK_ID) != null;
        if (inSpot && !hasBonus) {
            luck.addTemporaryModifier(new EntityAttributeModifier(SPOT_LUCK_ID,
                    "crabbersdelight:fishing_spot_luck", SPOT_LUCK_BONUS,
                    EntityAttributeModifier.Operation.ADDITION));
        } else if (!inSpot && hasBonus) {
            luck.removeModifier(SPOT_LUCK_ID);
        }

        boolean penaltyEnabled = Config.OUTSIDE_FISHING_SPOT_LUCK_PENALTY_ENABLED.get();
        boolean hasPenalty = luck.getModifier(OUTSIDE_SPOT_LUCK_ID) != null;
        boolean wantsPenalty = isFishing && !inSpot && penaltyEnabled;
        if (wantsPenalty && !hasPenalty) {
            double amount = -Config.OUTSIDE_FISHING_SPOT_LUCK_PENALTY_AMOUNT.get();
            luck.addTemporaryModifier(new EntityAttributeModifier(OUTSIDE_SPOT_LUCK_ID,
                    "crabbersdelight:outside_fishing_spot_luck_penalty", amount,
                    EntityAttributeModifier.Operation.ADDITION));
        } else if (!wantsPenalty && hasPenalty) {
            luck.removeModifier(OUTSIDE_SPOT_LUCK_ID);
        }
    }

    private static void spawnSpotTwinkle(PlayerEntity player, FishingBobberEntity hook) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        serverWorld.spawnParticles(ParticleTypes.WAX_OFF,
                hook.getX(), hook.getY() + SPOT_TWINKLE_HEIGHT_OFFSET, hook.getZ(),
                SPOT_TWINKLE_COUNT, SPOT_TWINKLE_SPREAD, SPOT_TWINKLE_SPREAD, SPOT_TWINKLE_SPREAD, 0.0);
    }

    private static void updateMagneticLuck(ServerPlayerEntity player, boolean active) {
        EntityAttributeInstance luck = player.getAttributeInstance(EntityAttributes.GENERIC_LUCK);
        if (luck == null) {
            return;
        }
        boolean hasModifier = luck.getModifier(MAGNETIC_LUCK_ID) != null;
        if (active && !hasModifier) {
            luck.addTemporaryModifier(new EntityAttributeModifier(MAGNETIC_LUCK_ID,
                    "crabbersdelight:magnetic_lure_luck", MAGNETIC_LUCK_BONUS,
                    EntityAttributeModifier.Operation.ADDITION));
        } else if (!active && hasModifier) {
            luck.removeModifier(MAGNETIC_LUCK_ID);
        }
    }

    private static void updateStormLuck(ServerPlayerEntity player, boolean hasStorm) {
        EntityAttributeInstance luck = player.getAttributeInstance(EntityAttributes.GENERIC_LUCK);
        if (luck == null) {
            return;
        }

        boolean raining = hasStorm && player.getWorld().hasRain(player.getBlockPos());
        boolean thundering = raining && player.getWorld().isThundering();
        double desiredBonus = thundering ? STORM_LUCK_BONUS_THUNDER : (raining ? STORM_LUCK_BONUS_RAIN : 0);

        EntityAttributeModifier existing = luck.getModifier(STORM_LUCK_ID);
        double currentBonus = existing != null ? existing.getValue() : 0;

        if (desiredBonus != currentBonus) {
            if (existing != null) {
                luck.removeModifier(STORM_LUCK_ID);
            }
            if (desiredBonus > 0) {
                luck.addTemporaryModifier(new EntityAttributeModifier(STORM_LUCK_ID,
                        "crabbersdelight:storm_lure_luck", desiredBonus,
                        EntityAttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static void keepBobberSubmerged(FishingBobberEntity hook) {
        BlockPos pos = hook.getBlockPos();
        FluidState fluid = hook.getWorld().getFluidState(pos);
        if (!fluid.isIn(FluidTags.WATER)) {
            return;
        }
        double surfaceY = pos.getY() + fluid.getHeight(hook.getWorld(), pos);
        double targetY = surfaceY - BARBED_SUBMERGED_DEPTH;
        if (hook.getY() > targetY) {
            hook.setPosition(hook.getX(), targetY, hook.getZ());
            Vec3d motion = hook.getVelocity();
            hook.setVelocity(motion.x, Math.min(motion.y, 0), motion.z);
        }
    }
}
