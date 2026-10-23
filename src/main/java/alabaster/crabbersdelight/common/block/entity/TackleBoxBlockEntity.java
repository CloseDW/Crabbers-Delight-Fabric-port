package alabaster.crabbersdelight.common.block.entity;

import alabaster.crabbersdelight.common.block.container.TackleBoxMenu;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

// 钓具箱方块实体（对应 1.21 版 TackleBoxBlockEntity）：库存直接存在方块实体里，开界面时按槽同步。
public class TackleBoxBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    public static final Text TACKLE_BOX_NAME = Text.translatable("block.crabbersdelight.tackle_box");

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
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("handler", this.handler.toNbtList());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("handler")) {
            this.handler.readNbtList(nbt.getList("handler", 10));
        }
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
