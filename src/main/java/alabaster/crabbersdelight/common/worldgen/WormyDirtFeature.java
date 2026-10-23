package alabaster.crabbersdelight.common.worldgen;

import alabaster.crabbersdelight.common.registry.CDModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

// 虫土生成（对应 1.21 版 WormyDirtFeature）：
// 从地表起点向下 8 格找泥土，随机挑一个，然后沿随机水平方向把 1–3 个泥土换成虫土。
// 开关与 1/N 频率由 placed_feature 里的 crabbersdelight:config_filter 负责，这里只管生成算法。
public class WormyDirtFeature extends Feature<DefaultFeatureConfig> {
    private static final int MIN_GROUP_SIZE = 1;
    private static final int MAX_GROUP_SIZE = 3;
    private static final int SEARCH_DEPTH_BELOW_ORIGIN = 8;

    public WormyDirtFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Random random = context.getRandom();
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        int minY = Math.max(world.getBottomY(), origin.getY() - SEARCH_DEPTH_BELOW_ORIGIN);

        BlockPos chosen = null;
        int dirtSeen = 0;
        BlockPos.Mutable cursor = origin.mutableCopy();
        for (int y = origin.getY(); y >= minY; y--) {
            cursor.setY(y);
            if (world.getBlockState(cursor).isOf(Blocks.DIRT)) {
                dirtSeen++;
                if (random.nextInt(dirtSeen) == 0) {
                    chosen = cursor.toImmutable();
                }
            }
        }

        if (chosen == null) {
            return false;
        }

        placeGroup(world, chosen, random);
        return true;
    }

    private void placeGroup(StructureWorldAccess world, BlockPos start, Random random) {
        int groupSize = MIN_GROUP_SIZE + random.nextInt(MAX_GROUP_SIZE - MIN_GROUP_SIZE + 1);
        BlockPos.Mutable cursor = start.mutableCopy();
        for (int i = 0; i < groupSize; i++) {
            if (world.getBlockState(cursor).isOf(Blocks.DIRT)) {
                world.setBlockState(cursor, CDModBlocks.WORMY_DIRT.getDefaultState(), Block.NOTIFY_ALL);
            }
            Direction direction = Direction.Type.HORIZONTAL.random(random);
            cursor.move(direction);
        }
    }
}
