package alabaster.crabbersdelight.common.loot;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.event.FishingGearEvents;
import alabaster.crabbersdelight.common.item.fishing.LureEffect;
import alabaster.crabbersdelight.common.registry.CDModLootFunctions;
import alabaster.crabbersdelight.common.tags.CDModTags;
import alabaster.crabbersdelight.common.utils.TackleBoxProximity;
import alabaster.crabbersdelight.common.utils.CDDebug;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;

import java.util.List;

// 上鱼时的装备结算（对应 1.21 版 FishingBaitEvents / FishingLureEvents.onItemFished / FishingLineEvents.onItemFished）：
// 1.20.1 没有 ItemFishedEvent，所以把"钓到东西时"的逻辑做进这个战利品函数。
//   1) 鱼饵：用饵自己的战利品表（gameplay/fishing_bait_loot/<饵 id>）替换渔获，并消耗 1 个
//   2) 双倍饵：数量翻倍
//   3) 鱼线 / 假饵：各掉 1 点耐久
public class FishingCatchLootFunction implements LootFunction {
    public static final FishingCatchLootFunction INSTANCE = new FishingCatchLootFunction();

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        Entity thisEntity = context.get(LootContextParameters.THIS_ENTITY);
        if (!(thisEntity instanceof FishingBobberEntity hook)) {
            CDDebug.log("catch: THIS_ENTITY is not a fishing hook -> {}", thisEntity);
            return stack;
        }
        PlayerEntity player = hook.getPlayerOwner();
        if (player == null) {
            CDDebug.log("catch: hook has no owner");
            return stack;
        }
        TackleBoxProximity.TackleBoxAccess box = TackleBoxProximity.find(player);
        if (box == null) {
            CDDebug.log("catch: player={} box=NONE", player.getName().getString());
            return stack;
        }
        CDDebug.log("catch: player={} box={} in={} bait1={} bait2={} lure={} line={}",
                player.getName().getString(), box.describe(), stack.getItem(),
                box.getSlot(TackleBoxItemHandler.BAIT_SLOT_1), box.getSlot(TackleBoxItemHandler.BAIT_SLOT_2),
                box.getSlot(TackleBoxItemHandler.LURE_SLOT), box.getSlot(TackleBoxItemHandler.LINE_SLOT));

        ItemStack result = stack;

        // 1) 鱼饵替换（专用槽优先，找不到就退回通用格）
        int baitSlot = TackleBoxProximity.findSlot(box, TackleBoxItemHandler.BAIT_SLOT_1,
                s -> s.isIn(CDModTags.FISHING_BAIT));
        if (baitSlot < 0) {
            baitSlot = TackleBoxProximity.findSlot(box, TackleBoxItemHandler.BAIT_SLOT_2,
                    s -> s.isIn(CDModTags.FISHING_BAIT));
        }
        ItemStack bait = TackleBoxProximity.getSlot(box, baitSlot);
        CDDebug.log("catch: baitSlot={} bait={}", baitSlot, bait);
        if (baitSlot >= 0 && !bait.isEmpty() && player.getWorld() instanceof ServerWorld serverWorld) {
            Identifier baitId = Registries.ITEM.getId(bait.getItem());
            Identifier tableId = CrabbersDelightFabric.id("gameplay/fishing_bait_loot/" + baitId.getPath());
            LootDataKey<LootTable> key = new LootDataKey<>(LootDataType.LOOT_TABLES, tableId);
            LootTable table = serverWorld.getServer().getLootManager().getElement(key);

            if (table != null && table != LootTable.EMPTY) {
                LootContextParameterSet params = new LootContextParameterSet.Builder(serverWorld)
                        .add(LootContextParameters.ORIGIN, hook.getPos())
                        .add(LootContextParameters.TOOL, ItemStack.EMPTY)
                        .add(LootContextParameters.THIS_ENTITY, player)
                        .luck(player.getLuck())
                        .build(LootContextTypes.FISHING);
                List<ItemStack> rolled = table.generateLoot(params);
                CDDebug.log("catch: bait table {} -> {} (rolled {} entries)", tableId, rolled, rolled.size());
                if (!rolled.isEmpty()) {
                    result = rolled.get(0);
                }
            } else {
                CDDebug.log("catch: bait table {} missing/empty", tableId);
            }

            ItemStack shrunk = bait.copy();
            shrunk.decrement(1);
            box.setSlot(baitSlot, shrunk);
        }

        // 2) 双倍饵
        if (FishingGearEvents.activeEffect(box) == LureEffect.DOUBLE && !result.isEmpty()) {
            result = result.copy();
            result.setCount(Math.min(result.getMaxCount(), result.getCount() * 2));
            CDDebug.log("catch: DOUBLE lure -> count={}", result.getCount());
        }

        // 3) 耐久消耗
        int lineSlot = FishingGearEvents.activeLineSlot(box);
        if (lineSlot >= 0) {
            damageSlot(box, lineSlot, player);
        }
        int lureSlot = TackleBoxProximity.findSlot(box, TackleBoxItemHandler.LURE_SLOT,
                s -> s.getItem() instanceof alabaster.crabbersdelight.common.item.LureItem);
        if (lureSlot >= 0) {
            damageSlot(box, lureSlot, player);
            grantMasterBaiter(player);
        }

        return result;
    }

    // "打窝仙人"成就：带着假饵钓上来东西时授予（1.21 版用 impossible 触发 + 代码授予）
    private static void grantMasterBaiter(PlayerEntity player) {
        if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)) {
            return;
        }
        var advancement = serverPlayer.getServer() == null ? null
                : serverPlayer.getServer().getAdvancementLoader()
                        .get(new net.minecraft.util.Identifier("crabbersdelight", "main/master_baiter"));
        if (advancement != null) {
            serverPlayer.getAdvancementTracker().grantCriterion(advancement, "bait_catch");
        }
    }
    private static void damageSlot(TackleBoxProximity.TackleBoxAccess box, int slot, PlayerEntity player) {
        ItemStack item = box.getSlot(slot);
        if (item.isEmpty()) {
            return;
        }
        ItemStack damaged = item.copy();
        damaged.damage(1, player, p -> {
        });
        box.setSlot(slot, damaged.isEmpty() ? ItemStack.EMPTY : damaged);
    }

    @Override
    public LootFunctionType getType() {
        return CDModLootFunctions.FISHING_CATCH;
    }

    public static class Serializer implements JsonSerializer<FishingCatchLootFunction> {
        @Override
        public void toJson(JsonObject json, FishingCatchLootFunction function, JsonSerializationContext context) {
        }

        @Override
        public FishingCatchLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
            return new FishingCatchLootFunction();
        }
    }
}
