package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.registry.CDModItems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

// 把瓶中信塞进"埋藏的宝藏"箱子（对应 1.21 版把 message_bottle 加进 buried_treasure 的做法）。
public final class NoteLootEvents {
    private static final Identifier BURIED_TREASURE = new Identifier("minecraft", "chests/buried_treasure");
    private static final float CHANCE = 0.35f;

    private NoteLootEvents() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!BURIED_TREASURE.equals(id)) {
                return;
            }
            LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(CDModItems.MESSAGE_BOTTLE)
                            .conditionally(RandomChanceLootCondition.builder(CHANCE)));
            tableBuilder.pool(pool);
        });
    }
}
