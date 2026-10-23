package alabaster.crabbersdelight.client.gui;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.block.container.TackleBoxMenu;
import alabaster.crabbersdelight.common.block.entity.inventory.TackleBoxItemHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// 钓具箱界面（对应 1.21 版 TackleBoxGUI）：空槽上显示"假饵/鱼线/鱼饵"提示。
public class TackleBoxGUI extends HandledScreen<TackleBoxMenu> {
    private static final Identifier TACKLE_BOX_GUI = CrabbersDelightFabric.id("textures/gui/tackle_box.png");

    public TackleBoxGUI(TackleBoxMenu menu, PlayerInventory playerInv, Text title) {
        super(menu, playerInv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 177;
        this.playerInventoryTitleY = 74;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TACKLE_BOX_GUI);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TACKLE_BOX_GUI, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        super.drawMouseoverTooltip(context, mouseX, mouseY);

        Slot slot = this.focusedSlot;
        if (slot != null && slot.getStack().isEmpty()) {
            Text label = slotLabel(slot.getIndex());
            if (label != null) {
                context.drawTooltip(this.textRenderer, label, mouseX, mouseY);
            }
        }
    }

    private static Text slotLabel(int slotIndex) {
        if (slotIndex == TackleBoxItemHandler.LURE_SLOT) {
            return Text.translatable("gui.crabbersdelight.tackle_box.lure_slot");
        }
        if (slotIndex == TackleBoxItemHandler.LINE_SLOT) {
            return Text.translatable("gui.crabbersdelight.tackle_box.line_slot");
        }
        if (slotIndex == TackleBoxItemHandler.BAIT_SLOT_1 || slotIndex == TackleBoxItemHandler.BAIT_SLOT_2) {
            return Text.translatable("gui.crabbersdelight.tackle_box.bait_slot");
        }
        return null;
    }
}
