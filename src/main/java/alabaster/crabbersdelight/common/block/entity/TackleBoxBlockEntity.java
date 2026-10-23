package alabaster.crabbersdelight.common.block.entity;

import alabaster.crabbersdelight.common.block.container.TackleBoxMenu;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

// 钓具箱方块实体（对应 1.21 版 TackleBoxBlockEntity）：库存直接存在方块实体里，开界面时按槽同步。
// 注意：内容物还会同步给客户端 —— 假饵外观、以及"客户端自己判断装了哪个假饵"都依赖它。
public class TackleBoxBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    public static final Text TACKLE_BOX_NAME = Text.translatable("block.crabbersdelight.tackle_box");

    // readNbt 期间不刷方块更新，否则区块加载时每读一个槽就会发一个包
    private boolean suppressSync;

    public final TackleBoxItemHandler handler = new TackleBoxItemHandler() {
        @Override
        public void markDirty() {
            super.markDirty();
            TackleBoxBlockEntity.this.markDirty();
        }
    };

    public TackleBoxBlockEntity(BlockPos pos, BlockState state) {
        super(CDModBlockEntity.TACKLE_BOX, pos, state);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.suppressSync) {
            this.markSynced();
        }
    }

    // 把内容物推给客户端（对应 1.21 版靠 SyncableBlockEntity 自动同步）
    public void markSynced() {
        if (this.world != null && !this.world.isClient) {
            BlockState state = this.getCachedState();
            this.world.updateListeners(this.pos, state, state, Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("handler", this.handler.toNbtList());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.suppressSync = true;
        try {
            super.readNbt(nbt);
            if (nbt.contains("handler")) {
                this.handler.readNbtList(nbt.getList("handler", 10));
            }
        } finally {
            this.suppressSync = false;
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

    @Override
    public Text getDisplayName() {
        return TACKLE_BOX_NAME;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TackleBoxMenu(syncId, playerInventory, this.handler);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        // 客户端自建空库存，内容由槽位同步下发
    }
}
