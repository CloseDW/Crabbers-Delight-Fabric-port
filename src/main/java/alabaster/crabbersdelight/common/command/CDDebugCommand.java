package alabaster.crabbersdelight.common.command;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.fishingspot.FishingSpotEntity;
import alabaster.crabbersdelight.common.fishingspot.FishingSpotManager;
import alabaster.crabbersdelight.common.utils.FishingHookReflection;
import alabaster.crabbersdelight.common.utils.TackleBoxProximity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

// 自检命令（/cddebug gear、/cddebug spot），只在调试时用：
//   gear：打印当前能被识别到的钓具箱、四个槽位、钓钩状态与所在钓点
//   spot：立刻在脚下水面尝试生成一个钓点，并把评分过程打出来
public final class CDDebugCommand {
    private CDDebugCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("cddebug")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("gear").executes(context -> gear(context.getSource())))
                        .then(CommandManager.literal("spot").executes(context -> spot(context.getSource())))
                        .then(CommandManager.literal("tp").executes(context -> tp(context.getSource())))
                        .then(CommandManager.literal("fish")
                                .then(CommandManager.argument("size", com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                java.util.Arrays.asList("tiny", "small", "regular", "large", "huge"), builder))
                                        .executes(context -> fish(context.getSource(),
                                                com.mojang.brigadier.arguments.StringArgumentType.getString(context, "size"), 0))
                                        .then(CommandManager.argument("count", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 64))
                                                .executes(context -> fish(context.getSource(),
                                                        com.mojang.brigadier.arguments.StringArgumentType.getString(context, "size"),
                                                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "count"))))))
                        .then(CommandManager.literal("config").executes(context -> config(context.getSource())))));
    }

    private static int gear(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }

        TackleBoxProximity.TackleBoxAccess box = TackleBoxProximity.find(player);
        source.sendFeedback(() -> Text.literal("[CD] 钓具箱: " + (box == null ? "未找到（快捷栏/副手/主手/3 格内都没有）" : box.describe())), false);

        if (box != null) {
            source.sendFeedback(() -> Text.literal("[CD] 假饵槽: " + name(box.getSlot(TackleBoxItemHandler.LURE_SLOT))), false);
            source.sendFeedback(() -> Text.literal("[CD] 鱼线槽: " + name(box.getSlot(TackleBoxItemHandler.LINE_SLOT))), false);
            source.sendFeedback(() -> Text.literal("[CD] 鱼饵1槽: " + name(box.getSlot(TackleBoxItemHandler.BAIT_SLOT_1))), false);
            source.sendFeedback(() -> Text.literal("[CD] 鱼饵2槽: " + name(box.getSlot(TackleBoxItemHandler.BAIT_SLOT_2))), false);
        }

        FishingBobberEntity hook = player.fishHook;
        source.sendFeedback(() -> Text.literal("[CD] 钓钩: " + (hook == null ? "无" :
                "有 pos=" + hook.getBlockPos().toShortString() + " inWater=" + hook.isTouchingWater()
                        + " biting=" + FishingHookReflection.isBiting(hook))), false);

        if (hook != null) {
            FishingSpotEntity spot = FishingSpotManager.findSpotAt(player.getWorld(), hook.getBlockPos());
            source.sendFeedback(() -> Text.literal("[CD] 浮标所在钓点: " + (spot == null ? "无（圈外钓鱼，幸运惩罚生效）"
                    : "有 radius=" + spot.getRadius() + " disturbed=" + spot.isDisturbed())), false);
        }
        return 1;
    }

    private static int spot(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null || !(player.getWorld() instanceof net.minecraft.server.world.ServerWorld world)) {
            return 0;
        }

        FishingSpotEntity spot = FishingSpotManager.findSpotAt(world, player.getBlockPos());
        if (spot != null) {
            source.sendFeedback(() -> Text.literal("[CD] 你已经在钓点内：radius=" + spot.getRadius()), false);
            return 1;
        }

        FishingSpotEntity created = FishingSpotManager.forceSpawnNear(world, player);
        if (created == null) {
            String reason = FishingSpotManager.getLastFailure();
            source.sendFeedback(() -> Text.literal("[CD] 生成失败：" + (reason == null ? "未知原因" : reason)), false);
            source.sendFeedback(() -> Text.literal("[CD] 提示：钓点要整片水面（5 格半径内无岸）+ 深度≥2 或有海草/海带；"
                    + "也可以站在开阔海面再试一次"), false);
        } else {
            BlockPos pos = created.getBlockPos();
            source.sendFeedback(() -> Text.literal("[CD] 已在 " + pos.toShortString()
                    + " 生成钓点 radius=" + created.getRadius() + " lifetime=" + created.getLifetimeTicks()
                    + "（边缘每 20 tick 会打一圈水花）"), false);
        }
        return 1;
    }

    private static int tp(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null || !(player.getWorld() instanceof ServerWorld world)) {
            return 0;
        }

        java.util.List<FishingSpotEntity> spots = world.getEntitiesByClass(FishingSpotEntity.class,
                new net.minecraft.util.math.Box(player.getBlockPos()).expand(96), spot -> true);
        if (spots.isEmpty()) {
            source.sendFeedback(() -> Text.literal("[CD] 96 格内没有钓点，先用 /cddebug spot 生成一个"), false);
            return 0;
        }

        FishingSpotEntity nearest = spots.get(0);
        double best = Double.MAX_VALUE;
        for (FishingSpotEntity spot : spots) {
            double distance = spot.squaredDistanceTo(player);
            if (distance < best) {
                best = distance;
                nearest = spot;
            }
        }

        FishingSpotEntity target = nearest;
        player.requestTeleport(target.getX(), target.getY() + 2.5, target.getZ());
        source.sendFeedback(() -> Text.literal("[CD] 已传送到钓点 " + target.getBlockPos().toShortString()
                + " radius=" + target.getRadius() + "，低头看水面就是水花圈"), false);
        return 1;
    }

    private static int fish(ServerCommandSource source, String sizeName, int count) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return 0;
        }

        alabaster.crabbersdelight.common.item.FishSize size =
                alabaster.crabbersdelight.common.item.FishSize.byName(sizeName);
        if (size == null) {
            source.sendError(Text.literal("[CD] 未知尺寸: " + sizeName + "（tiny / small / regular / large / huge）"));
            return 0;
        }

        if (count > 0) {
            ItemStack stack = new ItemStack(net.minecraft.item.Items.COD, count);
            alabaster.crabbersdelight.common.utils.FishSizeHelper.set(stack, size);
            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false);
            }
            source.sendFeedback(() -> Text.literal("[CD] 给了 " + count + " 条 " + sizeName
                    + " 鳕鱼（倍率 ×" + size.getMultiplier() + "）"), false);
            return 1;
        }

        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            source.sendError(Text.literal("[CD] 主手是空的：把鱼拿在手上，或用 /cddebug fish <尺寸> <数量> 直接给"));
            return 0;
        }
        alabaster.crabbersdelight.common.utils.FishSizeHelper.set(held, size);
        source.sendFeedback(() -> Text.literal("[CD] 主手物品已设为 " + sizeName
                + "（倍率 ×" + size.getMultiplier() + "），鱼名会带颜色和尺寸标签"), false);
        return 1;
    }
    private static int config(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("[CD] debug=" + safeDebug()
                + " fishSize=" + Config.FISH_SIZE_ENABLED.get()
                + " spotInterval=" + Config.FISHING_SPOT_SPAWN_INTERVAL.get()
                + " spotMaxPerArea=" + Config.FISHING_SPOT_MAX_PER_AREA.get()
                + " outsidePenalty=" + Config.OUTSIDE_FISHING_SPOT_LUCK_PENALTY_ENABLED.get()
                + "(" + Config.OUTSIDE_FISHING_SPOT_LUCK_PENALTY_AMOUNT.get() + ")"), false);
        return 1;
    }

    private static boolean safeDebug() {
        try {
            return Config.DEBUG_LOGGING.get();
        } catch (Exception e) {
            return false;
        }
    }

    private static String name(ItemStack stack) {
        return stack.isEmpty() ? "空" : stack.getItem().toString() + " x" + stack.getCount();
    }
}
