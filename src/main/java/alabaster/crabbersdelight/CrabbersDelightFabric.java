package alabaster.crabbersdelight;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.registry.CDFoliagePlacerTypes;
import alabaster.crabbersdelight.common.registry.CDTrunkPlacerTypes;
import alabaster.crabbersdelight.common.registry.CDModBlocks;
import alabaster.crabbersdelight.common.registry.CDModCreativeTabs;
import alabaster.crabbersdelight.common.registry.CDModItems;
import alabaster.crabbersdelight.common.registry.CDModEntities;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import alabaster.crabbersdelight.common.registry.CDModMenus;
import alabaster.crabbersdelight.common.registry.CDModPotions;
import alabaster.crabbersdelight.common.registry.CDStateProviders;
import alabaster.crabbersdelight.common.registry.CDModFeatures;
import alabaster.crabbersdelight.common.worldgen.placement.CDPlacementModifiers;
import alabaster.crabbersdelight.common.event.VillagerTrade;
import alabaster.crabbersdelight.common.event.WormDropEvents;
import alabaster.crabbersdelight.common.event.ConchLootEvents;
import alabaster.crabbersdelight.common.event.LifePreserverEvents;
import alabaster.crabbersdelight.common.event.FishSizeEvents;
import alabaster.crabbersdelight.common.event.FishingGearEvents;
import alabaster.crabbersdelight.common.fishingspot.FishingSpotManager;
import alabaster.crabbersdelight.common.command.CDDebugCommand;
import alabaster.crabbersdelight.common.network.NoteNetworking;
import alabaster.crabbersdelight.common.network.LifePreserverNetworking;
import alabaster.crabbersdelight.common.event.NoteLootEvents;
import alabaster.crabbersdelight.common.registry.CDModRecipes;
import alabaster.crabbersdelight.common.registry.CDModLootFunctions;
import alabaster.crabbersdelight.common.entity.crab.CrabEntity;
import alabaster.crabbersdelight.common.item.CrabClawItem;
import alabaster.crabbersdelight.common.worldgen.CDWorldGen;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrabbersDelightFabric implements ModInitializer {
	public static final String MOD_ID = "crabbersdelight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 注册顺序：方块/物品 → 创造标签 → 配置
		CDModBlocks.register();
		CDModItems.register();
		CDModEntities.register();
		CDModBlockEntity.register();
		CDModMenus.register();
		CDModPotions.register();
		CDStateProviders.register();
		CDModCreativeTabs.register();
		Config.register();

		// 酿造：粗制药水 + 墨囊 → 墨盲药水（对应 Forge PotionBrewing.addMix）
		FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD,
				Ingredient.ofItems(Items.INK_SAC), CDModPotions.INKY_POTION);

		// 村民交易（渔民收购海鲜 / 流浪商人用珍珠换宝藏）
		VillagerTrade.register();
		CDModRecipes.register();

		// 生成器类型 + 世界生成
		CDTrunkPlacerTypes.register();
		CDFoliagePlacerTypes.register();
		CDModFeatures.register();
		CDPlacementModifiers.register();
		CDWorldGen.register();

		// 螃蟹：属性 + 生成规则 + 群系生成（数量/权重/开关读配置，默认 10/2/3）
		FabricDefaultAttributeRegistry.register(CDModEntities.CRAB, CrabEntity.createAttributes());
		SpawnRestriction.register(CDModEntities.CRAB, SpawnRestriction.Location.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CrabEntity::checkCrabSpawnRules);
		if (Config.GENERATE_CRAB_SPAWNS.get()) {
			BiomeModifications.addSpawn(
					BiomeSelectors.includeByKey(BiomeKeys.MANGROVE_SWAMP, BiomeKeys.SWAMP, BiomeKeys.BEACH,
							BiomeKeys.STONY_SHORE, BiomeKeys.SNOWY_BEACH),
					SpawnGroup.CREATURE, CDModEntities.CRAB,
					Config.CRAB_SPAWN_WEIGHT.get(), Config.CRAB_SPAWN_MIN.get(), Config.CRAB_SPAWN_MAX.get());
		}

		// 可燃性（Forge 的 isFlammable/getFlammability 在这里等价）
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.STRIPPED_PALM_LOG, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.STRIPPED_PALM_WOOD, 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_LEAVES, 60, 30);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_PLANKS, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_STAIRS, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_SLAB, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_FENCE, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_FENCE_GATE, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_DOOR, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_TRAPDOOR, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_PRESSURE_PLATE, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_BUTTON, 20, 5);
		FlammableBlockRegistry.getDefaultInstance().add(CDModBlocks.PALM_CABINET, 20, 5);

		// 斧头剥皮（Forge 的 getToolModifiedState + ToolActions 在这里等价）
		StrippableBlockRegistry.register(CDModBlocks.PALM_LOG, CDModBlocks.STRIPPED_PALM_LOG);
		StrippableBlockRegistry.register(CDModBlocks.PALM_WOOD, CDModBlocks.STRIPPED_PALM_WOOD);

		// 蟹钳：超距破坏方块时消耗耐久（等价 Forge BlockEvent.BreakEvent）
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) ->
				CrabClawItem.consumeOnBreak(player, pos));

		// 蟹钳：超距攻击实体时消耗耐久（等价 Forge AttackEntityEvent）
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			CrabClawItem.handleAttackEntity(player, world, entity);
			return ActionResult.PASS;
		});

		// 蚯蚓：挖泥土 1% 掉落（等价 Forge BlockEvent.BreakEvent）
		WormDropEvents.register();

		// 海螺：埋藏宝藏战利品池（等价 Forge 全局战利品修饰器）
		ConchLootEvents.register();

		// 救生圈：穿戴时的浮力
		LifePreserverEvents.register();

		// 鱼尺寸：战利品函数 + 钓鱼战利品表注入
		CDModLootFunctions.register();
		FishSizeEvents.register();

		// 钓鱼装备：鱼线甩得更远 / 磁吸与风暴饵的幸运 / 自动与倒刺饵的钓钩操作
		FishingGearEvents.register();

		// 钓点：定期在玩家附近水面生成
		FishingSpotManager.register();

		// 便条的网络同步（编辑 / 签名）
		NoteNetworking.register();
		LifePreserverNetworking.register();
		NoteLootEvents.register();

		// 调试自检命令：/cddebug gear | spot | config
		CDDebugCommand.register();

		LOGGER.info("Crabber's Delight (Fabric port) loaded!");
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
