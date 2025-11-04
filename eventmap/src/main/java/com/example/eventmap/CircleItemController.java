package com.example.eventmap;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import com.example.eventmap.dto.CircleData;
import com.example.eventmap.dto.RectangleData;
import com.example.eventmap.dto.GoodsData;
import javafx.scene.paint.Color;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.Node;
import com.example.eventmap.dto.GoodsData;
import javafx.scene.paint.Color;

public class CircleItemController {
    @FXML
    public TextArea circleName;
    @FXML
    public TextArea circleComment;
    @FXML
    private ColorPicker pickColor;
    @FXML
    private TextField colorHeight, colorWidth;
    @FXML
    private Button colorButton, shapeClear, circleDelete, toggleButton;
    @FXML
    private CheckBox circleCheck;
    @FXML
    private VBox detailsBox;
    @FXML
    private Label goodsResult;
    private AnchorPane parentPane;
    private TotalChangeListener totalChangeListener;
    private Runnable deleteButtonAction;
    private EventViewController parentController;

    public void setParentController(EventViewController parentController) {
        this.parentController = parentController;
    }

    public void setCircleNameText(String text) {
        if (circleName != null) circleName.setText(text != null ? text : "");
    }
    public void setCircleCommentText(String text) {
        if (circleComment != null) circleComment.setText(text != null ? text : "");
    }

