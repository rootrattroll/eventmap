package com.example.eventmap;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.util.Duration;

public class GoodsListController {
    @FXML
    private TextArea productPrice;
    @FXML
    private TextArea productName;     // ← 追加
    @FXML
    private CheckBox productCheck;    // ← 追加
    private CircleItemController circleItemController;
    public void setCircleItemController(CircleItemController circleItemController) {
        this.circleItemController = circleItemController;
    }
    @FXML
    private void initialize() {
        productPrice.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                productPrice.setText(oldValue);
                showPopup(productPrice, "整数を入力してください");
            } else if (newValue.startsWith("0")) {
                productPrice.setText(oldValue);
                showPopup(productPrice, "0から入力することはできません");
            } else if (newValue.length() > 7) {
                productPrice.setText(oldValue);
                showPopup(productPrice, "入力できるのは7桁までです");
            } else {
                // 価格が変更されたときに合計金額を再計算
                if (circleItemController != null) {
                    circleItemController.updateGoodsResult();
                }
            }
        });
    }
    private void showPopup(Node owner, String message) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: white; -fx-padding: 10;");

        popup.getContent().add(label);

        Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
        double popupX = bounds.getMinX();
        double popupY = bounds.getMinY() - label.getHeight() - 36;
        popup.show(owner, popupX, popupY);

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> popup.hide());
        delay.play();
    }
    public void setValues(String name, int price, boolean checked) {
        if (productName != null) productName.setText(name != null ? name : "");
        productPrice.setText(String.valueOf(price));
        if (productCheck != null) productCheck.setSelected(checked);
    }

    // 逆に取り出したい時用（今は使わなくてもOK）
    public String getName() { return productName != null ? productName.getText() : ""; }
    public int getPrice() {
        try { return Integer.parseInt(productPrice.getText().trim()); }
        catch (Exception e) { return 0; }
    }
    public boolean isChecked() { return productCheck != null && productCheck.isSelected();
    }
}