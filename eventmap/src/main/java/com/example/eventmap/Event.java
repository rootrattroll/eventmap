package com.example.eventmap;

public class Event {
    private int serialNumber;
    private String name;
    private double totalAmount;
    private String coment;

    public Event(int serialNumber, String name, double totalAmount, String coment) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.totalAmount = totalAmount;
        this.coment = coment;
    }

    // Getter と Setter メソッドを追加
    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getComent() {
        return coment;
    }

    public void setComent(String coment) {
        this.coment = coment;
    }
}