    public String getCircleNameText() {
        return circleName != null ? circleName.getText() : "";
    }
    public String getCircleCommentText() {
        return circleComment != null ? circleComment.getText() : "";
    }
    public interface OnShapeGenerated {
        void onShapeGenerated(Rectangle shape);
    }
    private OnShapeGenerated onShapeGenerated;
    public void setOnShapeGenerated(OnShapeGenerated onShapeGenerated) {
        this.onShapeGenerated = onShapeGenerated;
    }
    // 親ノードを設定するメソッド
    public void setParentPane(AnchorPane parentPane) {
        this.parentPane = parentPane;
    }
    // 親ノードを取得するメソッド
    public AnchorPane getParentPane() {
        return parentPane;
    }
    // 親ノードを保持
    public boolean isCircleCheckSelected() {
        return circleCheck.isSelected();
    }
    public void setDeleteButtonAction(Runnable action) {
        this.deleteButtonAction = action;
    }
    private final List<Rectangle> shapes = new ArrayList<>();
    private final List<HBox> hboxList = new ArrayList<>();
    public interface TotalChangeListener {
        void onTotalChanged();
    }
    public void setTotalChangeListener(TotalChangeListener listener) {
        this.totalChangeListener = listener;
    }//これなに？　A　合計金額が変わった時の通知をするリスナ。受け取るためにthisでここに置いてる
    @FXML//ここはまあぁ初期化とかリスナセットだから無視で良いよ
    private void initialize() {// 色ピッカーの値変更リスナーを追加。observable: ObservableValue<Color>, oldValue/newValue: Color
        pickColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateAllShapesColor(newValue);// 図形群の塗り色を一括更新
                });
        shapeClear.setOnAction(event -> {
            onCircleDelete();
                });
        circleCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleTotalChange();
            colorButtonVisible(newValue);
            updateRectangleVisibility(newValue);
        });
        circleDelete.setOnAction(event -> {
            onCircleDelete();
            if (deleteButtonAction != null) {
                deleteButtonAction.run();  // 削除アクションを実行して親に伝える
            }
        });
    }
    // 渡された色(Color)で、保持中の全Rectangleの塗りを更新する
    private void updateAllShapesColor(Color newColor) {
        for (Rectangle shape : shapes) {
            shape.setFill(newColor); // 各図形の色を変更
        }
    }
    private void onCircleDelete() {
        EventViewController eventViewController = EventViewController.getInstance();//親コントローラーのインスタンス持ってきて＊まかり間違ってもローダーなんて回さないように！！
        if (eventViewController != null) {
            eventViewController.deleteShapesFromVBox(shapes);
            shapes.clear(); // 四角形入ってるリストを空にする。
        }
    }
    private void handleTotalChange() {
        if (totalChangeListener != null) {
            totalChangeListener.onTotalChanged();
        }
    }
    private void colorButtonVisible(boolean isChecked) {
        parentController.setButtonState(colorButton, isChecked);
    }
    private void updateRectangleVisibility(boolean isChecked) {
        // チェックボックスの状態に応じて、リスト内のRectangleの表示/非表示を設定
        for (Rectangle rectangle : shapes) {
            rectangle.setVisible(isChecked);
        }
    }
    @FXML
    private void selectColor() {
        if (colorWidth.getText().isEmpty() || colorHeight.getText().isEmpty()) {
            System.out.println("Width または Height が未入力です。処理を中断します。");
            return;
        }
        Color selectedColor = pickColor.getValue();
        double width = Double.parseDouble(colorWidth.getText());
        double height = Double.parseDouble(colorHeight.getText());

        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setFill(selectedColor);
        shapes.add(rectangle);

        // 図形を生成するために、親のイベントビューコントローラーに通知
        if (onShapeGenerated != null) {
            onShapeGenerated.onShapeGenerated(rectangle);
        }
    }
    public List<Rectangle> getShapes() {
        return shapes;
    }
    @FXML
    private void addHBox() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GoodsList.fxml"));
            HBox hbox = loader.load();
            GoodsListController controller = loader.getController();
            hbox.setUserData(controller);  // HBoxにコントローラを設定

            // CircleItemControllerをGoodsListControllerに渡す
            controller.setCircleItemController(this);

            CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateGoodsResult());

            Button deleteButton = (Button) hbox.getChildren().get(4);
            deleteButton.setOnAction(event -> removeHBox(hbox));

            hboxList.add(hbox);
            detailsBox.getChildren().add(hbox);

            updateToggleButtonVisibility();
            toggleButton.setText("詳細を隠す");
            detailsBox.setVisible(true);
            detailsBox.setManaged(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void removeHBox(HBox hboxToRemove) {
        detailsBox.getChildren().remove(hboxToRemove);
        hboxList.remove(hboxToRemove); // リストからも削除
        updateToggleButtonVisibility();
        updateGoodsResult();
    }
    public void updateGoodsResult() {
        int total = hboxList.stream()
                .filter(hbox -> {
                    CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
                    return checkBox.isSelected();
                })
                .mapToInt(hbox -> {
                    TextArea productPriceField = (TextArea) hbox.getChildren().get(2);
                    try {
                        return Integer.parseInt(productPriceField.getText().trim());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();
        goodsResult.setText(String.format("合計金額: %d円", total));
        // リスナーに通知
        if (totalChangeListener != null) {
            totalChangeListener.onTotalChanged();
        }
    }
    @FXML
    private void productHidden() {
        boolean isVisible = detailsBox.isVisible();
        detailsBox.setVisible(!isVisible);
        detailsBox.setManaged(!isVisible);
        toggleButton.setText(isVisible ? "詳細を表示" : "詳細を隠す");
    }
    private void updateToggleButtonVisibility() {
        toggleButton.setVisible(!hboxList.isEmpty());
    }
    public String getGoodsResultText() {
        return goodsResult.getText();
    }

    public CircleData toData() {
        CircleData cd = new CircleData();

        // チェック状態（サークル単位）
        cd.setChecked(circleCheck.isSelected());
        cd.setName(getCircleNameText());
        cd.setMemo(getCircleCommentText());

        // サークルの色（PickColor）を #RRGGBB へ
        if (pickColor != null && pickColor.getValue() != null) {
            cd.setColor(toHex(pickColor.getValue()));
        }

        // 矩形
        List<RectangleData> rectList = new ArrayList<>();
        for (Rectangle r : shapes) {
            RectangleData rd = new RectangleData();
            rd.setX(r.getX());
            rd.setY(r.getY());
            rd.setWidth(r.getWidth());
            rd.setHeight(r.getHeight());
            // 個々の矩形の実色（fill）があるならそれを優先
            if (r.getFill() instanceof Color c) {
                rd.setColor(toHex(c));
            } else {
                rd.setColor(cd.getColor()); // フォールバック
            }
            // どの画像か（親のGroup→画像ID）
            String imageId = parentController.getImageIdFor(r);
            rd.setImageId(imageId);

            rectList.add(rd);
        }
        cd.setRectangles(rectList);

        // 商品（hboxList は GoodsList.fxml の HBox）
        List<GoodsData> goods = new ArrayList<>();
        for (HBox h : hboxList) {
            // 0: CheckBox, 1: 商品名 TextArea, 2: 価格 TextArea, 3: "円" Text, 4: 削除ボタン
            CheckBox cb = (CheckBox) h.getChildren().get(0);
            TextArea nameArea = (TextArea) h.getChildren().get(1);
            TextArea priceArea = (TextArea) h.getChildren().get(2);

            GoodsData gd = new GoodsData();
            gd.setProductName(nameArea.getText().trim());
            try {
                gd.setPrice(Integer.parseInt(priceArea.getText().trim()));
            } catch (Exception e) {
                gd.setPrice(0);
            }
            gd.setChecked(cb.isSelected());
            goods.add(gd);
        }
        cd.setGoods(goods);

        return cd;
    }

    private static String toHex(Color c) {
        int r = (int)Math.round(c.getRed()*255);
        int g = (int)Math.round(c.getGreen()*255);
        int b = (int)Math.round(c.getBlue()*255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public void setCircleChecked(boolean checked) {
        circleCheck.setSelected(checked);
        // 可視/非表示も既存ロジックが反応するのでOK
    }
    public void setCircleColorHex(String hex) {
        if (hex == null) return;
        try {
            Color c = Color.web(hex);
            pickColor.setValue(c);
            updateAllShapesColor(c); // 既存の矩形にも反映（復元初期は空でもOK）
        } catch (Exception ignore) {}
    }

    // 復元：商品1件をUIに追加して値を流し込む
    public void addGoodsFromData(GoodsData gd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GoodsList.fxml"));
            HBox hbox = loader.load();

            GoodsListController controller = loader.getController();
            hbox.setUserData(controller);
            controller.setCircleItemController(this);
            controller.setValues(gd.getProductName(), gd.getPrice(), gd.isChecked());

            CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
            checkBox.selectedProperty().addListener((obs, ov, nv) -> updateGoodsResult());

            Button deleteButton = (Button) hbox.getChildren().get(4);
            deleteButton.setOnAction(event -> removeHBox(hbox));

            hboxList.add(hbox);
            detailsBox.getChildren().add(hbox);
            updateToggleButtonVisibility();

            toggleButton.setText("詳細を隠す");
            detailsBox.setVisible(true);
            detailsBox.setManaged(true);

            // 金額ラベル再計算
            updateGoodsResult();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 復元：矩形リストに直接追加（座標・サイズ・色は呼び出し側でセットしてから渡す）
    public void addRectangle(Rectangle rect) {
        shapes.add(rect);
    }


}