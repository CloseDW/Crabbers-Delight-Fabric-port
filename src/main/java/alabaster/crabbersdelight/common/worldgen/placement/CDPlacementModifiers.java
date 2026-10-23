package alabaster.crabbersdelight.common.worldgen.placement;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class CDPlacementModifiers {
    public static final PlacementModifierType<CDConfigFilterPlacementModifier> CONFIG_FILTER = Registry.register(
            Registries.PLACEMENT_MODIFIER_TYPE, CrabbersDelightFabric.id("config_filter"),
            () -> CDConfigFilterPlacementModifier.CODEC);

    public static void register() {
    }
}
