package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.CDFoodValues;
import alabaster.crabbersdelight.common.entity.boat.CDBoatEntity;
import alabaster.crabbersdelight.common.item.CDBoatItem;
import alabaster.crabbersdelight.common.item.ChumItem;
import alabaster.crabbersdelight.common.item.CoconutHelmetItem;
import alabaster.crabbersdelight.common.item.CrabClawItem;
import alabaster.crabbersdelight.common.item.BottledNoteItem;
import alabaster.crabbersdelight.common.item.MessageBottleItem;
import alabaster.crabbersdelight.common.item.NoteItem;
import alabaster.crabbersdelight.common.item.SignedNoteItem;
import alabaster.crabbersdelight.common.item.PearlNecklaceItem;
import alabaster.crabbersdelight.common.item.ConchItem;
import alabaster.crabbersdelight.common.item.WormItem;
import alabaster.crabbersdelight.common.item.FishingLineItem;
import alabaster.crabbersdelight.common.item.LifePreserverItem;
import alabaster.crabbersdelight.common.item.LureItem;
import alabaster.crabbersdelight.common.item.TackleBoxItem;
import alabaster.crabbersdelight.common.item.fishing.LureEffect;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.SignItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundEvents;
import net.minecraft.item.Items;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.DrinkableItem;
import vectorwing.farmersdelight.common.item.MilkBottleItem;

public class CDModItems {
    // 物品属性助手（对应 Forge 的 foodItem / bowlFoodItem / drinkItem / shellFoodItem）
    public static Item.Settings foodItem(FoodComponent food) {
        return new Item.Settings().food(food);
    }

    public static Item.Settings bowlFoodItem(FoodComponent food) {
        return new Item.Settings().food(food).recipeRemainder(Items.BOWL).maxCount(16);
    }

    public static Item.Settings drinkItem(FoodComponent food) {
        return new Item.Settings().food(food).recipeRemainder(Items.GLASS_BOTTLE).maxCount(16);
    }

    public static Item.Settings shellFoodItem(FoodComponent food) {
        return new Item.Settings().food(food).recipeRemainder(Items.NAUTILUS_SHELL);
    }

    public static Item.Settings scuteFoodItem(FoodComponent food) {
        return new Item.Settings().food(food).recipeRemainder(Items.SCUTE);
    }

