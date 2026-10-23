package alabaster.crabbersdelight.common.block.entity;

import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

// 鱼牌方块实体（对应 1.21 版 FishPlaqueBlockEntity）：
// 记录"展示的是哪种生物"、生物 NBT（颜色/大小等）、以及当初用来挂它的鱼桶。
public class FishPlaqueBlockEntity extends BlockEntity {
    @Nullable
    private Identifier entityTypeId = null;
    private NbtCompound entityData = new NbtCompound();
    private ItemStack sourceBucket = ItemStack.EMPTY;

    public FishPlaqueBlockEntity(BlockPos pos, BlockState state) {
        super(CDModBlockEntity.FISH_PLAQUE, pos, state);
    }

    public boolean hasFish() {
        return entityTypeId != null;
    }

    @Nullable
    public EntityType<?> getEntityType() {
        return entityTypeId == null ? null : Registries.ENTITY_TYPE.get(entityTypeId);
    }

    @Nullable
    public Identifier getEntityTypeId() {
        return entityTypeId;
    }

    public NbtCompound getEntityData() {
        return entityData;
    }

    public ItemStack getSourceBucket() {
        return sourceBucket;
    }

    public void setFishData(EntityType<?> type, NbtCompound data, ItemStack sourceBucket) {
        this.entityTypeId = Registries.ENTITY_TYPE.getId(type);
        this.entityData = data == null ? new NbtCompound() : data.copy();
        this.sourceBucket = sourceBucket == null ? ItemStack.EMPTY : sourceBucket.copy();
        this.markDirty();
    }

    public void clearFish() {
        this.entityTypeId = null;
        this.entityData = new NbtCompound();
        this.sourceBucket = ItemStack.EMPTY;
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (entityTypeId != null) {
            nbt.putString("EntityType", entityTypeId.toString());
            if (!entityData.isEmpty()) {
                nbt.put("EntityData", entityData.copy());
            }
            if (!sourceBucket.isEmpty()) {
                NbtCompound bucketNbt = new NbtCompound();
                sourceBucket.writeNbt(bucketNbt);
                nbt.put("SourceBucket", bucketNbt);
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("EntityType")) {
            this.entityTypeId = Identifier.tryParse(nbt.getString("EntityType"));
            this.entityData = nbt.contains("EntityData") ? nbt.getCompound("EntityData").copy() : new NbtCompound();
            this.sourceBucket = nbt.contains("SourceBucket")
                    ? ItemStack.fromNbt(nbt.getCompound("SourceBucket")) : ItemStack.EMPTY;
        } else {
            this.entityTypeId = null;
            this.entityData = new NbtCompound();
            this.sourceBucket = ItemStack.EMPTY;
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
