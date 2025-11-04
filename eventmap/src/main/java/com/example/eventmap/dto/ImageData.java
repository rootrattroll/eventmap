package com.example.eventmap.dto;

public class ImageData {
    private String id;        // 一意のID（復元用）
    private String filePath;  // ローカルまたはクラウドの画像パス
    private double width;     // 表示幅
    private double height;    // 表示高さ

    public ImageData() {}

    public ImageData(String id, String filePath, double width, double height) {
        this.id = id;
        this.filePath = filePath;
        this.width = width;
        this.height = height;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
