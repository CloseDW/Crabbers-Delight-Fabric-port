package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.CDSaplingBlock;
import alabaster.crabbersdelight.common.block.CDLeavesBlock;
import alabaster.crabbersdelight.common.block.CoconutBlock;
import alabaster.crabbersdelight.common.block.CrabTrapBlock;
import alabaster.crabbersdelight.common.block.FishPlaqueBlock;
import alabaster.crabbersdelight.common.block.NoteBlock;
import alabaster.crabbersdelight.common.block.CDHangingSignBlock;
import alabaster.crabbersdelight.common.block.CDSignBlock;
import alabaster.crabbersdelight.common.block.CDWallHangingSignBlock;
import alabaster.crabbersdelight.common.block.CDWallSignBlock;
import alabaster.crabbersdelight.common.block.NautilusShellBlock;
import alabaster.crabbersdelight.common.block.SeashellBlock;
import alabaster.crabbersdelight.common.block.LifePreserverBlock;
import alabaster.crabbersdelight.common.block.TackleBoxBlock;
import alabaster.crabbersdelight.common.block.WormBinBlock;
import alabaster.crabbersdelight.common.worldgen.tree.PalmTreeGrower;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import vectorwing.farmersdelight.common.block.CabinetBlock;

public class CDModBlocks {
    public static final Block SEASHELLS = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("seashells"),
            new SeashellBlock(AbstractBlock.Settings.copy(Blocks.HORN_CORAL).offset(AbstractBlock.OffsetType.XZ)));

    public static final Block COCONUT = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("coconut"),
            new CoconutBlock(AbstractBlock.Settings.copy(Blocks.COCOA)));

    // 棕榈树核心方块
    public static final Block PALM_LOG = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_log"),
            new PillarBlock(AbstractBlock.Settings.copy(Blocks.OAK_LOG)));
    public static final Block PALM_WOOD = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_wood"),
            new PillarBlock(AbstractBlock.Settings.copy(Blocks.OAK_WOOD)));
    public static final Block STRIPPED_PALM_LOG = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("stripped_palm_log"),
            new PillarBlock(AbstractBlock.Settings.copy(Blocks.STRIPPED_OAK_LOG)));
    public static final Block STRIPPED_PALM_WOOD = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("stripped_palm_wood"),
            new PillarBlock(AbstractBlock.Settings.copy(Blocks.STRIPPED_OAK_WOOD)));
    public static final Block PALM_LEAVES = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_leaves"),
            new CDLeavesBlock(AbstractBlock.Settings.copy(Blocks.OAK_LEAVES)));
    public static final Block PALM_SAPLING = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_sapling"),
            new CDSaplingBlock(new PalmTreeGrower(), AbstractBlock.Settings.copy(Blocks.OAK_SAPLING), () -> Blocks.SAND));

    public static final Block CRAB_TRAP = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("crab_trap"),
            new CrabTrapBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)
                    .strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)));

    // 棕榈木制品
    public static final Block PALM_PLANKS = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_planks"),
            new Block(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)));
    public static final Block PALM_SIGN = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_sign"),
            new CDSignBlock(AbstractBlock.Settings.copy(Blocks.OAK_SIGN), CDWoodTypes.PALM));
    public static final Block PALM_WALL_SIGN = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_wall_sign"),
            new CDWallSignBlock(AbstractBlock.Settings.copy(Blocks.OAK_WALL_SIGN), CDWoodTypes.PALM));
    public static final Block PALM_HANGING_SIGN = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_hanging_sign"),
            new CDHangingSignBlock(AbstractBlock.Settings.copy(Blocks.OAK_HANGING_SIGN), CDWoodTypes.PALM));
    public static final Block PALM_WALL_HANGING_SIGN = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_wall_hanging_sign"),
            new CDWallHangingSignBlock(AbstractBlock.Settings.copy(Blocks.OAK_WALL_HANGING_SIGN), CDWoodTypes.PALM));

    // 棕榈木套装
    public static final Block PALM_STAIRS = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_stairs"),
            new StairsBlock(PALM_PLANKS.getDefaultState(), AbstractBlock.Settings.copy(Blocks.OAK_STAIRS)));
    public static final Block PALM_SLAB = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_slab"),
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.OAK_SLAB)));
    public static final Block PALM_FENCE = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_fence"),
            new FenceBlock(AbstractBlock.Settings.copy(Blocks.OAK_FENCE)));
    public static final Block PALM_FENCE_GATE = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_fence_gate"),
            new FenceGateBlock(AbstractBlock.Settings.copy(Blocks.OAK_FENCE_GATE), CDWoodTypes.PALM));
    public static final Block PALM_DOOR = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_door"),
            new DoorBlock(AbstractBlock.Settings.copy(Blocks.OAK_DOOR), BlockSetType.OAK));
    public static final Block PALM_TRAPDOOR = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_trapdoor"),
            new TrapdoorBlock(AbstractBlock.Settings.copy(Blocks.OAK_TRAPDOOR), BlockSetType.OAK));
    public static final Block PALM_PRESSURE_PLATE = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_pressure_plate"),
            new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING,
                    AbstractBlock.Settings.copy(Blocks.OAK_PRESSURE_PLATE), BlockSetType.OAK));
    public static final Block PALM_BUTTON = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_button"),
            new ButtonBlock(AbstractBlock.Settings.copy(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true));
    public static final Block PALM_CABINET = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("palm_cabinet"),
            new CabinetBlock(AbstractBlock.Settings.copy(Blocks.BARREL)));

    // 存储方块：海鲜桶/箱子
    public static final Block CRAB_BARREL = storageBlock("crab_barrel");
    public static final Block CLAM_BARREL = storageBlock("clam_barrel");
    public static final Block CLAWSTER_BARREL = storageBlock("clawster_barrel");
    public static final Block SHRIMP_BARREL = storageBlock("shrimp_barrel");
    public static final Block COD_BARREL = storageBlock("cod_barrel");
    public static final Block SALMON_BARREL = storageBlock("salmon_barrel");
    public static final Block PUFFERFISH_BARREL = storageBlock("pufferfish_barrel");
    public static final Block TROPICAL_FISH_BARREL = storageBlock("tropical_fish_barrel");
    public static final Block SQUID_BARREL = storageBlock("squid_barrel");
    public static final Block GLOW_SQUID_BARREL = storageBlock("glow_squid_barrel");
    public static final Block FROG_LEG_BARREL = storageBlock("frog_leg_barrel");
    public static final Block COCONUT_CRATE = storageBlock("coconut_crate");
    public static final Block SCUTE_BLOCK = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("scute_block"),
            new Block(AbstractBlock.Settings.copy(Blocks.PRISMARINE).strength(2.0F, 3.0F).sounds(BlockSoundGroup.CORAL)));
    public static final Block PEARL_BLOCK = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("pearl_block"),
            new Block(AbstractBlock.Settings.copy(Blocks.PRISMARINE).strength(2.0F, 3.0F).sounds(BlockSoundGroup.CORAL)));
    public static final Block NAUTILUS_SHELL_BLOCK = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("nautilus_shell_block"),
            new NautilusShellBlock(AbstractBlock.Settings.copy(Blocks.PRISMARINE).strength(2.0F, 3.0F).sounds(BlockSoundGroup.CORAL)));
    public static final Block SEA_PICKLE_CRATE = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("sea_pickle_crate"),
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)));

    // 1.3.x 新增：蚯蚓箱 / 蚯蚓土
    public static final Block WORM_BIN = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("worm_bin"),
            new WormBinBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(2.0F).sounds(BlockSoundGroup.WOOD)));
    public static final Block WORMY_DIRT = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("wormy_dirt"),
            new Block(AbstractBlock.Settings.copy(Blocks.DIRT)));

    // 1.3.x 新增：钓具箱（掉落由 TackleBoxBlock#getDroppedStacks 接管，见空战利品表）
    public static final Block TACKLE_BOX = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("tackle_box"),
            new TackleBoxBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(2.5F)
                    .sounds(BlockSoundGroup.WOOD)));

    // 1.3.x 新增：救生圈（可贴地/墙/天花板，含水）
    public static final Block LIFE_PRESERVER = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("life_preserver"),
            new LifePreserverBlock(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).strength(0.5F)
                    .sounds(BlockSoundGroup.WOOL).nonOpaque()));

    // 1.3.x 新增：鱼牌（挂墙展示鱼桶里的生物）
    public static final Block FISH_PLAQUE = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("fish_plaque"),
            new FishPlaqueBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(1.0F)
                    .sounds(BlockSoundGroup.WOOD).nonOpaque()));
    // 1.3.x 新增：便条方块（可贴墙 / 平铺，方块实体存正文）
    public static final Block NOTE_BLOCK = Registry.register(Registries.BLOCK, CrabbersDelightFabric.id("note_block"),
            new NoteBlock(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).strength(0.3F).nonOpaque()));
    private static Block storageBlock(String name) {
        return Registry.register(Registries.BLOCK, CrabbersDelightFabric.id(name),
                new Block(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD)));
    }

    // 空方法：强制类加载，触发所有静态注册
    public static void register() {
    }
}
