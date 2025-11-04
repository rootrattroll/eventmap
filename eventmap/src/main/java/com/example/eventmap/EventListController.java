package com.example.eventmap;

import com.example.eventmap.dto.*;
import com.example.eventmap.list.SavedEventRow;
import com.example.eventmap.util.AppPaths;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EventListController {
    @FXML private Button newEvent;
    @FXML private TableView<SavedEventRow> eventTable;
    @FXML private TableColumn<SavedEventRow, Integer> serialNumberColumn;
    @FXML private TableColumn<SavedEventRow, String> eventNameColumn;
    @FXML private TableColumn<SavedEventRow, Integer> totalAmountColumn;
    @FXML private TableColumn<SavedEventRow, String> comentColumn;
    @FXML private TableColumn<SavedEventRow, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Label totalEventsLabel;

    private final ObservableList<SavedEventRow> rows = FXCollections.observableArrayList();
    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }
    public void closeStage() { if (stage != null) stage.close(); }

    @FXML
    public void initialize() {
        serialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        comentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        setupActionColumn();
        setupDeleteColumn();
        loadEventsFromFolder();

        // 簡易検索（名前に部分一致）
        searchField.textProperty().addListener((obs, ov, nv) -> {
            if (nv == null || nv.isBlank()) {
                eventTable.setItems(rows);
            } else {
                String q = nv.toLowerCase();
                eventTable.setItems(rows.filtered(r -> r.getName() != null && r.getName().toLowerCase().contains(q)));
            }
            totalEventsLabel.setText("総イベント数: " + eventTable.getItems().size());
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("開く");
            {
                openBtn.setOnAction(e -> {
                    SavedEventRow row = getTableView().getItems().get(getIndex());
                    openEvent(row.getFilePath());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openBtn);
            }
        });
    }

    private void loadEventsFromFolder() {
        rows.clear();
        Path dir = AppPaths.ensureSaveDir();

        try (var stream = Files.list(dir)) {
            List<Path> files = stream
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            int idx = 1;
            for (Path p : files) {
                try {
                    EventData data = mapper.readValue(p.toFile(), EventData.class);
                    String name = data.getEventName();
                    int total = computeTotalAmount(data);     // チェックされた商品合計
                    String comment = "";                      // イベント全体のコメントが無ければ空
                    rows.add(new SavedEventRow(idx++, name, total, comment, p.toAbsolutePath().toString()));
                } catch (Exception ex) {
                    // 壊れたJSONはスキップ
                    System.err.println("読み込み失敗: " + p + " : " + ex.getMessage());
                }
            }
            eventTable.setItems(rows);
            totalEventsLabel.setText("総イベント数: " + rows.size());
        } catch (Exception e) {
            e.printStackTrace();
            totalEventsLabel.setText("総イベント数: 0");
        }
    }

    // 合計：サークルがチェックされているものだけ、かつ商品チェック済みだけ合算
    private int computeTotalAmount(EventData data) {
        if (data.getCircles() == null) return 0;
        int sum = 0;
        for (CircleData c : data.getCircles()) {
            if (!c.isChecked()) continue;
            if (c.getGoods() == null) continue;
            for (GoodsData g : c.getGoods()) {
                if (g.isChecked()) sum += g.getPrice();
            }
        }
        return sum;
    }

    private void openEvent(String filePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EventView.fxml"));
            Parent root = loader.load();
            EventViewController controller = loader.getController();

            // EventView に遷移
            Stage stg = (Stage) eventTable.getScene().getWindow();
            stg.setTitle("イベント詳細");
            stg.setScene(new Scene(root));
            stg.show();

            // JSONをロードして復元
            controller.loadEventFromJson(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("読み込みエラー", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToEventWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EventView.fxml"));
            Parent root = loader.load();
            Stage stg = (Stage) newEvent.getScene().getWindow();
            stg.setTitle("イベント詳細");
            stg.setScene(new Scene(root));
            stg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    private void setupDeleteColumn() {
        TableColumn<SavedEventRow, Void> deleteColumn = new TableColumn<>("削除");
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("削除");
            {
                deleteBtn.setOnAction(e -> {
                    SavedEventRow row = getTableView().getItems().get(getIndex());
                    if (row == null) return;

                    // 確認ダイアログ
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("確認");
                    alert.setHeaderText("このイベントを削除しますか？");
                    alert.setContentText("この操作は取り消せません。");
                    var result = alert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            Path p = Paths.get(row.getFilePath());
                            boolean deleted = Files.deleteIfExists(p);
                            if (deleted) {
                                // 一覧から削除 & ラベル更新
                                getTableView().getItems().remove(row);
                                totalEventsLabel.setText("総イベント数: " + getTableView().getItems().size());
                                showAlert("削除完了", "ファイルを削除しました:\n" + p.toAbsolutePath(), Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("削除できませんでした", "ファイルが見つからないか、削除できませんでした。", Alert.AlertType.WARNING);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showAlert("削除エラー", ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        // テーブルに列を追加（末尾に表示）
        eventTable.getColumns().add(deleteColumn);
    }

}
