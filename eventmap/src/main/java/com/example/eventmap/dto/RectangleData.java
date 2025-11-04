package com.example.eventmap.dto;

public class RectangleData {
    private String id;       // 任意（必要なら使う）
    private String imageId;  // ← どの画像に属しているか
    private double x;
    private double y;
    private double width;
    private double height;
    private String color;    // #RRGGBB

    public RectangleData() {}

    // --- Getter / Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
