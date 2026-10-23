package alabaster.crabbersdelight.client.gui;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.item.NoteData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// 便条编辑界面（对应 1.21 版 NoteScreen）：左边写正文，点 Sign 后输入标题并最终签名。
public class NoteEditScreen extends Screen {
    private static final Identifier BACKGROUND = CrabbersDelightFabric.id("textures/gui/note.png");
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    // 图片 + 下方按钮的整体高度，用来把整块内容在屏幕里垂直居中
    private static final int CONTENT_HEIGHT = IMAGE_HEIGHT + 24;
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final int TEXT_COLOR = 0x000000;

    private final NoteEditTarget target;
    private final String initialText;

    private NotePageWidget textBox;
    private NotePageWidget titleBox;
    private ButtonWidget doneButton;
    private ButtonWidget signButton;
    private ButtonWidget cancelButton;
    private ButtonWidget finalizeButton;
    private boolean signing;
    private int imageLeft;
    private int imageTop;

    public NoteEditScreen(NoteEditTarget target, String initialText) {
        super(Text.translatable("gui.crabbersdelight.note.edit"));
        this.target = target;
        this.initialText = initialText == null ? "" : initialText;
    }

    @Override
    protected void init() {
        this.imageLeft = (this.width - IMAGE_WIDTH) / 2;
        this.imageTop = Math.max(2, (this.height - CONTENT_HEIGHT) / 2);
        int left = this.imageLeft + 36;

        this.textBox = new NotePageWidget(this.textRenderer, left, this.imageTop + 30, TEXT_WIDTH, TEXT_HEIGHT,
                this.initialText, NoteData.MAX_NOTE_LENGTH, false);
        this.addDrawableChild(this.textBox);

        this.titleBox = new NotePageWidget(this.textRenderer, left, this.imageTop + 48, TEXT_WIDTH, 9,
                "", NoteData.MAX_TITLE_LENGTH, true);
        this.addDrawableChild(this.titleBox);

        int buttonY = this.imageTop + IMAGE_HEIGHT + 2;
        this.doneButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"),
                        button -> saveAndClose())
                .dimensions(this.width / 2 - 100, buttonY, 98, 20).build());
        this.signButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.crabbersdelight.note.sign"),
                        button -> startSigning())
                .dimensions(this.width / 2 + 2, buttonY, 98, 20).build());
        this.cancelButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"),
                        button -> cancelSigning())
                .dimensions(this.width / 2 - 100, buttonY, 98, 20).build());
        this.finalizeButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("book.finalizeButton"),
                        button -> finalizeSign())
                .dimensions(this.width / 2 + 2, buttonY, 98, 20).build());

        updateVisibility();
        this.textBox.setEditing(true);
    }

    private void updateVisibility() {
        this.textBox.visible = !this.signing;
        this.doneButton.visible = !this.signing;
        this.signButton.visible = !this.signing;
        this.titleBox.visible = this.signing;
        this.cancelButton.visible = this.signing;
        this.finalizeButton.visible = this.signing;
        this.finalizeButton.active = this.signing && !this.titleBox.getValue().isBlank();
    }

    @Override
    public void tick() {
        if (this.signing) {
            this.finalizeButton.active = !this.titleBox.getValue().isBlank();
        }
    }

    private void startSigning() {
        this.signing = true;
        updateVisibility();
        this.textBox.setEditing(false);
        this.titleBox.setEditing(true);
    }

    private void cancelSigning() {
        this.signing = false;
        updateVisibility();
        this.titleBox.setEditing(false);
        this.textBox.setEditing(true);
    }

    private void saveAndClose() {
        this.target.save(this.textBox.getValue());
        this.close();
    }

    private void finalizeSign() {
        this.target.sign(this.titleBox.getValue(), this.textBox.getValue());
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND, this.imageLeft, this.imageTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        super.render(context, mouseX, mouseY, delta);

        if (this.signing) {
            int left = this.imageLeft + 36;

            Text label = Text.translatable("gui.crabbersdelight.note.editTitle");
            context.drawText(this.textRenderer, label,
                    left + (TEXT_WIDTH - this.textRenderer.getWidth(label)) / 2, this.imageTop + 32, TEXT_COLOR, false);

            if (this.client != null && this.client.player != null) {
                Text owner = Text.translatable("book.byAuthor", this.client.player.getName());
                context.drawText(this.textRenderer, owner,
                        left + (TEXT_WIDTH - this.textRenderer.getWidth(owner)) / 2, this.imageTop + 58, TEXT_COLOR, false);
            }

            context.drawTextWrapped(this.textRenderer,
                    Text.translatable("gui.crabbersdelight.note.finalizeWarning"),
                    left, this.imageTop + 80, TEXT_WIDTH, TEXT_COLOR);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.textBox.setEditing(this.textBox.visible && this.textBox.isMouseOver(mouseX, mouseY));
        this.titleBox.setEditing(this.titleBox.visible && this.titleBox.isMouseOver(mouseX, mouseY));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.textBox.visible && this.textBox.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.titleBox.visible && this.titleBox.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.textBox.visible && this.textBox.isEditing() && this.textBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.titleBox.visible && this.titleBox.isEditing() && this.titleBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
