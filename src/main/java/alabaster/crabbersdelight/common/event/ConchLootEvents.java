package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.registry.CDModItems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

// 海螺：埋藏的宝藏里约 1/7 概率出一个（对应 1.21 版的 add_loot_buried_treasure 战利品修饰器）
public class ConchLootEvents {
    private static final Identifier BURIED_TREASURE = new Identifier("minecraft", "chests/buried_treasure");

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!BURIED_TREASURE.equals(id)) {
                return;
            }
            tableBuilder.pool(LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .with(ItemEntry.builder(CDModItems.CONCH).weight(1))
                    .with(EmptyEntry.builder().weight(6)));
        });
    }
}
