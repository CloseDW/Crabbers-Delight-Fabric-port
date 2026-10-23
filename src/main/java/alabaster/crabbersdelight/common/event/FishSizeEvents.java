package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.loot.FishSizeLootFunction;
import alabaster.crabbersdelight.common.loot.FishingCatchLootFunction;
import net.fabricmc.fabric.api.loot.v2.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.util.Identifier;

// 把"装备结算"和"尺寸抽取"两个战利品函数挂到原版钓鱼战利品表上
// （对 fish / junk / treasure 三个引用表的结果统一生效；顺序：先结算装备，再抽尺寸）。
public class FishSizeEvents {
    private static final Identifier FISHING_LOOT = new Identifier("minecraft", "gameplay/fishing");

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!FISHING_LOOT.equals(id)) {
                return;
            }
            ((FabricLootTableBuilder) tableBuilder).modifyPools(pool -> {
                FabricLootPoolBuilder fabricPool = (FabricLootPoolBuilder) pool;
                fabricPool.apply(FishingCatchLootFunction.INSTANCE);
                fabricPool.apply(FishSizeLootFunction.INSTANCE);
            });
        });
    }
}
