package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CDModCreativeTabs {
    public static final Identifier TAB_ID = CrabbersDelightFabric.id("crabbers_delight");
    public static final RegistryKey<ItemGroup> TAB_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, TAB_ID);

    public static final ItemGroup TAB_CRABBERS_DELIGHT = FabricItemGroup.builder()
            .icon(() -> new ItemStack(CDModItems.RAW_CLAWSTER))
            .displayName(Text.translatable("itemGroup.crabbersdelight"))
            .build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, TAB_ID, TAB_CRABBERS_DELIGHT);

        ItemGroupEvents.modifyEntriesEvent(TAB_KEY).register(entries -> {
            // 材料与工具
            entries.add(CDModItems.CLAM);
            entries.add(CDModItems.PEARL);
            entries.add(CDModItems.CAN);
            entries.add(CDModItems.CORAL_FRAGMENTS);
            entries.add(CDModItems.FISH_BONES);
            entries.add(CDModItems.BUCKET_OF_CRAB_CHUM);
            entries.add(CDModItems.BUCKET_OF_CLAWSTER_CHUM);
            entries.add(CDModItems.BUCKET_OF_CLAM_CHUM);
            entries.add(CDModItems.BUCKET_OF_SHRIMP_CHUM);
            entries.add(CDModItems.CRAB_CLAW);
            entries.add(CDModItems.PEARL_NECKLACE);

            entries.add(CDModItems.PALM_LOG);
            entries.add(CDModItems.PALM_WOOD);
            entries.add(CDModItems.STRIPPED_PALM_LOG);
            entries.add(CDModItems.STRIPPED_PALM_WOOD);
            entries.add(CDModItems.PALM_LEAVES);
            entries.add(CDModItems.PALM_SAPLING);
            entries.add(CDModItems.CRAB);
            entries.add(CDModItems.CRAB_SPAWN_EGG);
            entries.add(CDModItems.CRAB_BUCKET);
            entries.add(CDModItems.CRAB_TRAP);
            entries.add(CDModItems.PALM_PLANKS);
            entries.add(CDModItems.PALM_STAIRS);
            entries.add(CDModItems.PALM_SLAB);
            entries.add(CDModItems.PALM_FENCE);
            entries.add(CDModItems.PALM_FENCE_GATE);
            entries.add(CDModItems.PALM_DOOR);
            entries.add(CDModItems.PALM_TRAPDOOR);
            entries.add(CDModItems.PALM_PRESSURE_PLATE);
            entries.add(CDModItems.PALM_BUTTON);
            entries.add(CDModItems.PALM_CABINET);
            entries.add(CDModItems.PALM_SIGN);
            entries.add(CDModItems.PALM_HANGING_SIGN);
            entries.add(CDModItems.PALM_BOAT);
            entries.add(CDModItems.PALM_CHEST_BOAT);
            entries.add(CDModItems.COCONUT);
            entries.add(CDModItems.COCONUT_HALVE);
            entries.add(CDModItems.COCONUT_MILK);
            entries.add(CDModItems.COCONUT_PUDDING);
            entries.add(CDModItems.COCONUT_HELMET);
            entries.add(CDModItems.SEASHELLS);

            // 海鲜食物
            entries.add(CDModItems.COOKED_CRAB);
            entries.add(CDModItems.RAW_CLAWSTER);
            entries.add(CDModItems.COOKED_CLAWSTER);
            entries.add(CDModItems.RAW_SHRIMP);
            entries.add(CDModItems.COOKED_SHRIMP);
            entries.add(CDModItems.RAW_CLAM_MEAT);
            entries.add(CDModItems.COOKED_CLAM_MEAT);
            entries.add(CDModItems.RAW_SQUID_TENTACLES);
            entries.add(CDModItems.RAW_GLOW_SQUID_TENTACLES);
            entries.add(CDModItems.COOKED_SQUID_TENTACLES);
            entries.add(CDModItems.COOKED_GLOW_SQUID_TENTACLES);
            entries.add(CDModItems.RAW_FROG_LEG);
            entries.add(CDModItems.COOKED_FROG_LEG);
            entries.add(CDModItems.CRAB_CAKES);
            entries.add(CDModItems.CRAB_LEGS);
            entries.add(CDModItems.FISH_STICK);
            entries.add(CDModItems.FRIED_FISH);
            entries.add(CDModItems.FISH_SANDWICH);
            entries.add(CDModItems.TURTLE_STEW);
            entries.add(CDModItems.SURF_AND_TURF);
            entries.add(CDModItems.SHRIMP_SKEWER);
            entries.add(CDModItems.CLAM_BAKE);
            entries.add(CDModItems.COOKED_TROPICAL_FISH);
            entries.add(CDModItems.PUFFERFISH_SLICE);
            entries.add(CDModItems.COOKED_PUFFERFISH_SLICE);
            entries.add(CDModItems.TROPICAL_FISH_SLICE);
            entries.add(CDModItems.COOKED_TROPICAL_FISH_SLICE);
            entries.add(CDModItems.STUFFED_NAUTILUS_SHELL);
            entries.add(CDModItems.SQUID_KEBAB);
            entries.add(CDModItems.FROG_LEG_KEBAB);
            entries.add(CDModItems.JAR_OF_PICKLES);
            entries.add(CDModItems.KELP_SHAKE);
            entries.add(CDModItems.SEA_PICKLE_JUICE);
            entries.add(CDModItems.BISQUE);
            entries.add(CDModItems.SEAFOOD_GUMBO);
            entries.add(CDModItems.CLAM_CHOWDER);
            entries.add(CDModItems.SHRIMP_FRIED_RICE);
            entries.add(CDModItems.CORAL_CRUNCH);

            // 存储方块
            entries.add(CDModItems.CRAB_BARREL);
            entries.add(CDModItems.CLAM_BARREL);
            entries.add(CDModItems.CLAWSTER_BARREL);
            entries.add(CDModItems.SHRIMP_BARREL);
            entries.add(CDModItems.COD_BARREL);
            entries.add(CDModItems.SALMON_BARREL);
            entries.add(CDModItems.PUFFERFISH_BARREL);
            entries.add(CDModItems.TROPICAL_FISH_BARREL);
            entries.add(CDModItems.SQUID_BARREL);
            entries.add(CDModItems.GLOW_SQUID_BARREL);
            entries.add(CDModItems.FROG_LEG_BARREL);
            entries.add(CDModItems.COCONUT_CRATE);
            entries.add(CDModItems.SCUTE_BLOCK);
            entries.add(CDModItems.PEARL_BLOCK);
            entries.add(CDModItems.NAUTILUS_SHELL_BLOCK);
            entries.add(CDModItems.SEA_PICKLE_CRATE);

            // 1.3.x 新增：蚯蚓 / 蚯蚓土 / 蚯蚓箱 / 海螺
            entries.add(CDModItems.WORM);
            entries.add(CDModItems.WORMY_DIRT);
            entries.add(CDModItems.WORM_BIN);
            entries.add(CDModItems.CONCH);

            entries.add(CDModItems.FISH_PLAQUE);
            entries.add(CDModItems.NOTE);
            entries.add(CDModItems.SIGNED_NOTE);
            entries.add(CDModItems.MESSAGE_BOTTLE);
            entries.add(CDModItems.BOTTLED_NOTE);
            // 1.3.x 新增：钓具箱与钓鱼装备
            entries.add(CDModItems.TACKLE_BOX);
            entries.add(CDModItems.FISHING_LINE);
            entries.add(CDModItems.BARBED_LURE);
            entries.add(CDModItems.DOUBLE_LURE);
            entries.add(CDModItems.MAGNETIC_LURE);
            entries.add(CDModItems.AUTOMATIC_LURE);
            entries.add(CDModItems.STORM_LURE);

            // 1.3.x 新增：救生圈
            entries.add(CDModItems.LIFE_PRESERVER);
        });
    }
}
