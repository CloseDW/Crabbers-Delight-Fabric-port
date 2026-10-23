package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.worldgen.WormyDirtFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class CDModFeatures {
    public static final Feature<DefaultFeatureConfig> WORMY_DIRT = Registry.register(
            Registries.FEATURE, CrabbersDelightFabric.id("wormy_dirt"), new WormyDirtFeature());

    public static void register() {
    }
}
