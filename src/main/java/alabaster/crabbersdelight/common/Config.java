package alabaster.crabbersdelight.common;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_SETTINGS = "Settings";
    public static ForgeConfigSpec.BooleanValue FISHERMAN_BUY_SEAFOOD;
    public static ForgeConfigSpec.BooleanValue WANDERING_TRADER_PEARLS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MIN_TICKS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_TICKS;
    public static ForgeConfigSpec.BooleanValue COOK_IN_POTS;
    public static ForgeConfigSpec.BooleanValue REQUIRE_SURROUNDING_WATER;
    public static ForgeConfigSpec.ConfigValue<Integer> SEASHELL_VARIANT_COUNT;

    public static final String CATEGORY_WORLD = "world";
    public static ForgeConfigSpec.BooleanValue GENERATE_PALM_TREES;
    public static ForgeConfigSpec.BooleanValue GENERATE_SEASHELLS;
    public static ForgeConfigSpec.BooleanValue GENERATE_WORMY_DIRT;
    public static ForgeConfigSpec.IntValue CHANCE_WORMY_DIRT;
    public static ForgeConfigSpec.BooleanValue DIRT_DROPS_WORMS;
    public static ForgeConfigSpec.BooleanValue GENERATE_CRAB_SPAWNS;
    public static ForgeConfigSpec.IntValue CRAB_SPAWN_WEIGHT;
    public static ForgeConfigSpec.IntValue CRAB_SPAWN_MIN;
    public static ForgeConfigSpec.IntValue CRAB_SPAWN_MAX;

    public static final String CATEGORY_FISH_SIZE = "fish_size";
    public static ForgeConfigSpec.BooleanValue FISH_SIZE_ENABLED;

    public static final String CATEGORY_FISHING_SPOTS = "fishing_spots";
    public static ForgeConfigSpec.ConfigValue<Integer> FISHING_SPOT_SPAWN_INTERVAL;
    public static ForgeConfigSpec.IntValue FISHING_SPOT_MAX_PER_AREA;
    public static ForgeConfigSpec.BooleanValue OUTSIDE_FISHING_SPOT_LUCK_PENALTY_ENABLED;
    public static ForgeConfigSpec.DoubleValue OUTSIDE_FISHING_SPOT_LUCK_PENALTY_AMOUNT;

    public static final String CATEGORY_DEBUG = "debug";
    public static ForgeConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("Game Settings").push(CATEGORY_SETTINGS);
        FISHERMAN_BUY_SEAFOOD = COMMON_BUILDER.comment("Should fisherman buy/sell items related to this mods items (Seafood and pearls)")
                .define("fishermanBuyCDItems", true);
        WANDERING_TRADER_PEARLS = COMMON_BUILDER.comment("Should the Wandering Trader sell items in exchange for pearls?")
                .define("wanderingTraderDealsPearls", true);
        COOK_IN_POTS = COMMON_BUILDER.comment("Should seafood only be able to be cooked through the Farmer's Delight pot?")
                .define("cookInPot", true);
        MIN_TICKS = COMMON_BUILDER.comment("Minimum ticks before seafood can be gathered by the crab trap. Default = 4000")
                .define("minTicks", 4000);
        MAX_TICKS = COMMON_BUILDER.comment("Maximum ticks before seafood can be gathered by the crab trap. Default = 8000")
                .define("maxTicks", 8000);
        REQUIRE_SURROUNDING_WATER = COMMON_BUILDER.comment("Requires the crab trap to have a 3x3 of open water or waterlogged blocks around it to be able to function. Default = true")
                .define("require_surrounding_water", true);
        SEASHELL_VARIANT_COUNT = COMMON_BUILDER.comment("Number of seashell variants available (must match textures/models/blockstates files)")
                .defineInRange("seashellVariantCount", 7, 1, 64);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("World generation").push(CATEGORY_WORLD);

        COMMON_BUILDER.comment("Palm tree generation").push("palm_trees");
        GENERATE_PALM_TREES = COMMON_BUILDER.comment("Should palm trees generate on beaches?")
                .define("generatePalmTrees", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Seashell generation").push("seashells");
        GENERATE_SEASHELLS = COMMON_BUILDER.comment("Should seashells generate on beaches and ocean floors?")
                .define("generateSeashells", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Wormy Dirt generation").push("wormy_dirt");
        GENERATE_WORMY_DIRT = COMMON_BUILDER.comment("Should wormy dirt generate in newly generated terrain?")
                .define("generateWormyDirt", true);
        CHANCE_WORMY_DIRT = COMMON_BUILDER.comment(
                        "Chance of generating per chunk. Smaller value = more frequent. Default = 3")
                .defineInRange("chance", 3, 1, Integer.MAX_VALUE);
        DIRT_DROPS_WORMS = COMMON_BUILDER.comment(
                        "Allows any block in the 'dirt' block tag to drop worms at a 1% chance when broken. Default = true")
                .define("dirt_drops_worms", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Crab spawning").push("crab_spawns");
        GENERATE_CRAB_SPAWNS = COMMON_BUILDER.comment("Should crabs spawn naturally in the world?")
                .define("generateCrabSpawns", true);
        CRAB_SPAWN_WEIGHT = COMMON_BUILDER.comment("Spawn weight. Higher value = more frequent relative to other mobs.")
                .defineInRange("spawnWeight", 10, 1, Integer.MAX_VALUE);
        CRAB_SPAWN_MIN = COMMON_BUILDER.comment("Minimum crabs per spawn attempt.")
                .defineInRange("minCount", 2, 1, Integer.MAX_VALUE);
        CRAB_SPAWN_MAX = COMMON_BUILDER.comment("Maximum crabs per spawn attempt.")
                .defineInRange("maxCount", 3, 1, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Fish Size").push(CATEGORY_FISH_SIZE);
        FISH_SIZE_ENABLED = COMMON_BUILDER.comment(
                        "If true, caught fish randomly roll a size (Tiny/Small/Regular/Large/Huge) that scales their",
                        "hunger value and is shown in their tooltip. Default = true")
                .define("fishSizeEnabled", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Fishing Spots").push(CATEGORY_FISHING_SPOTS);
        FISHING_SPOT_SPAWN_INTERVAL = COMMON_BUILDER.comment(
                        "Ticks between fishing spot spawn attempts per player. Lower value = more common. Default = 200")
                .defineInRange("spawnAttemptInterval", 200, 20, Integer.MAX_VALUE);
        FISHING_SPOT_MAX_PER_AREA = COMMON_BUILDER.comment(
                        "Maximum number of fishing spots allowed to cluster within a 64-block radius. Default = 3")
                .defineInRange("maxPerArea", 3, 1, Integer.MAX_VALUE);
        OUTSIDE_FISHING_SPOT_LUCK_PENALTY_ENABLED = COMMON_BUILDER.comment(
                        "If true, fishing outside a fishing spot applies a negative luck penalty. Default = true")
                .define("outsideSpotLuckPenaltyEnabled", true);
        OUTSIDE_FISHING_SPOT_LUCK_PENALTY_AMOUNT = COMMON_BUILDER.comment(
                        "How much luck is subtracted when fishing outside a fishing spot. Default = 4.0")
                .defineInRange("outsideSpotLuckPenaltyAmount", 4.0, 0.0, Double.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Debug").push(CATEGORY_DEBUG);
        DEBUG_LOGGING = COMMON_BUILDER.comment(
                        "Print [CD-DEBUG] logs for the fishing gear / fishing spot systems. Default = false")
                .define("debugLogging", false);
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();

        // 移植点：Forge 用 ModLoadingContext 注册，Fabric 用 FCAP 的 ForgeConfigRegistry
        ForgeConfigRegistry.INSTANCE.register(CrabbersDelightFabric.MOD_ID, ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    // 空方法：强制类加载，确保上面的静态块（含配置注册）在初始化时就执行
    public static void register() {
    }
}
