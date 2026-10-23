package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public class VillagerTrade {
    public static void register() {
        // 渔民收购海鲜，付给绿宝石（对应 Forge VillagerTradesEvent + emeraldForItemsTrade）
        if (Config.FISHERMAN_BUY_SEAFOOD.get()) {
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FISHERMAN, 1, factories -> {
                factories.add(emeraldForItemsTrade(CDModItems.CRAB, 6, 16, 2));
                factories.add(emeraldForItemsTrade(CDModItems.RAW_SHRIMP, 8, 16, 2));
            });
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FISHERMAN, 2, factories -> {
                factories.add(emeraldForItemsTrade(CDModItems.RAW_CLAWSTER, 4, 16, 5));
                factories.add(emeraldForItemsTrade(CDModItems.CLAM, 3, 16, 5));
                // 1.21 新增：渔民卖蚯蚓（2 绿宝石 → 8 条，16 次）
                factories.add(itemForEmeraldTrade(2, CDModItems.WORM, 8, 16, 3));
            });
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FISHERMAN, 3, factories -> {
                // 1.21 新增：渔民卖鱼线与假饵
                factories.add(itemForEmeraldTrade(4, CDModItems.FISHING_LINE, 3, 8));
                factories.add(itemForEmeraldTrade(6, CDModItems.DOUBLE_LURE, 3, 8));
                factories.add(itemForEmeraldTrade(6, CDModItems.BARBED_LURE, 3, 8));
            });
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FISHERMAN, 4, factories -> {
                factories.add(emeraldForItemsTrade(CDModItems.PEARL, 1, 16, 5));
                // 1.21 新增：高级假饵 + 蚯蚓箱 / 钓具箱
                factories.add(itemForEmeraldTrade(8, CDModItems.AUTOMATIC_LURE, 3, 12));
                factories.add(itemForEmeraldTrade(8, CDModItems.STORM_LURE, 3, 12));
                factories.add(itemForEmeraldTrade(8, CDModItems.MAGNETIC_LURE, 3, 12));
                factories.add(itemForEmeraldTrade(8, CDModItems.WORM_BIN, 4, 24));
                factories.add(itemForEmeraldTrade(8, CDModItems.TACKLE_BOX, 4, 24));
            });
        }

        // 流浪商人用珍珠换宝藏（对应 Forge WandererTradesEvent）
        if (Config.WANDERING_TRADER_PEARLS.get()) {
            TradeOfferHelper.registerWanderingTraderOffers(1, factories -> {
                factories.add(emeraldForItemsTrade(CDModItems.CRAB_CLAW, 8, 4, 12));
                factories.add(emeraldForItemsTrade(CDModItems.PALM_SAPLING, 2, 8, 12));
                factories.add(heartForPearlsTrade(CDModItems.PEARL, 32, 4, 12));
                factories.add(spongeForPearlsTrade(CDModItems.PEARL, 8, 4, 12));
                factories.add(tridentForPearlsTrade(CDModItems.PEARL, 64, 1, 12));
            });
        }
    }

    // 渔民：支付 count 个海鲜，换取 1 个绿宝石
    private static TradeOffers.Factory emeraldForItemsTrade(Item item, int count, int maxTrades, int xp) {
        return (entity, random) -> new TradeOffer(
                new ItemStack(item, count), ItemStack.EMPTY, new ItemStack(Items.EMERALD),
                maxTrades, xp, 0.05F);
    }

    // 流浪商人：支付 count 个珍珠，换取海洋之心
    private static TradeOffers.Factory heartForPearlsTrade(Item item, int count, int maxTrades, int xp) {
        return (entity, random) -> new TradeOffer(
                new ItemStack(item, count), ItemStack.EMPTY, new ItemStack(Items.HEART_OF_THE_SEA),
                maxTrades, xp, 0.05F);
    }

    // 流浪商人：支付 count 个珍珠，换取海绵
    private static TradeOffers.Factory spongeForPearlsTrade(Item item, int count, int maxTrades, int xp) {
        return (entity, random) -> new TradeOffer(
                new ItemStack(item, count), ItemStack.EMPTY, new ItemStack(Items.SPONGE),
                maxTrades, xp, 0.05F);
    }

    // 流浪商人：支付 count 个珍珠，换取三叉戟
    private static TradeOffers.Factory tridentForPearlsTrade(Item item, int count, int maxTrades, int xp) {
        return (entity, random) -> new TradeOffer(
                new ItemStack(item, count), ItemStack.EMPTY, new ItemStack(Items.TRIDENT),
                maxTrades, xp, 0.05F);
    }

    // 渔民：支付 price 个绿宝石，换 count 个物品（对应 1.21 版 itemForEmeraldTrade）
    // 注意 TradeOffer 的参数顺序：firstBuy=玩家付出、secondBuy=第二种付款、sell=玩家得到
    private static TradeOffers.Factory itemForEmeraldTrade(int price, Item item, int count, int maxTrades, int xp) {
        return (entity, random) -> new TradeOffer(
                new ItemStack(Items.EMERALD, price), ItemStack.EMPTY, new ItemStack(item, count),
                maxTrades, xp, 0.05F);
    }

    private static TradeOffers.Factory itemForEmeraldTrade(int price, Item item, int maxTrades, int xp) {
        return itemForEmeraldTrade(price, item, 1, maxTrades, xp);
    }
}
