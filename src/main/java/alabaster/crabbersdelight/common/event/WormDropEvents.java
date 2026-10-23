package alabaster.crabbersdelight.common.event;

import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.registry.CDModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;

// 挖掉任何带 dirt 标签的方块时 1% 掉蚯蚓（对应 1.21 版的 WormDropEvents）
public class WormDropEvents {
    private static final double WORM_CHANCE = 0.01;

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !Config.DIRT_DROPS_WORMS.get()) {
                return;
            }
            if (!state.isIn(BlockTags.DIRT)) {
                return;
            }
            if (world.getRandom().nextDouble() >= WORM_CHANCE) {
                return;
            }
            Block.dropStack(world, pos, new ItemStack(CDModItems.WORM));
        });
    }
}
