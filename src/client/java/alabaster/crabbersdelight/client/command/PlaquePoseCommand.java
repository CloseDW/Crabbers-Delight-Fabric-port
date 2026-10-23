package alabaster.crabbersdelight.client.command;

import alabaster.crabbersdelight.client.renderer.FishPlaqueRenderer;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;


// 客户端调试命令：实时调整鱼牌上生物的摆放姿态（只影响本地渲染，不用重开游戏）
//   /cdplaque set <实体id> <yaw> <roll> <pitch> <upDown> <leftRight> <frontBack>
//   /cdplaque list
//   /cdplaque reset
public final class PlaquePoseCommand {
    private PlaquePoseCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("cdplaque")
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("entity", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("yaw", FloatArgumentType.floatArg())
                                                .then(ClientCommandManager.argument("roll", FloatArgumentType.floatArg())
                                                        .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg())
                                                                .then(ClientCommandManager.argument("upDown", FloatArgumentType.floatArg())
                                                                        .then(ClientCommandManager.argument("leftRight", FloatArgumentType.floatArg())
                                                                                .then(ClientCommandManager.argument("frontBack", FloatArgumentType.floatArg())
                                                                                        .executes(context -> {
                                                                                            String entity = StringArgumentType.getString(context, "entity");
                                                                                            FishPlaqueRenderer.TUNED_OVERRIDES.put(entity,
                                                                                                    new FishPlaqueRenderer.MobOverride(
                                                                                                            FloatArgumentType.getFloat(context, "yaw"),
                                                                                                            FloatArgumentType.getFloat(context, "roll"),
                                                                                                            FloatArgumentType.getFloat(context, "pitch"),
                                                                                                            FloatArgumentType.getFloat(context, "upDown"),
                                                                                                            FloatArgumentType.getFloat(context, "leftRight"),
                                                                                                            FloatArgumentType.getFloat(context, "frontBack")));
                                                                                            context.getSource().sendFeedback(Text.literal(
                                                                                                    "[CD] 已设置 " + entity + " -> " + describe(entity)));
                                                                                            return 1;
                                                                                        })))))))))
                        .then(ClientCommandManager.literal("list").executes(context -> {
                            if (FishPlaqueRenderer.TUNED_OVERRIDES.isEmpty()) {
                                context.getSource().sendFeedback(Text.literal("[CD] 没有临时调整项"));
                            }
                            for (String key : FishPlaqueRenderer.TUNED_OVERRIDES.keySet()) {
                                context.getSource().sendFeedback(Text.literal("[CD] " + key + " -> " + describe(key)));
                            }
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("reset").executes(context -> {
                            FishPlaqueRenderer.TUNED_OVERRIDES.clear();
                            context.getSource().sendFeedback(Text.literal("[CD] 已恢复默认摆放"));
                            return 1;
                        }))));
    }

    private static String describe(String entity) {
        FishPlaqueRenderer.MobOverride override = FishPlaqueRenderer.TUNED_OVERRIDES.get(entity);
        if (override == null) {
            return "(默认)";
        }
        return String.format("yaw=%.3f roll=%.3f pitch=%.3f up=%.3f lr=%.3f fb=%.3f",
                override.yaw(), override.roll(), override.pitch(),
                override.upDown(), override.leftRight(), override.frontBack());
    }
}
