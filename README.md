# 通信応答時間チェッカー（Java）

## 概要

指定したWebサイトにHTTPリクエストを複数回送り、応答時間とHTTPステータスを確認するコマンドラインツールです。Webサイトへのアクセス結果を記録・集計し、応答状況を簡単に把握することを目的としています。

## 主な機能

- 測定先URLと測定回数（1～100回）の指定
- GETリクエストによるHTTP応答の確認
- 各リクエストのHTTPステータスと応答時間の表示
- HTTP 2xx応答の回数・成功率の集計
- 接続エラーや、2xx以外のHTTPステータスの回数の表示
- HTTP 2xx応答について、最短・最長・平均応答時間を計算

## 使用技術

- Java 11以降
- java.net.http.HttpClient：HTTPリクエスト
- java.time.Duration：タイムアウト設定
- java.util.List / ArrayList：測定結果の管理
- Scanner：コマンドライン入力

## 実行方法

ソースコードがあるフォルダで、次のコマンドを実行します。

```bash
javac NetworkResponseChecker.java
java NetworkResponseChecker
