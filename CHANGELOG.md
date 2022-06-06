# Reports

## Unreleased
- with-let の使い方
- ERROR: XMLHttpRequest is not defined
- error Access to /r/ is not authorized を
  /login にリダイレクトする
- button.is-primary.is-small でも大きすぎる
- is-fifth でも大きすぎる。
- good 送信後の input フィールドのクリア(クリアしない方がいい)
- login 名の最大幅で div
- feedback upload
- return key でメッセージ送信
- hot に時刻表示
  -> get /users で最近アップデートの時刻をくっつけて持って来れれば可能だが、
  0.8.3 のソリューションが影響を受ける。やめとこ。
  -> hkimura が毎日数回以上、レポートページを更新すると、hot にしたときに
  hkimura より上にある人が前回のチェックの後に更新あった人ってわかる。
- Goods/graph から各レポートをリンク（リンク先がバレる。嫌がるか？）
- 再読み込みの後じゃないと good! が出ていかないことがある。
- graph に表示される REPLY を除く。
  -> 簡単にやるにはループの内側で。
  -> 効率を考えるにはループの前にフィルタする。
- reports ページが下に長すぎ。head で切って、more? とかやるか？
- 誰から誰にをすべて隠して、メッセージ本文だけ時系列で表示する。
- TDD
- db/functions の引数
- reagent, マップの場所はそこか？

## 0.11.0 - 2022-06-06
### Added
- Upload のページに uploaded のセクション追加。
  * 全体
  * 自分
  * hkimura

## 0.10.0 - 2022-06-05
### Added
- goods 総数を表示する。
### Changed
- REPLY をグラフから外す

## 0.9.4 - 2022-06-04
- chaged home menu

## 0.9.3-mikan - 2022-06-22
### Fixed
- sender/receiver を間違えた。

## 0.9.2-mikan - 2022-06-02
- goods sent を message に '(Re:' が入っていたら reply とし、
  sender 名 を abbrev する。
- received -> name -> send の name を太字に。

## 0.9.1 - 2022-06-02
- mikan's advice
  goods received 表示だけ abbrev

## 0.9.0 - 2022-06-02
- l.melt にデプロイ
- タイトルを login のリンクの横に表示する
  -> title 書いてる人少ない。やめるか。

## 0.9.0-SNAPSHOT
- dswcj 通りの (migrate) は期待通りに行かず、
  lein run migrate 20220602044123 を
  実行した。
- core.clj/upsert
- /api/titles
- display titles

## 0.8.12 - 2022-05-31
### Added
- about 見ないでも login したらバージョンわかるように。
- コミットしたら core.clj は上書きされてしまうか(vscode)
  -> しない。やっぱ、何かのショートカットキーをミスって打ってるんじゃないかなあ。
- REPLY に元メッセージを引用する。
- 自分のリンクは赤表示。

## 0.8.11 - 2022-05-31
### Added
- reply メッセージにオリジナルメッセージを (Re: ) で囲んでアペンド。

## 0.8.10 - 2022-05-31
### Changed
- 0.8.9 を変更。hkimura のみ赤色で。

## 0.8.9 - 2022-05-30
### Changed
- Browse & Comments 自分のリンクを赤で、ホバリングしたら larger

## 0.8.8 - 2022-05-29
- users-all を l22.melt/api/logins から読む。
  l22 もこの対応で 0.4.5.
### Removed
- 古い histogram 関連、users-all をハードコーディングしていた過去のコードを削除。
### Fixme
- CORS に抵触するのだが、
  #"https://rp.melt.kyutech.ac.jp" だと許可されるのに、
  #"https://rp.melt.kyutech.ac.jp.*" がダメっつう理由がわからない。


## 0.8.7 - 2022-05-29
### Fixed
- Not Yet Send To バグ
  -> core/disj じゃなく set/difference でした。
### Changed
- deply.sh は `lein uberjar` を含む。

## 0.8.6 - 2022-05-29
### Added
- 緑のリプライするとオリジナルメッセージの送信者がわかってしまう。
  返事しない限り、わからないんで、OK にしとこ。anonymous などにするのは可能。
  先にユーザアカウント作っておくか。
  -> 送信者 REPLY にする。
- REPLY メッセージへの reply はできないこととする。

## 0.8.5 - 2022-05-28
- goods received に返事書きたい。誰が送信したかを分からないままで返信
  -> CLJS の js/propt で実装。

## 0.8.4 - 2022-05-28
- browse-page は hot をデフォルトにする。
- Goods | graph, sent/received 別ページよりも both がいい。
  内容に対してコードが複雑すぎる。再帰が敗北感を感じる。

## 0.8.3 - 2022-05-28
### Fixed
- goods が送れなかった理由はなんだ？ hkimura だけ？
  -> ブラウザのキャッシュか？
- Browse の random で上から何番目につけた goods が hot のその番目で出てしまう。
  -> max-index の仕方を変更して対応した。

## 0.8.2 - 2022-05-27
- いいねとユーザの順番を sent/receive で変える。
- both が良くないか？
- refactor: ルーティング整理
### Removed
- 使わなくなった関数、アップデート前にコメントアウトした関数を削除した。

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