    // 材料
    public static final Item CLAM = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("clam"),
            new Item(new Item.Settings()));
    public static final Item PEARL = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("pearl"),
            new Item(new Item.Settings()));
    public static final Item CAN = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("can"),
            new Item(new Item.Settings()));
    public static final Item CORAL_FRAGMENTS = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coral_fragments"),
            new Item(new Item.Settings()));
    public static final Item FISH_BONES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("fish_bones"),
            new Item(new Item.Settings()));

    // 鱼饵桶：ChumItem 自带 48 点耐久，配合捕蟹笼的 CREATURE_CHUMS 标签逻辑
    public static final Item BUCKET_OF_CRAB_CHUM = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("bucket_of_crab_chum"),
            new ChumItem(new Item.Settings()));
    public static final Item BUCKET_OF_CLAWSTER_CHUM = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("bucket_of_clawster_chum"),
            new ChumItem(new Item.Settings()));
    public static final Item BUCKET_OF_CLAM_CHUM = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("bucket_of_clam_chum"),
            new ChumItem(new Item.Settings()));
    public static final Item BUCKET_OF_SHRIMP_CHUM = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("bucket_of_shrimp_chum"),
            new ChumItem(new Item.Settings()));

    // 工具/装备
    public static final Item CRAB_CLAW = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab_claw"),
            new CrabClawItem(new Item.Settings()));
    public static final Item PEARL_NECKLACE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("pearl_necklace"),
            new PearlNecklaceItem(new Item.Settings().maxCount(1)));

    public static final Item SEASHELLS = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("seashells"),
            new BlockItem(CDModBlocks.SEASHELLS, new Item.Settings()));

    public static final Item COCONUT = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coconut"),
            new BlockItem(CDModBlocks.COCONUT, new Item.Settings()));

    public static final Item COCONUT_HALVE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coconut_halve"),
            new Item(new Item.Settings().food(CDFoodValues.COCONUT_HALVE)));

    public static final Item COCONUT_HELMET = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coconut_helmet"),
            new CoconutHelmetItem(new Item.Settings().maxCount(1)));

    // 棕榈树物品
    public static final Item PALM_LOG = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_log"),
            new BlockItem(CDModBlocks.PALM_LOG, new Item.Settings()));
    public static final Item PALM_WOOD = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_wood"),
            new BlockItem(CDModBlocks.PALM_WOOD, new Item.Settings()));
    public static final Item STRIPPED_PALM_LOG = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("stripped_palm_log"),
            new BlockItem(CDModBlocks.STRIPPED_PALM_LOG, new Item.Settings()));
    public static final Item STRIPPED_PALM_WOOD = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("stripped_palm_wood"),
            new BlockItem(CDModBlocks.STRIPPED_PALM_WOOD, new Item.Settings()));
    public static final Item PALM_LEAVES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_leaves"),
            new BlockItem(CDModBlocks.PALM_LEAVES, new Item.Settings()));
    public static final Item PALM_SAPLING = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_sapling"),
            new BlockItem(CDModBlocks.PALM_SAPLING, new Item.Settings()));

    // 螃蟹相关物品
    public static final Item CRAB = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_CRAB)));
    public static final Item CRAB_SPAWN_EGG = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab_spawn_egg"),
            new SpawnEggItem(CDModEntities.CRAB, 0x2f437c, 0xf48b45, new Item.Settings()));
    public static final Item CRAB_BUCKET = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab_bucket"),
            new EntityBucketItem(CDModEntities.CRAB, Fluids.WATER, SoundEvents.ITEM_BUCKET_EMPTY_FISH,
                    new Item.Settings().maxCount(1)));

    public static final Item CRAB_TRAP = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab_trap"),
            new BlockItem(CDModBlocks.CRAB_TRAP, new Item.Settings()));

    // 棕榈木制品
    public static final Item PALM_PLANKS = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_planks"),
            new BlockItem(CDModBlocks.PALM_PLANKS, new Item.Settings()));
    public static final Item PALM_SIGN = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_sign"),
            new SignItem(new Item.Settings().maxCount(16), CDModBlocks.PALM_SIGN, CDModBlocks.PALM_WALL_SIGN));
    public static final Item PALM_HANGING_SIGN = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_hanging_sign"),
            new HangingSignItem(CDModBlocks.PALM_HANGING_SIGN, CDModBlocks.PALM_WALL_HANGING_SIGN,
                    new Item.Settings().maxCount(16)));
    public static final Item PALM_BOAT = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_boat"),
            new CDBoatItem(false, CDBoatEntity.Type.PALM, new Item.Settings()));
    public static final Item PALM_CHEST_BOAT = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("palm_chest_boat"),
            new CDBoatItem(true, CDBoatEntity.Type.PALM, new Item.Settings()));

    // 棕榈木套装
    public static final Item PALM_STAIRS = blockItem("palm_stairs", CDModBlocks.PALM_STAIRS);
    public static final Item PALM_SLAB = blockItem("palm_slab", CDModBlocks.PALM_SLAB);
    public static final Item PALM_FENCE = blockItem("palm_fence", CDModBlocks.PALM_FENCE);
    public static final Item PALM_FENCE_GATE = blockItem("palm_fence_gate", CDModBlocks.PALM_FENCE_GATE);
    public static final Item PALM_DOOR = blockItem("palm_door", CDModBlocks.PALM_DOOR);
    public static final Item PALM_TRAPDOOR = blockItem("palm_trapdoor", CDModBlocks.PALM_TRAPDOOR);
    public static final Item PALM_PRESSURE_PLATE = blockItem("palm_pressure_plate", CDModBlocks.PALM_PRESSURE_PLATE);
    public static final Item PALM_BUTTON = blockItem("palm_button", CDModBlocks.PALM_BUTTON);
    public static final Item PALM_CABINET = blockItem("palm_cabinet", CDModBlocks.PALM_CABINET);

    // 存储方块
    public static final Item CRAB_BARREL = blockItem("crab_barrel", CDModBlocks.CRAB_BARREL);
    public static final Item CLAM_BARREL = blockItem("clam_barrel", CDModBlocks.CLAM_BARREL);
    public static final Item CLAWSTER_BARREL = blockItem("clawster_barrel", CDModBlocks.CLAWSTER_BARREL);
    public static final Item SHRIMP_BARREL = blockItem("shrimp_barrel", CDModBlocks.SHRIMP_BARREL);
    public static final Item COD_BARREL = blockItem("cod_barrel", CDModBlocks.COD_BARREL);
    public static final Item SALMON_BARREL = blockItem("salmon_barrel", CDModBlocks.SALMON_BARREL);
    public static final Item PUFFERFISH_BARREL = blockItem("pufferfish_barrel", CDModBlocks.PUFFERFISH_BARREL);
    public static final Item TROPICAL_FISH_BARREL = blockItem("tropical_fish_barrel", CDModBlocks.TROPICAL_FISH_BARREL);
    public static final Item SQUID_BARREL = blockItem("squid_barrel", CDModBlocks.SQUID_BARREL);
    public static final Item GLOW_SQUID_BARREL = blockItem("glow_squid_barrel", CDModBlocks.GLOW_SQUID_BARREL);
    public static final Item FROG_LEG_BARREL = blockItem("frog_leg_barrel", CDModBlocks.FROG_LEG_BARREL);
    public static final Item COCONUT_CRATE = blockItem("coconut_crate", CDModBlocks.COCONUT_CRATE);
    public static final Item SCUTE_BLOCK = blockItem("scute_block", CDModBlocks.SCUTE_BLOCK);
    public static final Item PEARL_BLOCK = blockItem("pearl_block", CDModBlocks.PEARL_BLOCK);
    public static final Item NAUTILUS_SHELL_BLOCK = blockItem("nautilus_shell_block", CDModBlocks.NAUTILUS_SHELL_BLOCK);
    public static final Item SEA_PICKLE_CRATE = blockItem("sea_pickle_crate", CDModBlocks.SEA_PICKLE_CRATE);

    // 1.3.x 新增：蚯蚓箱 / 蚯蚓土 / 蚯蚓 / 海螺
    public static final Item WORM_BIN = blockItem("worm_bin", CDModBlocks.WORM_BIN);
    public static final Item WORMY_DIRT = blockItem("wormy_dirt", CDModBlocks.WORMY_DIRT);
    public static final Item WORM = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("worm"),
            new WormItem(new Item.Settings()));
    public static final Item CONCH = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("conch"),
            new ConchItem(new Item.Settings().maxCount(1)));

    // 1.3.x 新增：钓具箱 + 钓鱼装备（假饵 / 鱼线）
    public static final Item TACKLE_BOX = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("tackle_box"),
            new TackleBoxItem(CDModBlocks.TACKLE_BOX, new Item.Settings().maxCount(1)));
    public static final Item BARBED_LURE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("barbed_lure"),
            new LureItem(new Item.Settings().maxCount(1).maxDamage(64), LureEffect.BARBED));
    public static final Item DOUBLE_LURE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("double_lure"),
            new LureItem(new Item.Settings().maxCount(1).maxDamage(64), LureEffect.DOUBLE));
    public static final Item MAGNETIC_LURE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("magnetic_lure"),
            new LureItem(new Item.Settings().maxCount(1).maxDamage(64), LureEffect.MAGNETIC));
    public static final Item AUTOMATIC_LURE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("automatic_lure"),
            new LureItem(new Item.Settings().maxCount(1).maxDamage(64), LureEffect.AUTOMATIC));
    public static final Item STORM_LURE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("storm_lure"),
            new LureItem(new Item.Settings().maxCount(1).maxDamage(64), LureEffect.STORM));
    public static final Item FISHING_LINE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("fishing_line"),
            new FishingLineItem(new Item.Settings().maxCount(1).maxDamage(64)));

    // 1.3.x 新增：救生圈（方块 + 可穿戴物品，同一 ID）
    public static final Item LIFE_PRESERVER = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("life_preserver"),
            new LifePreserverItem(CDModBlocks.LIFE_PRESERVER, new Item.Settings().maxCount(1)));

    // 1.3.x 新增：便条 / 已签名便条（批次 5）
    public static final Item NOTE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("note"),
            new NoteItem(CDModBlocks.NOTE_BLOCK, new Item.Settings().maxCount(16)));
    public static final Item BOTTLED_NOTE = Registry.register(Registries.ITEM,
            CrabbersDelightFabric.id("bottled_note"), new BottledNoteItem(new Item.Settings().maxCount(16)));
    public static final Item MESSAGE_BOTTLE = Registry.register(Registries.ITEM,
            CrabbersDelightFabric.id("message_bottle"), new MessageBottleItem(new Item.Settings().maxCount(16)));
    public static final Item SIGNED_NOTE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("signed_note"),
            new SignedNoteItem(new Item.Settings().maxCount(16)));
    public static final Item FISH_PLAQUE = blockItem("fish_plaque", CDModBlocks.FISH_PLAQUE);
    private static Item blockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, CrabbersDelightFabric.id(name),
                new BlockItem(block, new Item.Settings()));
    }

    // 生/熟海鲜
    public static final Item COOKED_CRAB = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_crab"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_CRAB)));
    public static final Item RAW_CLAWSTER = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("clawster"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_CLAWSTER)));
    public static final Item COOKED_CLAWSTER = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_clawster"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_CLAWSTER)));
    public static final Item RAW_SHRIMP = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("shrimp"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_SHRIMP), true));
    public static final Item COOKED_SHRIMP = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_shrimp"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_SHRIMP)));
    public static final Item RAW_CLAM_MEAT = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("raw_clam_meat"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_CLAM_MEAT)));
    public static final Item COOKED_CLAM_MEAT = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_clam_meat"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_CLAM_MEAT)));
    public static final Item RAW_SQUID_TENTACLES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("raw_squid_tentacles"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_SQUID_TENTACLES)));
    public static final Item RAW_GLOW_SQUID_TENTACLES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("raw_glow_squid_tentacles"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_SQUID_TENTACLES)));
    public static final Item COOKED_SQUID_TENTACLES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_squid_tentacles"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_SQUID_TENTACLES)));
    public static final Item COOKED_GLOW_SQUID_TENTACLES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_glow_squid_tentacles"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_SQUID_TENTACLES)));
    public static final Item RAW_FROG_LEG = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("raw_frog_leg"),
            new ConsumableItem(foodItem(CDFoodValues.RAW_FROG_LEG)));
    public static final Item COOKED_FROG_LEG = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_frog_leg"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_FROG_LEG)));

    // 1.3.x 新增：炸鱼 / 鱼肉三明治
    public static final Item FRIED_FISH = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("fried_fish"),
            new ConsumableItem(foodItem(CDFoodValues.FRIED_FISH)));
    public static final Item FISH_SANDWICH = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("fish_sandwich"),
            new ConsumableItem(foodItem(CDFoodValues.FISH_SANDWICH)));

    // 餐品
    public static final Item CRAB_CAKES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab_cakes"),
            new ConsumableItem(foodItem(CDFoodValues.CRAB_CAKES), true));
    public static final Item CRAB_LEGS = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("crab_legs"),
            new ConsumableItem(foodItem(CDFoodValues.CRAB_LEGS)));
    public static final Item FISH_STICK = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("fish_stick"),
            new ConsumableItem(foodItem(CDFoodValues.FISH_STICK)));
    public static final Item SURF_AND_TURF = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("surf_and_turf"),
            new ConsumableItem(foodItem(CDFoodValues.SURF_AND_TURF), true));
    public static final Item SHRIMP_SKEWER = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("shrimp_skewer"),
            new ConsumableItem(foodItem(CDFoodValues.SHRIMP_SKEWER)));
    public static final Item CLAM_BAKE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("clam_bake"),
            new ConsumableItem(foodItem(CDFoodValues.CLAM_BAKE), true));
    public static final Item COOKED_TROPICAL_FISH = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_tropical_fish"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_TROPICAL_FISH)));
    public static final Item PUFFERFISH_SLICE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("pufferfish_slice"),
            new ConsumableItem(foodItem(CDFoodValues.PUFFERFISH_SLICE), true));
    public static final Item COOKED_PUFFERFISH_SLICE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_pufferfish_slice"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_PUFFERFISH_SLICE)));
    public static final Item TROPICAL_FISH_SLICE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("tropical_fish_slice"),
            new ConsumableItem(foodItem(CDFoodValues.TROPICAL_FISH_SLICE)));
    public static final Item COOKED_TROPICAL_FISH_SLICE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("cooked_tropical_fish_slice"),
            new ConsumableItem(foodItem(CDFoodValues.COOKED_TROPICAL_FISH_SLICE)));
    public static final Item STUFFED_NAUTILUS_SHELL = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("stuffed_nautilus_shell"),
            new ConsumableItem(shellFoodItem(CDFoodValues.STUFFED_NAUTILUS_SHELL), true));
    public static final Item SQUID_KEBAB = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("squid_kebab"),
            new ConsumableItem(foodItem(CDFoodValues.SQUID_KEBAB)));
    public static final Item FROG_LEG_KEBAB = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("frog_leg_kebab"),
            new ConsumableItem(foodItem(CDFoodValues.FROG_LEG_KEBAB)));
    public static final Item JAR_OF_PICKLES = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("jar_of_pickles"),
            new ConsumableItem(foodItem(CDFoodValues.JAR_OF_PICKLES).recipeRemainder(Items.GLASS_BOTTLE)));

    // 饮品/甜点
    public static final Item COCONUT_MILK = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coconut_milk"),
            new MilkBottleItem(drinkItem(CDFoodValues.COCONUT_MILK)));
    public static final Item COCONUT_PUDDING = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coconut_pudding"),
            new ConsumableItem(foodItem(CDFoodValues.COCONUT_PUDDING).recipeRemainder(Items.GLASS_BOTTLE)));
    public static final Item KELP_SHAKE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("kelp_shake"),
            new DrinkableItem(drinkItem(CDFoodValues.KELP_SHAKE), true));
    public static final Item SEA_PICKLE_JUICE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("sea_pickle_juice"),
            new DrinkableItem(drinkItem(CDFoodValues.SEA_PICKLE_JUICE), true));

    // 碗装食物
    public static final Item BISQUE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("bisque"),
            new ConsumableItem(bowlFoodItem(CDFoodValues.BISQUE), true));
    public static final Item SEAFOOD_GUMBO = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("seafood_gumbo"),
            new ConsumableItem(bowlFoodItem(CDFoodValues.SEAFOOD_GUMBO), true));
    public static final Item CLAM_CHOWDER = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("clam_chowder"),
            new ConsumableItem(bowlFoodItem(CDFoodValues.CLAM_CHOWDER), true));
    public static final Item SHRIMP_FRIED_RICE = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("shrimp_fried_rice"),
            new ConsumableItem(bowlFoodItem(CDFoodValues.SHRIMP_FRIED_RICE), true));
    public static final Item CORAL_CRUNCH = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("coral_crunch"),
            new ConsumableItem(bowlFoodItem(CDFoodValues.CORAL_CRUNCH), true));

    // 1.3.x 新增：海龟汤（用海龟鳞甲当碗，喝完返还鳞甲）
    public static final Item TURTLE_STEW = Registry.register(Registries.ITEM, CrabbersDelightFabric.id("turtle_stew"),
            new ConsumableItem(scuteFoodItem(CDFoodValues.TURTLE_STEW), true));

    public static void register() {
    }
}
