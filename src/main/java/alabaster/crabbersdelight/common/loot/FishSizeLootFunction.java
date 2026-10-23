package alabaster.crabbersdelight.common.loot;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.registry.CDModLootFunctions;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonSerializer;

// 钓鱼战利品函数（对应 1.21 版 FishSizeEvents.onItemFished）：
// 1.20.1 没有 ItemFishedEvent，改为给 minecraft:gameplay/fishing 池挂一个战利品函数，
// 鱼一出表就按幸运值抽尺寸写进 NBT。
public class FishSizeLootFunction implements LootFunction {
    public static final FishSizeLootFunction INSTANCE = new FishSizeLootFunction();

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (!Config.FISH_SIZE_ENABLED.get()) {
            return stack;
        }
        FishSizeHelper.rollAndApply(stack, context.getLuck(), context.getRandom());
        return stack;
    }

    @Override
    public LootFunctionType getType() {
        return CDModLootFunctions.FISH_SIZE;
    }

    public static class Serializer implements JsonSerializer<FishSizeLootFunction> {
        @Override
        public void toJson(JsonObject json, FishSizeLootFunction function, JsonSerializationContext context) {
            // 只由代码挂载，不需要 JSON 表达
        }

        @Override
        public FishSizeLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
            return new FishSizeLootFunction();
        }
    }
}
