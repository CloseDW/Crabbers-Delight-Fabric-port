package alabaster.crabbersdelight.common.worldgen.placement;

import alabaster.crabbersdelight.common.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import java.util.stream.Stream;

// 配置过滤型 PlacementModifier（对应 1.21 版 CDConfigPlacementModifier）：
// 按配置开关决定这个放置流程是否放行；虫土还额外按 CHANCE_WORMY_DIRT 做 1/N 概率过滤。
// 用法（JSON）：{ "type": "crabbersdelight:config_filter", "config_key": "palm_trees" }
public class CDConfigFilterPlacementModifier extends PlacementModifier {
    public static final Codec<CDConfigFilterPlacementModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("config_key")
                            .xmap(ConfigOption::byKey, option -> option.key)
                            .forGetter(CDConfigFilterPlacementModifier::option)
            ).apply(instance, CDConfigFilterPlacementModifier::new));

    private final ConfigOption option;

    public CDConfigFilterPlacementModifier(ConfigOption option) {
        this.option = option;
    }

    public ConfigOption option() {
        return option;
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        return option.passes(random) ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<CDConfigFilterPlacementModifier> getType() {
        return CDPlacementModifiers.CONFIG_FILTER;
    }

    public enum ConfigOption {
        PALM_TREES("palm_trees"),
        SEASHELLS("seashells"),
        WORMY_DIRT("wormy_dirt");

        private final String key;

        ConfigOption(String key) {
            this.key = key;
        }

        public boolean passes(Random random) {
            switch (this) {
                case PALM_TREES:
                    return Config.GENERATE_PALM_TREES.get();
                case SEASHELLS:
                    return Config.GENERATE_SEASHELLS.get();
                case WORMY_DIRT:
                    if (!Config.GENERATE_WORMY_DIRT.get()) {
                        return false;
                    }
                    return random.nextInt(Math.max(1, Config.CHANCE_WORMY_DIRT.get())) == 0;
                default:
                    return true;
            }
        }

        public static ConfigOption byKey(String key) {
            for (ConfigOption option : values()) {
                if (option.key.equals(key)) {
                    return option;
                }
            }
            return PALM_TREES;
        }
    }
}
