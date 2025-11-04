package com.example.eventmap.dto;

import java.util.List;

public class EventData {
    private String eventName;             // イベント名
    private List<ImageData> images;       // 画像リスト
    private List<CircleData> circles;     // サークルリスト

    public EventData() {}

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public List<ImageData> getImages() { return images; }
    public void setImages(List<ImageData> images) { this.images = images; }

    public List<CircleData> getCircles() { return circles; }
    public void setCircles(List<CircleData> circles) { this.circles = circles; }
}
