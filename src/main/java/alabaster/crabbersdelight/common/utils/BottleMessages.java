package alabaster.crabbersdelight.common.utils;

import net.minecraft.util.math.random.Random;

import java.util.List;

// 瓶中信的"内置留言池"（1.21 版是数据包注册表 bottle_message，1.20.1 没有这套机制，改为内置列表）。
// 文本里的 %player% 会被替换成打开者名字。
public final class BottleMessages {
    public record Message(String title, String text, String author) {
    }

    private static final List<Message> MESSAGES = List.of(
            new Message("To Whoever Finds This",
                    "The sea took my ship but not my hope. If you read this, the tide was kind to you.",
                    "A Castaway"),
            new Message("",
                    "Day 12. I have counted the stars twice and gotten two different answers. %player%, if this reaches you, do not trust the gulls.",
                    "The Lighthouse Keeper"),
            new Message("A Warning",
                    "Do not fish where the water is too still. Something down there remembers hooks.",
                    "An Old Sailor"),
            new Message("",
                    "I buried my treasure somewhere warm, then forgot which island. Enjoy the map, stranger. %player% owes me nothing.",
                    "Captain of the Salty Gull"),
            new Message("Recipes from the Deep",
                    "Kelp, three claws, a pinch of salt. Boil until the shells sing. Serve to friends only.",
                    "The Cook of Tidefall"),
            new Message("",
                    "The crabs walk sideways because forward was taken. %player%, remember that when the road narrows.",
                    "A Dockhand's Diary"),
            new Message("For the Patient Angler",
                    "Luck is not a fish. You cannot catch it, only wait near it long enough.",
                    "Unknown"),
            new Message("A Song Half-Remembered",
                    "Low tide, low tide, bring the little boats home. High tide, high tide, bring the big ones alone.",
                    "The Harbour Choir")
    );

    private BottleMessages() {
    }

    public static List<Message> all() {
        return MESSAGES;
    }

    public static Message random(Random random) {
        return MESSAGES.get(random.nextInt(MESSAGES.size()));
    }
}
