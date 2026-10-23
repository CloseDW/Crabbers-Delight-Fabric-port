package alabaster.crabbersdelight.common.worldgen;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.registry.CDStateProviders;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class CDWorldGen {
    public static void register() {
        CDStateProviders.register();

        // 棕榈树生成在沙滩（BEACH）群系的植被阶段
        // 特征/放置数据由数据包 JSON 定义：
        //   data/crabbersdelight/worldgen/configured_feature/palm.json
        //   data/crabbersdelight/worldgen/placed_feature/palm_placed.json
        RegistryKey<PlacedFeature> palmPlacedKey = RegistryKey.of(
                RegistryKeys.PLACED_FEATURE, CrabbersDelightFabric.id("palm_placed"));

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.BEACH),
                GenerationStep.Feature.VEGETAL_DECORATION,
                palmPlacedKey);

        // 贝壳：沙滩（干） + 海洋（水下），对应 Forge 的 CDBiomeModifiers
        RegistryKey<PlacedFeature> seashellsPlacedKey = RegistryKey.of(
                RegistryKeys.PLACED_FEATURE, CrabbersDelightFabric.id("seashells_placed"));
        RegistryKey<PlacedFeature> seashellsUnderwaterPlacedKey = RegistryKey.of(
                RegistryKeys.PLACED_FEATURE, CrabbersDelightFabric.id("seashells_placed_underwater"));

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.BEACH),
                GenerationStep.Feature.VEGETAL_DECORATION,
                seashellsPlacedKey);
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.OCEAN, BiomeKeys.WARM_OCEAN,
                        BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN),
                GenerationStep.Feature.VEGETAL_DECORATION,
                seashellsUnderwaterPlacedKey);

        // 虫土：主世界地下矿物阶段（对应 NEO 的 add_wormy_dirt biome modifier）
        RegistryKey<PlacedFeature> wormyDirtPlacedKey = RegistryKey.of(
                RegistryKeys.PLACED_FEATURE, CrabbersDelightFabric.id("wormy_dirt_placed"));
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                wormyDirtPlacedKey);
    }
}
