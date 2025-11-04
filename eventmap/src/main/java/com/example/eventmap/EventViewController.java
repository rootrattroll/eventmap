package com.example.eventmap;

import com.example.eventmap.dto.EventData;
import com.example.eventmap.dto.CircleData;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.UUID;
import com.example.eventmap.dto.*;
import java.nio.file.*;
import com.example.eventmap.util.AppPaths;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventViewController {
    @FXML
    private Text eventNameText;
    @FXML
    private TextField eventNameField;
    @FXML
    private Button backButton, editButton, newCircle, imageButton, removeImageButton,dataButton, fullScreen;
    @FXML
    private ScrollPane scrollPaneLeft;
    @FXML
    private  BorderPane circleList;
    @FXML
    private VBox vboxMap, circleBox;
    @FXML
    private Label totalResult;
    private static EventViewController instance;
    private boolean isEditMode = false;
    private ImageView selectedImageView = null;
    private final List<CircleItemController> circleControllers = new ArrayList<>();
    private final List<ImageView> imageViews = new ArrayList<>();
    public EventViewController() {
        instance = this;
    }
    public static EventViewController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        scrollPaneLeft.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            adjustImageWidths(newWidth.doubleValue());
        });
        totalResult.setText("総合計: 0円\n500円玉必要枚数: 0枚\n1000円札必要枚数: 0枚");
    }
    @FXML
    private void goToEventList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EventList.fxml"));
            Parent root = loader.load();
            // 現在のステージを取得
            Stage stage = (Stage) backButton.getScene().getWindow();

            stage.setTitle("イベントリスト");

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            editButton.setText("保存");
            setButtonState(imageButton, true);
            setButtonState(removeImageButton, true);
            setButtonState(newCircle, true);

            // eventNameTextをTextFieldに切り替える
            eventNameField.setText(eventNameText.getText());
            eventNameText.setVisible(false);
            eventNameText.setManaged(false);
            eventNameField.setVisible(true);
            eventNameField.setManaged(true);
        } else {
            editButton.setText("編集");
            setButtonState(newCircle, false);
            setButtonState(imageButton, false);
            setButtonState(removeImageButton, false);

            // 編集内容をeventNameTextに反映させる
            eventNameText.setText(eventNameField.getText());
            eventNameText.setVisible(true);
            eventNameText.setManaged(true);
            eventNameField.setVisible(false);
            eventNameField.setManaged(false);

            // 選択中の画像または図形を解除
            selectedImageView = null; // selectedImageViewをリセット
            System.out.println("選択を解除しました。");
        }
    }
    @FXML
    private void addCircle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CircleItem.fxml"));
            AnchorPane newCircle = loader.load();

            CircleItemController controller = loader.getController();
            controller.setParentPane(newCircle);
            controller.setParentController(this);

            circleControllers.add(controller);

            controller.setOnShapeGenerated(shape -> {
                // 親クラスに値突っ込んでUIに追加
                if (selectedImageView != null) {
                    // ImageViewの属するGroupを取得
                    Group parentGroup = (Group) selectedImageView.getParent();
                    if (parentGroup != null) {
                        double xPosition = (selectedImageView.getFitWidth() - shape.getWidth()) / 2;
                        double yPosition = (selectedImageView.getFitHeight() - shape.getHeight()) / 2;

                        // レクタングルの位置取っておく
                        shape.setX(xPosition);
                        shape.setY(yPosition);

                        selectedImageView.fitWidthProperty().addListener((observable, oldValue, newValue) -> {
                            double shapeWidth = shape.getWidth();
                            double shapeHeight = shape.getHeight();
                            double shapeX = shape.getX();
                            double shapeY = shape.getY();
                            double newWidth = newValue.doubleValue();
                            double oldWidth = oldValue.doubleValue();
                            double scaleFactor = newWidth / oldWidth;
                            shape.widthProperty().set(shapeWidth * scaleFactor);
                            shape.heightProperty().set(shapeHeight * scaleFactor);
                            shape.setX(shapeX * scaleFactor);
                            shape.setY(shapeY * scaleFactor);
                        });

                        makeDraggable(shape, selectedImageView);

                        parentGroup.getChildren().add(shape); // Groupにshapeを追加
                    }
                } else {
                    System.out.println("画像を選択してください");
                }
            });

            controller.setTotalChangeListener(this::updateTotalResult);
            controller.setDeleteButtonAction(() -> removeCircle(newCircle));

            circleBox.getChildren().add(newCircle);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void removeCircle(AnchorPane circle) {
        // CircleItemControllerをリストから探して、親ノードを設定
        circleControllers.stream()
                .filter(controller -> controller.getParentPane() == circle)
                .findFirst()
                .ifPresent(controller -> {
                    // circleBoxからそのCircleItemを削除
                    circleBox.getChildren().remove(circle);
                    // CircleItemControllerをリストから削除
                    circleControllers.remove(controller);
                    updateTotalResult();
                });
    }
    private void updateTotalResult() {
        int totalSum = circleControllers.stream()
                .filter(CircleItemController::isCircleCheckSelected)
                .mapToInt(controller -> {
                    try {
                        String totalText = controller.getGoodsResultText();
                        return Integer.parseInt(totalText.replaceAll("\\D", ""));  // 数字を取り出して合計に加算
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();
        int num500YenCoins = (int) Math.ceil((double) totalSum / 500);
        int num1000YenBills = (int) Math.ceil((double) totalSum / 1000);
        totalResult.setText(String.format(
                "総合計: %d円\n500円玉必要枚数: %d枚\n1000円札必要枚数: %d枚",
                totalSum, num500YenCoins, num1000YenBills
        ));
    }
    @FXML
    private void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("画像を選択");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("画像ファイル", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(vboxMap.getScene().getWindow());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            double aspectRatio = image.getWidth() / image.getHeight();
            if (imageViews.isEmpty()) {
                double imageHeight = scrollPaneLeft.getWidth() / aspectRatio;
                imageView.setFitWidth(scrollPaneLeft.getWidth());
                imageView.setFitHeight(imageHeight);
            } else {
                double firstImageWidth = imageViews.get(0).getFitWidth();
                double firstImageHeight = firstImageWidth / aspectRatio;
                imageView.setFitWidth(firstImageWidth);
                imageView.setFitHeight(firstImageHeight);
            }

            // GroupにImageViewを追加
            Group imageGroup = new Group();
            imageGroup.getChildren().add(imageView);

            // 画像クリック時の動作設定
            imageView.setOnMouseClicked(event -> {
                if (!isEditMode) {
                    System.out.println("編集モードを有効にしてください。");
                    return;
                }
                // 画像選択
                if (selectedImageView == imageView) {
                    selectedImageView = null;
                    System.out.println("画像の選択を解除しました。");
                } else {
                    selectedImageView = imageView;
                    System.out.println("画像を選択しました。");
                }
            });
            imageView.setOnScroll(event -> {
                if (event.isControlDown()) {
                    // スクロール量で拡大・縮小
                    double delta = event.getDeltaY();
                    double scaleFactor = (delta > 0) ? 1.1 : 0.9;

                    // imageViews内のすべてのImageViewを拡大・縮小
                    for (ImageView imgView : imageViews) {
                        // 現在の幅を取得して拡大縮小を行う
                        double currentWidth = imgView.getFitWidth();
                        double newWidth = currentWidth * scaleFactor;
                        imgView.fitWidthProperty().set(newWidth);  // 新しい幅を設定

                        // 現在の高さを取得して拡大縮小を行う
                        double currentHeight = imgView.getFitHeight();
                        double newHeight = currentHeight * scaleFactor;
                        imgView.fitHeightProperty().set(newHeight);  // 新しい高さを設定
                    }
                }
            });

            // VBoxにGroupを追加
            vboxMap.getChildren().add(imageGroup);

            // ImageViewをリストに追加
            imageViews.add(imageView);

            MapImageRef ref = new MapImageRef();
            ref.id = UUID.randomUUID().toString();
            ref.filePath = selectedFile.getAbsolutePath(); // or selectedFile.toURI().toString()
            ref.imageView = imageView;
            ref.group = imageGroup;
            mapImages.add(ref);
        }
    }
    private static class MapImageRef {
        String id;
        String filePath;  // 追加: 選択ファイルの絶対パス or URI
        ImageView imageView;
        Group group;
    }
    private final List<MapImageRef> mapImages = new ArrayList<>();
    private void adjustImageWidths(double scrollPaneWidth) {
        if (!imageViews.isEmpty()) {
            ImageView firstImageView = imageViews.get(0);
            double newAspectRatio = firstImageView.getFitWidth() / firstImageView.getFitHeight();
            double newHeight = scrollPaneWidth / newAspectRatio;
            for (ImageView imageView : imageViews) {
                imageView.setFitWidth(scrollPaneWidth);
                imageView.setFitHeight(newHeight);//ここなんでアスペクト比なのか後で聞かなきゃ
            }
        }
    }
    @FXML
    private void deleteImage() {
        if (selectedImageView != null) {
            // 親Groupを取得（確実にGroupであると仮定）
            Group parentGroup = (Group) selectedImageView.getParent();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("確認");
            alert.setHeaderText("画像を削除しますか？");
            alert.setContentText("この操作は元に戻せません。");

            // ユーザーの選択結果を取得
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // VBoxからGroupを削除
                vboxMap.getChildren().remove(parentGroup);

                // Group内のRectangleをリストから削除
                circleControllers.forEach(controller -> {
                    List<Rectangle> shapesToRemove = new ArrayList<>();
                    for (Rectangle rectangle : controller.getShapes()) {
                        // rectangleが削除対象のGroupに属している場合
                        if (parentGroup.getChildren().contains(rectangle)) {
                            shapesToRemove.add(rectangle);
                        }
                    }
                    // リストから削除
                    controller.getShapes().removeAll(shapesToRemove);
                });

                // imageViewsリストのフィルタリング
                imageViews.removeIf(imageView -> imageView == selectedImageView);

                // 選択状態をリセット
                selectedImageView = null;

                System.out.println("画像と関連するRectangleを削除しました。");
            }
        } else {
            System.out.println("削除する画像が選択されていません。");
        }
    }
    @FXML
    public void saveGroup() {
        // vboxMapにある全てのGroupを取得
        List<Node> children = vboxMap.getChildren();

        if (children.isEmpty()) {
            showAlert("保存エラー", "保存できるGroupがありません。", AlertType.ERROR);
            return;
        }

        // DirectoryChooserを使って保存先フォルダを選択
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("保存先を選択");
        File directory = directoryChooser.showDialog(vboxMap.getScene().getWindow());

        if (directory == null) {
            // ユーザーがキャンセルした場合
            showAlert("キャンセル", "保存先が選択されませんでした。", AlertType.INFORMATION);
            return;
        }

        // 保存用のフォルダを作成
        File groupFolder = new File(directory, "Snapshots");
        if (!groupFolder.exists() && !groupFolder.mkdirs()) {
            showAlert("保存エラー", "フォルダを作成できませんでした。", AlertType.ERROR);
            return;
        }

        // 各Groupを保存
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof Group) {
                Group group = (Group) children.get(i);
                File groupFile = new File(groupFolder, "snapshot_" + (i + 1) + ".png");

                // スナップショットを高解像度で保存
                if (!saveHighResolutionSnapshot(group, groupFile)) {
                    showAlert("保存エラー", "画像の保存中にエラーが発生しました。", AlertType.ERROR);
                    return;
                }
            }
        }

        // 保存完了ダイアログ
        showAlert("保存完了", "全てのGroupが保存されました。保存先: " + groupFolder.getAbsolutePath(), AlertType.INFORMATION);
    }
    @FXML
    private void circleHidden() {
        boolean isVisible = circleList.isVisible();
        circleList.setVisible(!isVisible);
        circleList.setManaged(!isVisible);
        fullScreen.setText(isVisible ? "サークル表示" : "サークル非表示");
    }
    public void setButtonState(Button button, boolean enabled) {
        button.setDisable(!enabled); // 有効化/無効化を設定
        button.setOpacity(enabled ? 1.0 : 0.5); // 有効なら通常表示、無効なら薄く表示
    }

    private void makeDraggable(Rectangle rectangle, ImageView imageView) {
        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];

        // マウスが押されたときに、現在の位置を記録
        rectangle.setOnMousePressed(event -> {
            mouseAnchorX[0] = event.getSceneX() - rectangle.getX();
            mouseAnchorY[0] = event.getSceneY() - rectangle.getY();
        });

        // マウスがドラッグされたときに、Rectangleの位置を更新
        rectangle.setOnMouseDragged(event -> {
            // 画像の位置とサイズを取得
            double imageViewX = imageView.getLayoutX();
            double imageViewY = imageView.getLayoutY();
            double imageViewWidth = imageView.getFitWidth();
            double imageViewHeight = imageView.getFitHeight();

            // 新しい位置を計算
            double newX = event.getSceneX() - mouseAnchorX[0];
            double newY = event.getSceneY() - mouseAnchorY[0];

            // ImageViewの範囲内でRectangleを制限
            newX = Math.max(imageViewX, Math.min(newX, imageViewX + imageViewWidth - rectangle.getWidth()));
            newY = Math.max(imageViewY, Math.min(newY, imageViewY + imageViewHeight - rectangle.getHeight()));

            // Rectangleの位置を更新
            rectangle.setX(newX);
            rectangle.setY(newY);
        });
    }
    public void deleteShapesFromVBox(List<Rectangle> shapes) {
        for (Node node : vboxMap.getChildren()) {
            if (node instanceof Group) {
                Group group = (Group) node;
                group.getChildren().removeIf(shapes::contains); // shapesに含まれるRectangleを削除
            }
        }
    }
    // 高解像度スナップショットを保存するメソッド
    private boolean saveHighResolutionSnapshot(Group group, File file) {
        double scaleFactor = 2.0; // 解像度のスケール (2倍に設定)
        WritableImage snapshot = new WritableImage(
                (int) (group.getBoundsInLocal().getWidth() * scaleFactor),
                (int) (group.getBoundsInLocal().getHeight() * scaleFactor)
        );

        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(scaleFactor, scaleFactor));

        try {
            WritableImage highResImage = group.snapshot(params, snapshot);
            ImageIO.write(SwingFXUtils.fromFXImage(highResImage, null), "png", file);
            System.out.println("Saved: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save snapshot: " + e.getMessage());
            return false;
        }
    }
    // ダイアログを表示するメソッド
    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getImageIdFor(Rectangle rect) {
        Parent p = rect.getParent();
        while (p != null && !(p instanceof Group)) {
            p = p.getParent();
        }
        if (p instanceof Group) {
            Group g = (Group) p;
            for (MapImageRef ref : mapImages) {
                if (ref.group == g) return ref.id;
            }
        }
        return null; // 見つからなかった場合
    }

    public List<com.example.eventmap.dto.ImageData> buildImageDataList() {
        List<com.example.eventmap.dto.ImageData> list = new ArrayList<>();
        for (MapImageRef ref : mapImages) {
            com.example.eventmap.dto.ImageData d = new com.example.eventmap.dto.ImageData();
            d.setId(ref.id);
            d.setFilePath(ref.filePath);
            d.setWidth(ref.imageView.getFitWidth());
            d.setHeight(ref.imageView.getFitHeight());
            list.add(d);
        }
        return list;
    }
    public EventData toEventData() {
        EventData data = new EventData();

        // イベント名（編集モードの有無で適切に）
        String name = (eventNameField.isVisible() ? eventNameField.getText() : eventNameText.getText());
        data.setEventName(name);

        // 画像一覧
        data.setImages(buildImageDataList());

        // サークル一覧
        List<CircleData> circles = new ArrayList<>();
        for (CircleItemController c : circleControllers) {
            circles.add(c.toData());
        }
        data.setCircles(circles);

        return data;
    }

    @FXML
    private void saveEventToJson() {
        EventData data = toEventData();
        ObjectMapper mapper = new ObjectMapper();
        try {
            Path dir = AppPaths.ensureSaveDir();
            String fileName = UUID.randomUUID().toString() + ".json";
            Path out = dir.resolve(fileName);

            mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), data);
            showAlert("保存完了", "保存先: " + out.toAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("保存エラー", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void attachAutoScale(Rectangle shape, ImageView imageView) {
        final double[] prevW = { imageView.getFitWidth() };
        imageView.fitWidthProperty().addListener((obs, oldW, newW) -> {
            double scale = newW.doubleValue() / prevW[0];
            // 位置とサイズを同率で拡縮
            shape.setX(shape.getX() * scale);
            shape.setY(shape.getY() * scale);
            shape.setWidth(shape.getWidth() * scale);
            shape.setHeight(shape.getHeight() * scale);
            prevW[0] = newW.doubleValue();
        });
    }
    private void clearAllForLoad() {
        vboxMap.getChildren().clear();
        imageViews.clear();
        mapImages.clear();
        circleBox.getChildren().clear();
        circleControllers.clear();
        selectedImageView = null;
    }
    private void restoreImages(List<com.example.eventmap.dto.ImageData> images) {
        if (images == null) return;
        for (var img : images) {
            Image image = new Image(new File(img.getFilePath()).toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(img.getWidth());
            imageView.setFitHeight(img.getHeight());

            Group imageGroup = new Group(imageView);

            // クリック選択（既存と同じ）
            imageView.setOnMouseClicked(event -> {
                if (!isEditMode) { System.out.println("編集モードを有効にしてください。"); return; }
                selectedImageView = (selectedImageView == imageView) ? null : imageView;
            });

            // Ctrl+スクロールで全画像拡縮（既存動作を踏襲）
            imageView.setOnScroll(event -> {
                if (event.isControlDown()) {
                    double delta = event.getDeltaY();
                    double scaleFactor = (delta > 0) ? 1.1 : 0.9;
                    for (ImageView iv : imageViews) {
                        iv.setFitWidth(iv.getFitWidth() * scaleFactor);
                        iv.setFitHeight(iv.getFitHeight() * scaleFactor);
                    }
                }
            });

            vboxMap.getChildren().add(imageGroup);
            imageViews.add(imageView);

            // レジストリ登録（id は JSON のまま使う）
            MapImageRef ref = new MapImageRef();
            ref.id = img.getId();
            ref.filePath = img.getFilePath();
            ref.imageView = imageView;
            ref.group = imageGroup;
            mapImages.add(ref);
        }
    }
    public void loadEventFromJson(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            EventData data = mapper.readValue(file, EventData.class);

            // 画面を初期化
            clearAllForLoad();

            // イベント名
            eventNameText.setText(data.getEventName());
            eventNameField.setText(data.getEventName());

            // 画像を復元
            restoreImages(data.getImages());

            // サークル（並び順があればソート）
            List<CircleData> circles = data.getCircles();
            if (circles == null) circles = List.of();

            for (CircleData cd : circles) {
                // UI生成
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CircleItem.fxml"));
                AnchorPane pane = loader.load();

                CircleItemController ctrl = loader.getController();
                ctrl.setParentPane(pane);
                ctrl.setParentController(this);

                // チェック・色
                ctrl.setCircleChecked(cd.isChecked());
                ctrl.setCircleColorHex(cd.getColor());
                ctrl.setCircleNameText(cd.getName());
                ctrl.setCircleCommentText(cd.getMemo());

                // 商品
                if (cd.getGoods() != null) {
                    for (GoodsData gd : cd.getGoods()) {
                        ctrl.addGoodsFromData(gd);
                    }
                }

                // 矩形
                if (cd.getRectangles() != null) {
                    for (RectangleData rd : cd.getRectangles()) {
                        // どの画像かを探す
                        MapImageRef target = mapImages.stream()
                                .filter(m -> m.id.equals(rd.getImageId()))
                                .findFirst().orElse(null);
                        if (target == null) continue; // 見つからない場合はスキップ

                        // 矩形を作成
                        Rectangle rect = new Rectangle(rd.getWidth(), rd.getHeight());
                        rect.setX(rd.getX());
                        rect.setY(rd.getY());
                        try { rect.setFill(Color.web(rd.getColor())); } catch (Exception ignore) {}

                        // 動かせるようにする＆拡縮追従
                        makeDraggable(rect, target.imageView);
                        attachAutoScale(rect, target.imageView);

                        // 画面へ
                        target.group.getChildren().add(rect);
                        // サークルの shapes リストにも登録
                        ctrl.addRectangle(rect);
                    }
                }

                // 親へ登録
                circleControllers.add(ctrl);
                circleBox.getChildren().add(pane);
            }

            // 合計など再計算
            updateTotalResult();

            showAlert("復元完了", "イベントを復元しました。", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("復元エラー", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}