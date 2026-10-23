package alabaster.crabbersdelight.common.tags;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class CDModTags {
    public static final TagKey<Item> CRAB_TEMPT_ITEM =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("crab_tempt_item"));
    public static final TagKey<Block> CRAB_SPAWN_ON =
            TagKey.of(RegistryKeys.BLOCK, CrabbersDelightFabric.id("crab_spawn_on"));
    public static final TagKey<Item> PALM_LOGS =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("palm_logs"));
    public static final TagKey<Block> PALM_LOG_BLOCKS =
            TagKey.of(RegistryKeys.BLOCK, CrabbersDelightFabric.id("palm_log_blocks"));
    public static final TagKey<Item> CRAB_TRAP_BAIT =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("crab_trap_bait"));
    public static final TagKey<Item> CREATURE_CHUMS =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("creature_chums"));
    public static final TagKey<Item> RAW_SEAFOOD =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("raw_seafood"));
    public static final TagKey<Item> COOKED_SEAFOOD =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("cooked_seafood"));
    public static final TagKey<Item> RAW_SQUID =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("raw_squid"));
    public static final TagKey<Item> COOKED_SQUID =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("cooked_squid"));

    // 1.3.x 新增：钓鱼装备标签
    public static final TagKey<Item> FISHING_BAIT =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("fishing_bait"));
    public static final TagKey<Item> FISHING_LURE =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("fishing_lure"));
    public static final TagKey<Item> FISHING_LINE =
            TagKey.of(RegistryKeys.ITEM, CrabbersDelightFabric.id("fishing_line"));
}
