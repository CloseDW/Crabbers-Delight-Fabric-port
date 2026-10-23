package alabaster.crabbersdelight.client.gui;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

// 已签名便条的阅读界面（对应 1.21 版 NoteReadScreen）。
public class NoteReadScreen extends Screen {
    private static final Identifier BACKGROUND = CrabbersDelightFabric.id("textures/gui/note.png");
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    // 图片 + 下方按钮的整体高度，用来把整块内容在屏幕里垂直居中
    private static final int CONTENT_HEIGHT = IMAGE_HEIGHT + 24;
    private static final int TEXT_WIDTH = 114;
    private static final int LINE_HEIGHT = 9;
    private static final int TEXT_COLOR = 0x000000;

    private final String title;
    private final String text;
    private final String author;

    private List<OrderedText> wrappedLines;
    private int imageLeft;
    private int imageTop;

    public NoteReadScreen(String title, String text, String author) {
        super(Text.translatable("gui.crabbersdelight.note.read"));
        this.title = title == null ? "" : title;
        this.text = text == null ? "" : text;
        this.author = author == null ? "" : author;
    }

    @Override
    protected void init() {
        this.imageLeft = (this.width - IMAGE_WIDTH) / 2;
        this.imageTop = Math.max(2, (this.height - CONTENT_HEIGHT) / 2);
        this.wrappedLines = this.textRenderer.wrapLines(Text.literal(this.text), TEXT_WIDTH);

        int buttonY = this.imageTop + IMAGE_HEIGHT + 2;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(this.width / 2 - 100, buttonY, 200, 20).build());
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

        int left = this.imageLeft + 36;
        int top = this.imageTop + 30;

        if (!this.title.isBlank()) {
            Text titleText = Text.literal(this.title);
            context.drawText(this.textRenderer, titleText,
                    left + (TEXT_WIDTH - this.textRenderer.getWidth(titleText)) / 2, this.imageTop + 16, TEXT_COLOR, false);
        }

        List<OrderedText> lines = this.wrappedLines == null ? List.of() : this.wrappedLines;
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(this.textRenderer, lines.get(i), left, top + i * LINE_HEIGHT, TEXT_COLOR, false);
        }

        if (!this.author.isBlank()) {
            context.drawText(this.textRenderer, "- " + this.author,
                    left, top + lines.size() * LINE_HEIGHT + 6, TEXT_COLOR, false);
        }
    }
}
