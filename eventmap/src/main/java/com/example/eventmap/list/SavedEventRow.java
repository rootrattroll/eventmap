package com.example.eventmap.list;

public class SavedEventRow {
    private int serialNumber;
    private String name;
    private int totalAmount;
    private String comment;   // 今は空でもOK
    private String filePath;  // ← ロードに使う

    public SavedEventRow(int serialNumber, String name, int totalAmount, String comment, String filePath) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.totalAmount = totalAmount;
        this.comment = comment;
        this.filePath = filePath;
    }

    public int getSerialNumber() { return serialNumber; }
    public String getName() { return name; }
    public int getTotalAmount() { return totalAmount; }
    public String getComment() { return comment; }
    public String getFilePath() { return filePath; }
}
