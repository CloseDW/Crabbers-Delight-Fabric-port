package alabaster.crabbersdelight.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

// 便条的多行文本输入框（对应 1.21 版 NotePageWidget）：
// 1.20.1 没有现成的多行输入组件，这里自己维护一个字符串 + 换行渲染 + 光标闪烁。
public class NotePageWidget extends ClickableWidget {
    private static final int LINE_HEIGHT = 9;
    private static final int TEXT_COLOR = 0x000000;

    private final TextRenderer textRenderer;
    private final int maxLength;
    private final boolean centered;

    private String text;
    private boolean editing;

    public NotePageWidget(TextRenderer textRenderer, int x, int y, int width, int height,
                          String initialText, int maxLength, boolean centered) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.maxLength = maxLength;
        this.centered = centered;
        this.text = initialText == null ? "" : initialText;
    }

    public String getValue() {
        return this.text;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        this.setFocused(editing);
    }

    public boolean isEditing() {
        return this.editing;
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal(this.text), this.getWidth());
        int y = this.getY();
        int lastLineX = this.centered ? this.getX() + this.getWidth() / 2 : this.getX();
        int lastLineWidth = 0;

        for (OrderedText line : lines) {
            int lineWidth = this.textRenderer.getWidth(line);
            int x = this.centered ? this.getX() + (this.getWidth() - lineWidth) / 2 : this.getX();
            context.drawText(this.textRenderer, line, x, y, TEXT_COLOR, false);
            lastLineX = x;
            lastLineWidth = lineWidth;
            y += LINE_HEIGHT;
        }

        if (this.editing && System.currentTimeMillis() / 300L % 2L == 0L) {
            int cursorX = lastLineX + lastLineWidth;
            int cursorY = this.getY() + Math.max(0, lines.size() - 1) * LINE_HEIGHT;
            context.drawText(this.textRenderer, "_", cursorX, cursorY, TEXT_COLOR, false);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.editing) {
            return false;
        }
        if (chr >= ' ' && this.text.length() < this.maxLength) {
            this.text = this.text + chr;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.editing) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!this.text.isEmpty()) {
                this.text = this.text.substring(0, this.text.length() - 1);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.text.length() < this.maxLength) {
                this.text = this.text + "\n";
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setEditing(true);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.translatable("gui.crabbersdelight.note.edit"));
    }
}
