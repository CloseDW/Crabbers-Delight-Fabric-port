package alabaster.crabbersdelight.common.registry;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.entity.CrabTrapBlockEntity;
import alabaster.crabbersdelight.common.block.entity.FishPlaqueBlockEntity;
import alabaster.crabbersdelight.common.block.entity.NoteBlockEntity;
import alabaster.crabbersdelight.common.block.entity.CDSignBlockEntity;
import alabaster.crabbersdelight.common.block.entity.CDHangingSignBlockEntity;
import alabaster.crabbersdelight.common.block.entity.WormBinBlockEntity;
import alabaster.crabbersdelight.common.block.entity.TackleBoxBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CDModBlockEntity {
    public static final BlockEntityType<CrabTrapBlockEntity> CRAB_TRAP = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("crab_trap"),
            BlockEntityType.Builder.create(CrabTrapBlockEntity::new, CDModBlocks.CRAB_TRAP).build(null));

    public static final BlockEntityType<CDSignBlockEntity> PALM_SIGN = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("palm_sign"),
            BlockEntityType.Builder.create(CDSignBlockEntity::new,
                    CDModBlocks.PALM_SIGN, CDModBlocks.PALM_WALL_SIGN).build(null));
    public static final BlockEntityType<CDHangingSignBlockEntity> HANGING_PALM_SIGN = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("palm_hanging_sign"),
            BlockEntityType.Builder.create(CDHangingSignBlockEntity::new,
                    CDModBlocks.PALM_HANGING_SIGN, CDModBlocks.PALM_WALL_HANGING_SIGN).build(null));

    public static final BlockEntityType<WormBinBlockEntity> WORM_BIN = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("worm_bin"),
            BlockEntityType.Builder.create(WormBinBlockEntity::new, CDModBlocks.WORM_BIN).build(null));

    public static final BlockEntityType<TackleBoxBlockEntity> TACKLE_BOX = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("tackle_box"),
            BlockEntityType.Builder.create(TackleBoxBlockEntity::new, CDModBlocks.TACKLE_BOX).build(null));

    public static final BlockEntityType<FishPlaqueBlockEntity> FISH_PLAQUE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("fish_plaque"),
            BlockEntityType.Builder.create(FishPlaqueBlockEntity::new, CDModBlocks.FISH_PLAQUE).build(null));
    public static final BlockEntityType<NoteBlockEntity> NOTE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, CrabbersDelightFabric.id("note_block"),
            BlockEntityType.Builder.create(NoteBlockEntity::new, CDModBlocks.NOTE_BLOCK).build(null));
    public static void register() {
    }
}
