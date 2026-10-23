package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.loot.FishingCatchLootFunction;
import alabaster.crabbersdelight.common.loot.FishSizeLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CDModLootFunctions {
    public static final LootFunctionType FISH_SIZE = Registry.register(
            Registries.LOOT_FUNCTION_TYPE, CrabbersDelightFabric.id("fish_size"),
            new LootFunctionType(new FishSizeLootFunction.Serializer()));

    public static final LootFunctionType FISHING_CATCH = Registry.register(
            Registries.LOOT_FUNCTION_TYPE, CrabbersDelightFabric.id("fishing_catch"),
            new LootFunctionType(new FishingCatchLootFunction.Serializer()));

    public static void register() {
    }
}
