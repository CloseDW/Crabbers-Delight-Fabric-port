package alabaster.crabbersdelight.common.item;

import net.minecraft.util.Formatting;

// 鱼尺寸档位（对应 1.21 版 FishSize）：数值完全照搬，1.20.1 里用物品 NBT 存，不用 DataComponent。
public enum FishSize {
    TINY("tiny", 0.25f, Formatting.RED),
    SMALL("small", 0.5f, Formatting.YELLOW),
    REGULAR("regular", 1.0f, Formatting.WHITE),
    LARGE("large", 1.5f, Formatting.GREEN),
    HUGE("huge", 2.0f, Formatting.GOLD);

    private final String serializedName;
    private final float multiplier;
    private final Formatting color;

    FishSize(String serializedName, float multiplier, Formatting color) {
        this.serializedName = serializedName;
        this.multiplier = multiplier;
        this.color = color;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public Formatting getColor() {
        return color;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static FishSize byName(String name) {
        for (FishSize size : values()) {
            if (size.serializedName.equals(name)) {
                return size;
            }
        }
        return REGULAR;
    }
}
