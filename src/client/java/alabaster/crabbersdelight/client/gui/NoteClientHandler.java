package alabaster.crabbersdelight.client.gui;

import alabaster.crabbersdelight.common.utils.NoteClientHooks;
import alabaster.crabbersdelight.client.network.NoteClientNetwork;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

// 客户端实现：真正打开便条的编辑 / 阅读界面，并把保存/签名发到服务端。
public final class NoteClientHandler {
    private NoteClientHandler() {
    }

    public static void register() {
        NoteClientHooks.setOpener(new NoteClientHooks.Opener() {
            @Override
            public void openHandEdit(Hand hand, String text) {
                MinecraftClient.getInstance().setScreen(new NoteEditScreen(new NoteEditTarget() {
                    @Override
                    public void save(String value) {
                        NoteClientNetwork.sendEdit(hand, value);
                    }

                    @Override
                    public void sign(String title, String value) {
                        NoteClientNetwork.sendSign(hand, title, value);
                    }
                }, text));
            }

            @Override
            public void openBlockEdit(BlockPos pos, String text) {
                MinecraftClient.getInstance().setScreen(new NoteEditScreen(new NoteEditTarget() {
                    @Override
                    public void save(String value) {
                        NoteClientNetwork.sendBlockEdit(pos, value);
                    }

                    @Override
                    public void sign(String title, String value) {
                        NoteClientNetwork.sendBlockSign(pos, title, value);
                    }
                }, text));
            }

            @Override
            public void openReadScreen(String title, String text, String author) {
                MinecraftClient.getInstance().setScreen(new NoteReadScreen(title, text, author));
            }
        });
    }
}
