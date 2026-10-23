package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.entity.ThrownBottledNote;
import alabaster.crabbersdelight.common.entity.boat.CDBoatEntity;
import alabaster.crabbersdelight.common.entity.boat.CDChestBoatEntity;
import alabaster.crabbersdelight.common.entity.crab.CrabEntity;
import alabaster.crabbersdelight.common.entity.LifePreserverSeat;
import alabaster.crabbersdelight.common.fishingspot.FishingSpotEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.World;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CDModEntities {
    public static final EntityType<CrabEntity> CRAB = Registry.register(
            Registries.ENTITY_TYPE, CrabbersDelightFabric.id("crab"),
            EntityType.Builder.create(CrabEntity::new, SpawnGroup.CREATURE)
                    .setDimensions(0.7f, 0.7f)
                    .build("crabbersdelight:crab"));

    public static final EntityType<CDBoatEntity> MOD_BOAT = Registry.register(
            Registries.ENTITY_TYPE, CrabbersDelightFabric.id("mod_boat"),
            EntityType.Builder.create((EntityType<CDBoatEntity> type, World world) -> new CDBoatEntity(type, world), SpawnGroup.MISC)
                    .setDimensions(1.375f, 0.5625f).build("palm_boat"));
    public static final EntityType<CDChestBoatEntity> MOD_CHEST_BOAT = Registry.register(
            Registries.ENTITY_TYPE, CrabbersDelightFabric.id("mod_chest_boat"),
            EntityType.Builder.create((EntityType<CDChestBoatEntity> type, World world) -> new CDChestBoatEntity(type, world), SpawnGroup.MISC)
                    .setDimensions(1.375f, 0.5625f).build("palm_chest_boat"));

    // 1.3.x 新增：救生圈的隐形座位
    public static final EntityType<LifePreserverSeat> LIFE_PRESERVER_SEAT = Registry.register(
            Registries.ENTITY_TYPE, CrabbersDelightFabric.id("life_preserver_seat"),
            EntityType.Builder.create((EntityType<LifePreserverSeat> type, World world) -> new LifePreserverSeat(type, world), SpawnGroup.MISC)
                    .setDimensions(0.1f, 0.1f)
                    .disableSaving()
                    .maxTrackingRange(6)
                    .trackingTickInterval(40)
                    .build("life_preserver_seat"));

    // 1.3.x 新增：钓点（隐形标记实体，不存档）
    public static final EntityType<FishingSpotEntity> FISHING_SPOT = Registry.register(
            Registries.ENTITY_TYPE, CrabbersDelightFabric.id("fishing_spot"),
            EntityType.Builder.create((EntityType<FishingSpotEntity> type, World world) -> new FishingSpotEntity(type, world), SpawnGroup.MISC)
                    .setDimensions(0.1f, 0.1f)
                    .disableSaving()
                    .maxTrackingRange(6)
                    .trackingTickInterval(40)
                    .build("fishing_spot"));

    public static final EntityType<ThrownBottledNote> THROWN_BOTTLED_NOTE = Registry.register(
            Registries.ENTITY_TYPE, CrabbersDelightFabric.id("thrown_bottled_note"),
            EntityType.Builder.<ThrownBottledNote>create(ThrownBottledNote::new, SpawnGroup.MISC)
                    .setDimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10)
                    .build("thrown_bottled_note"));
    public static void register() {
    }
}
