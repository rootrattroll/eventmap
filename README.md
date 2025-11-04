# eventmap
同人即売会などで使えるように開発中のeventのマップです。現在頒布物のメモを印刷できるような仕組みを考えています。
🎪 EventMap – 同人イベント向けマップ管理アプリ

JavaFX で開発した、同人即売会・イベント参加者向けマップ管理ツール。
地図画像の上にサークル位置（矩形）を配置し、商品リストや購入予定金額を自動集計できます。
状態はイベント単位で JSON（UUID ファイル名）に保存・完全復元できます。

✨ 主な機能

🗺 マップ管理：イベント会場の画像を複数追加・切替（画像ごとに矩形を紐づけ）

🎨 サークル登録：矩形（Rectangle）で位置指定、名称・メモ・色・有効/無効を管理

🛍 商品管理：サークル単位で商品名／価格／チェック（購入予定）を追加・削除

💰 合計の自動計算：チェック済み商品のみ集計（サークルのON/OFFも反映）

💾 保存・復元：イベント単位で JSON 保存（ファイル名は UUID.json）、完全復元

🗑 イベント削除：一覧から UUID.json を物理削除

🧩 直感的UI：FXML + Controller による動的 UI（CircleItem / GoodsList / EventView）

### プロジェクト構成
```
main/
├─ java/
│  └─ com.example.eventmap/
│     ├─ EventListController.java      # イベント一覧（ロード/削除/新規）
│     ├─ EventViewController.java      # メイン画面（画像・矩形・サークル管理）
│     ├─ CircleItemController.java     # サークル枠UI（商品・色・チェック等）
│     ├─ GoodsListController.java      # 商品行の追加・削除・合計計算
│     └─ dto/
│         ├─ EventData.java
│         ├─ CircleData.java
│         ├─ RectangleData.java
│         ├─ GoodsData.java
│         └─ ImageData.java
│
└─ resources/com/example/eventmap/
   ├─ EventList.fxml
   ├─ EventView.fxml
   ├─ CircleItem.fxml
   └─ GoodsList.fxml
```

---
#保存データ仕様
```
{
  "eventName": "夏コミ2025",
  "images": [
    {
      "id": "uuid-image-123",
      "filePath": "C:\\Users\\gatou\\Pictures\\map1.jpg",
      "width": 720,
      "height": 512
    }
  ],
  "circles": [
    {
      "id": "uuid-circle-1",
      "name": "CircleA",
      "memo": "友人サークル",
      "checked": true,
      "color": "#FFAA00",
      "rectangles": [
        { "id": "uuid-rect-1", "x": 240, "y": 300, "width": 80, "height": 60, "color": "#FFAA00", "imageId": "uuid-image-123" }
      ],
      "goods": [
        { "productName": "新刊", "price": 1000, "checked": true },
        { "productName": "缶バッジ", "price": 500, "checked": false }
      ]
    }
  ]
}
```

---
#技術スタック
Java 21

JavaFX 17（FXML / Controller）

Jackson（JSON シリアライズ / デシリアライズ）

Maven（依存管理）

#実行環境
IDE：IntelliJ IDEA（Community 版でOK）
JDK：21 を推奨（プロジェクトの言語レベルを合わせてください）


#使い方
1.イベント一覧から「新規イベント」を作成（イベント名入力）

2.マップ画像を追加（複数可）

3.サークルを追加

4.色・サイズを指定 → 矩形を生成（画像を選択している必要あり）

5.サークル名・コメントを入力、チェックON/OFFで有効化

6.商品を追加

7.商品名・価格を入力、チェックで購入予定に反映

8.右上で合計金額を自動集計

9.保存：イベント単位で UUID.json が ~/Documents/EventMapSaves/ に出力

10.次回起動：イベント一覧からロード（開く） / 削除（UUID.json を物理削除）

# 設計メモ

DTO分離：UI状態は EventData / CircleData / RectangleData / GoodsData / ImageData にマッピング

完全復元：画像 → サークル → 矩形（座標/サイズ/色/imageId）→ 商品（チェック含む）の順に復元

拡張余地：PDF マップの抽出、MongoDB Atlas 連携、上書き保存モード、統計ビュー等

# 今後の拡張予定
PDF の直接インポート & AI による座標抽出
MongoDB Atlas 連携（クラウド保存・共有）
上書き保存モード（UUID 新規発行ではなく既存JSONを更新）
クラウドを用いて専用アプリと連携




