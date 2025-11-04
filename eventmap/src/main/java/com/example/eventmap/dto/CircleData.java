package com.example.eventmap.dto;

import java.util.List;

public class CircleData {
    private String id;                 // 任意（必要なら使う）
    private String name;               // 任意（Circle名UIを作るなら）
    private String memo;               // 任意（メモUIを作るなら）
    private boolean checked;           // サークルのチェック状態（合計反映用）
    private String color;              // サークル基準色（#RRGGBB）
    private List<RectangleData> rectangles; // 矩形
    private List<GoodsData> goods;          // 商品

    public CircleData() {}

    // --- Getter / Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public List<RectangleData> getRectangles() { return rectangles; }
    public void setRectangles(List<RectangleData> rectangles) { this.rectangles = rectangles; }

    public List<GoodsData> getGoods() { return goods; }
    public void setGoods(List<GoodsData> goods) { this.goods = goods; }
}
