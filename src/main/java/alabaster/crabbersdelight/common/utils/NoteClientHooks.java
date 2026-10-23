package alabaster.crabbersdelight.common.utils;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

// 主源集（common）不能直接引用客户端界面（splitEnvironmentSourceSets），
// 所以这里放"打开便条界面"的钩子，由客户端源集在初始化时接管。
public final class NoteClientHooks {
    private NoteClientHooks() {
    }

    public interface Opener {
        void openHandEdit(Hand hand, String text);

        void openBlockEdit(BlockPos pos, String text);

        void openReadScreen(String title, String text, String author);
    }

    private static Opener opener;

    public static void setOpener(Opener implementation) {
        opener = implementation;
    }

    public static void openEdit(Hand hand, String text) {
        if (opener != null) {
            opener.openHandEdit(hand, text);
        }
    }

    public static void openBlockEdit(BlockPos pos, String text) {
        if (opener != null) {
            opener.openBlockEdit(pos, text);
        }
    }

    public static void openRead(String title, String text, String author) {
        if (opener != null) {
            opener.openReadScreen(title, text, author);
        }
    }
}
