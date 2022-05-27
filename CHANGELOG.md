# Reports

## Unreleased
- with-let の使い方
- ERROR: XMLHttpRequest is not defined
- error Access to /r/ is not authorized を
  /login にリダイレクトする
- button.is-primary.is-small でも大きすぎる
- is-fifth でも大きすぎる。
- good 送信後の input フィールドのクリア
- feedback upload
- return key でメッセージ送信
- login 名の最大幅で div
- hot に時刻表示

## 0.8.2-SNAPSHOT
- いいねとユーザの順番を sent/receive で変える。
- both が良くないか？

## 0.8.1 - 2022-05-27
- goods sent/received を別ページに。
- admin でログイン時、sent/received をログイン名で表示、
  一般アカウントでログイン時は abbrev.

## 0.8.0 - 2022-05-27
- define `core.cljs` private functions using `defn-`
- histogram(?)
- cljs repl
  今は reports プロジェクト内で clj/cljs を切り替えて作業できている。
  手順次第でできるようだ。タブを選択するだけで repl が切り替わっている(m2)
- Warning: validateDOMNesting(...): <div> cannot appear as a descendant of <p>.

## 0.7.4 - 2022-05-26
### Changed
- /api/goods/:me -> /api/goods-to/:user
- /api/sends/:me -> /api/goods-from/:user
- /api/goods-to, goods-from -> まとめて /api/goods
- reverse order good reveived/sent
- windows の絵文字は美しくない。favicon.ico 代えよう。
- renamed r/atom goods -> r/atom recvs
- To [:b user],
- goods をダウンロードしておき、使い回す。
### Added
- /api/goods

## 0.7.3 - 2022-05-26
- not yet sent to をシャッフル
- forget access restriction remove comment

## 0.7.2 - 2022-05-26
- 0.7.1 は機能していない。
- get や contains ではなく、.indexOf
- calva-cljs node repl

## 0.7.1 - 2022-05-26
- Goods: 未提出はリンクにしない。

## 0.7.0 - 2022-05-26
- one-fifth より幅が狭いクラスは定義されてない。
- logout! assoc が良くて disj がダメな理由
  -> disj の引数は set

## 0.6.4 - 2022-05-26
- Goods(sent)
- Goods を三分割
- 自分から自分へのメッセージを弾く
- Goods(not yet) 提出がないときどうする？
  -> エラーでいいか。

## 0.6.3 - 2022-05-25
### Fixed
- Goods ページ: react.development.js:221 Warning: Each child in a list should have a unique "key" prop.
  (for [[id g] (map-indexed vector @goods)]
     [:p {:key id}
      (.toLocaleString (:timestamp g))
      [:br]
      (:message g)])
- timestamp の表示
  [TaggedValue: LocalDateTime, 2022-05-24T23:30:40.697]
  (defn time-format [time]
    (let [s (str time)
       date (subs s 28 39)
       time (subs s 40 48)]
   (str date " " time)))

## 0.6.2 - 2022-05-25
- db-dump/{db-dump,db-restore}.sh
- チラッと見える土台　html
  -> clean up home.html
- title "Report"
- bump-version.sh, 日付を date '+%F %T' で得る
- Upload に説明文

## 0.6.1 - 2022-05-24
- 送信メッセージ長さのチェック
- 受け取ったメッセージの表示

## 0.6.0 - 2022-05-24
- js/alert recv, mesg
- can send messages
- (for [u @users]) のループを　map-indexed で回した。

  Warning: Every element in a seq should have a unique :key: ([:div.columns [:div.column [:a {:href "http://localhost:8080/hkimura"} "hkimura"]] [:div.column " " [:input {:placeholder "message"}] [:button "send"]]] [:div.columns [:div.column [:a {:href "http://localhost:8080/user2"} "user2"]] [:div.column " " [:input {:placeholder "message"}] [:button "send"]]] [:div.columns [:div.column [:a {:href "http://localhost:8080/user1"} "user1"]] [:div.column " " [:input {:placeholder "message"}] [:button "send"]]])
 (in browse-page)
- can send messages


## 0.5.1 - 2022-05-24
- Browse random/hot の並び替えができる。
  ラジオボタンが選びにくく、選べないのではとずーっと思ってた。

## 0.5.0 - 2022-05-24
can browse locally
- Invalid anti-forgery token
  -> 「再読み込み後にログイン」のメッセージ
- alter table uploads add column filename varchar(64) not null
- db/create-upload!
- db/get-uploads

## 0.4.0 - 2022-05-24
- :page-url "http://localhost:3001/" の導入。
  デベロップでは http-server を動かしとけ。
  プロダクションでは "https://hp.melt.kyutech.ac.jp/" になる。
- upload 後の戻り先 -> /r/#/upload とした。
- (js/Date.) or (js/Date)
  (js/Date) == (str (js/Date.))
- `check your report` in upload-page
- `check your report` in home-page
- check your report URL を csrf と同様の手段で cljs に渡す。
- time format
  (.toLocaleString (js/Date.))

## 0.3.3 - 2022-05-23
- /api/upload production では PUBLIC_DIR 環境変数を定義すること。
    export PUBLIC_DIR=/home/ubuntu/reports/public
- api/copy! でエラー。
  No method in multimethod 'do-copy' for dispatch value: [java.io.File java.lang.String]
  ->  (io/copy tempfile (io/file (str dir "/" filename)))
- About にバージョンと更新日時を表示。
- Uploads が動き出した。

## 0.3.1 - 2022-05-23
- (layout/render [req] "template.html" {:key value}) で渡し、
  template.html 中に、

```
  <script type="text/javascript">
        var key = "{{value}}";
  </script>
```
  cljs 側ではその値を js/key で参照できる。
- test deploy to l.melt. OK.
- fix typo. parInfer 使ってると時々、括弧の対応を外してしまって気がつかない。

## 0.3.0 - 2022-05-23
- (assoc :session {}) は良くて、(dissoc :session) はダメな理由はなんだ？
  :session キーがないのがダメってこと？
- /r/ping が反応しない -> /api/ping の間違い。
- /api/ping not allowed -> プログラムミス。シンタックスエラーが実行時までエラーにならない。
- login/logout を nav-bar に表示。
- nav-bar の Report はリンクじゃなくていいけど　-> メニューが不細工に見えるので止める。
  代わりに #/ をリンク。
- カラの Upload, Browse, Goods ページ。
- github 取り下げて上げ直し。

# RESTART
深夜のプログラミングは思ったほど捗らない。
最初からやり直し。+auth 忘れないよう。

## 0.2.1 - 2022-05-22
- early deploy
- (hato.client/get url {:as :json})
- I don't believe CORS.
  リバースプロキシ配下の web app のアクセスを許すのに次はまずいんじゃないの？
  せめて表向きのグローバルアドレスでフィルタすべき。誤解しているか？

  :access-control-allow-origin  [#"http://localhost.*"]

## 0.2.0 - 2022-05-22
- forgot buddy. copied from other project created by
  `lein new luminus app +budy`
- login/logout. when login error, flash message

## 0.1.0  - 2022-05-22
prep for development.
- lein new luminus reports +reagent +postgres
- antq --upgrade
- npm install
- npm install xmlhttprequest
- code REPL report Server+Client
- create database reports owner='postgres';
- (create-migration "uploads")
- (create-migration "goods")
- (migrate)
- gh repo create hkim0331/reports.git --public
- git remote add origin git@github.com:hkim0331/reports.git