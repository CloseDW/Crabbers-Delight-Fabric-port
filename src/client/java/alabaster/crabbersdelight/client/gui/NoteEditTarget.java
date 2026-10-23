package alabaster.crabbersdelight.client.gui;

// 便条编辑界面的"保存目标"：可能是手上的物品，也可能是墙上的便条方块。
public interface NoteEditTarget {
    void save(String text);

    void sign(String title, String text);
}
