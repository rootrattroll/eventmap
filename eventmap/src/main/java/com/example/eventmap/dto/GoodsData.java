package com.example.eventmap.dto;

public class GoodsData {
    private String productName;   // 商品名
    private int price;            // 価格
    private boolean checked;      // チェック状態

    public GoodsData() {}

    public GoodsData(String productName, int price, boolean checked) {
        this.productName = productName;
        this.price = price;
        this.checked = checked;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}

