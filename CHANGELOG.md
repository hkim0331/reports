# Reports

## Unreleased
- with-let の使い方
- 再読み込みの後じゃないと good! が出ていかないことがある。
- dev container?
- report-pt! - 同人に複数票が入れられる。
- colorful buttons.
- 1:1 -> 2:1

## v2.6-SNAPSHOT

## v2.6.580 / 2024-05-30
### Added
- update button to `points received`.
### Changed
- radio to buttons

## v2.6.578 / 2024-05-30
### Added
- prod only wrap-restricted
```
  :middleware [(if (:dev env) identity middleware/wrap-restricted)
  ...
```
- points sent


## v2.5.570 / 2024-05-30
### Added
- db-dumps/create-points.sql (gitignored)
- core.cljs:student-page and its relatinng functions.
- services.clj:report-pt!

## v2.4.558 / 2024-05-30
### Added
- `make node_modules`
- `hp-server.sh` - start python http server to show received reports.
### Fixed
- `home.clj/home-page` - add {:mode (:mode env)} to params.
- `home.html` - var mode = "{{mode}}";
- `layout.clj/render` -  (println "layout/redder params" params)
- `start-local.sh` - export RP_MODE="exam"

## v2.3.546 / 2024-05-20
### Added
- dev-config.edn-model
  dev-config.edn is gitignored. so, model definitions.
- defined `core.cljs/shorten`


## v2.2.544 / 2024-05-10
- core.cljs upload sounds

## v2.1.539 / 2024-05-05
- 中間試験は 6/5。レポート〆切は 5/29 だ。

## v2.1.534 / 2024-04-30
- [org.webjars.npm/bulma "0.9.4"] not "1.0.0"

## v2.1.530 / 2024-04-30
- updated libraries

| :file       | :name                                           | :current  | :latest   |
|------------ | ----------------------------------------------- | --------- | ----------|
| project.clj | buddy/buddy-core                                | 1.10.413  | 1.11.423  |
|             | buddy/buddy-hashers                             | 1.8.158   | 2.0.167   |
|             | buddy/buddy-sign                                | 3.4.333   | 3.5.351   |
|             | ch.qos.logback/logback-classic                  | 1.2.10    | 1.5.6     |
|             | cider/cider-nrepl                               | 0.30.0    | 0.47.1    |
|             | clojure.java-time/clojure.java-time             | 1.2.0     | 1.4.2     |
|             | com.google.javascript/closure-compiler-unshaded | v20230502 | v20240317 |
|             | cprop/cprop                                     | 0.1.19    | 0.1.20    |
|             | jonase/eastwood                                 | 1.4.0     | 1.4.2     |
|             | markdown-clj/markdown-clj                       | 1.11.4    | 1.12.1    |
|             | metosin/muuntaja                                | 0.6.8     | 0.6.10    |
|             | metosin/reitit                                  | 0.5.15    | 0.7.0     |
|             | mount/mount                                     | 0.1.17    | 0.1.18    |
|             | nrepl/nrepl                                     | 1.0.0     | 1.1.1     |
|             | org.clojure/clojure                             | 1.11.1    | 1.11.3    |
|             | org.clojure/clojurescript                       | 1.11.60   | 1.11.132  |
|             | org.clojure/core.async                          | 1.6.673   | 1.6.681   |
|             | org.clojure/tools.cli                           | 1.0.219   | 1.1.230   |
|             | org.clojure/tools.logging                       | 1.2.4     | 1.3.0     |
|             | org.clojure/tools.namespace                     | 1.4.4     | 1.5.0     |
|             | org.postgresql/postgresql                       | 42.6.0    | 42.7.3    |
|             | org.webjars.npm/bulma                           | 0.9.4     | 1.0.0     |
|             | org.webjars/webjars-locator                     | 0.46      | 0.52      |
|             | ring/ring-core                                  | 1.9.5     | 1.12.1    |
|             | ring/ring-defaults                              | 0.3.4     | 0.5.0     |
|             | ring/ring-devel                                 | 1.9.5     | 1.12.1    |
|             | selmer/selmer                                   | 1.12.58   | 1.12.59   |
|             | thheller/shadow-cljs                            | 2.23.3    | 2.28.4    |


## v2.0.524 / 2024-04-26
- delete the link to 2022 reports.

## v2.0.516 / 2024-04-26
- 2024 started
- nrepl port 7004
- app port 3004
- bump-version.sh will update CHANGELOG.md
- hkimura 'sample' page

## 1.24.0 - 2023-08-23
- updated start.sh, stop.sh
  launch/stop public/start.sh inside the scripts.
- FIXED: can upload other than *.md files from upload md form.
  {:accept text/markdown} を指定しても、ほとんどのブラウザは text/plain と解釈してしまう。
  /app/upload でチェックした。
```clojure
      (when (= type "md")
        (when-not (str/ends-with? filename ".md")
          (throw (Exception. "*.md only"))))
```


## 1.22.0 - 2023-07-05
- can upload zip files

## 1.21.0 - 2023-06-18
- /day-by-day Goods sent, day by day

## 1.20.2 - 2023-06-15
### Refactored
- 送信メッセージのvalidation を早めに
### CHANGED
- /api/goods returns order by id desc


## 1.20.1 - 2023-06-12
- finish midterm exam. resume.

## 1.20.0 - 2023-06-11
- exam mode
services.clj/upload! returns {:status 200 :body "upload success}

## 1.19.0 - 2023-06-09
- can upload mpvies
- 🌞 and 🌳

## 1.18.18 - 2023-06-09
- goods abbrev を解いて、リンク復活
- 自分のアカウントアンカーの背景を赤、フォント色白で強調。

## 1.18.17 - 2023-06-08
- sort uploaded date

## v2.0.516 / 2024-04-26
- fixed empty uloaded-date bug
- 2022 から選択、reports/public/2022 にコピー

## 1.18.13 - 2023-06-06
- /r/#/recv-sent からリンク

## 1.18.12 - 2023-06-05
- uploaded を日付-全体-you になるように。
  テーブルが日付バラバラ、上に詰まったら比較にならない。

## 1.18.11
REFACTOR
- Re の表示を短く

## 1.18.10 - 2023-06-05
- Calva: Server + Client での動作確認。ブラウザを立ち上げ、つなげ!
- login 失敗で赤フラッシュできるようになった
- uploaded の hkimura コラム削除

## 1.18.9 - 2023-06-04
- goods ページ、横割り, is-one-fifth

## 1.18.8 - 2023-06-04
- core.cljs/upload-column {:accept MIME-TYPE} を引数に追加した。

## 1.18.7 - 2023-06-04
- auth 切れをエラーにせずに /login にリダイレクト。

## 1.18.6 - 2023-06-03
- メッセージの使い回しはダメよ。

## 1.18.5 - 2023-06-03
- (.-rep) の警告を克服する
```
(defn- wrap-string [^String d] d)
(.-rep (wrap-string (:date r)))
```

## 1.18.4 - 2023-06-03
- 2022 学生を表示しない
- l22/api/logins は　["login" "login" ...] を返す。
- l22/api/subj/:subj を作成した。{:users [{"login": "login"} ...]} が返ってくる。

## 1.18.3 - 2023-06-03
- ログの整理
- エラーメッセージ
- メニュー、ナビの改良

## 1.18.2
- (reports.config/env :hp-url) で dev-config.edb の内容を読める。
- defined login/redirect-to-reports
### antq upgrade, after
buddy, metosin, ring を残した。一旦ここでタグをうつ。
```
% clojure -Tantq outdated
[##################################################] 52/52

|       :file |                          :name | :current |  :latest |
|------------ | ------------------------------ | -------- | ---------|
| project.clj |               buddy/buddy-core | 1.10.413 | 1.11.418 |
|             |            buddy/buddy-hashers |  1.8.158 |  2.0.162 |
|             |               buddy/buddy-sign |  3.4.333 |  3.5.346 |
|             | ch.qos.logback/logback-classic |   1.2.10 |    1.4.7 |
|             |                 metosin/reitit |   0.5.15 |    0.6.0 |
|             |                 ring/ring-core |    1.9.5 |   1.10.0 |
|             |                ring/ring-devel |    1.9.5 |   1.10.0 |

Available changes:
- https://github.com/funcool/buddy-hashers/blob/2.0.162/CHANGES.md
- https://github.com/funcool/buddy-sign/blob/3.5.346/CHANGES.md
- https://github.com/metosin/reitit/blob/0.6.0/CHANGELOG.md
- https://github.com/ring-clojure/ring/blob/1.10.0/CHANGELOG.md
```

### antq upgrade, before
```
% clojure -Tantq outdated
[##################################################] 52/52

|       :file |                                           :name |  :current |   :latest |
|------------ | ----------------------------------------------- | --------- | ----------|
| project.clj |                              binaryage/devtools |     1.0.4 |     1.0.7 |
|             |                                buddy/buddy-core |  1.10.413 |  1.11.418 |
|             |                             buddy/buddy-hashers |   1.8.158 |   2.0.162 |
|             |                                buddy/buddy-sign |   3.4.333 |   3.5.346 |
|             |                  ch.qos.logback/logback-classic |    1.2.10 |     1.4.7 |
|             |                               cider/cider-nrepl |    0.26.0 |    0.30.0 |
|             |             clojure.java-time/clojure.java-time |     0.3.3 |     1.2.0 |
|             |                       com.cognitect/transit-clj |   1.0.329 |   1.0.333 |
|             |                      com.cognitect/transit-cljs |   0.8.269 |   0.8.280 |
|             | com.google.javascript/closure-compiler-unshaded | v20220301 | v20230502 |
|             |               com.jakemccrary/lein-test-refresh |    0.24.1 |    0.25.0 |
|             |                                   conman/conman |     0.9.3 |     0.9.6 |
|             |                                       hato/hato |     0.8.2 |     0.9.0 |
|             |                                 jonase/eastwood |     0.3.5 |     1.4.0 |
|             |           luminus-migrations/luminus-migrations |     0.7.2 |     0.7.5 |
|             |                 luminus-transit/luminus-transit |     0.1.5 |     0.1.6 |
|             |               luminus-undertow/luminus-undertow |    0.1.14 |    0.1.18 |
|             |                       markdown-clj/markdown-clj |    1.11.1 |    1.11.4 |
|             |                                  metosin/reitit |    0.5.15 |     0.6.0 |
|             |                                     mount/mount |    0.1.16 |    0.1.17 |
|             |                                     nrepl/nrepl |     0.9.0 |     1.0.0 |
|             |                       org.clojure/clojurescript |   1.11.51 |   1.11.60 |
|             |                          org.clojure/core.async |   1.5.648 |   1.6.673 |
|             |                           org.clojure/tools.cli |   1.0.206 |   1.0.219 |
|             |                     org.clojure/tools.namespace |     1.2.0 |     1.4.4 |
|             |                       org.postgresql/postgresql |    42.3.2 |    42.6.0 |
|             |                           org.webjars.npm/bulma |     0.9.3 |     0.9.4 |
|             |                  org.webjars.npm/material-icons |     1.0.0 |    1.13.2 |
|             |                     org.webjars/webjars-locator |      0.42 |      0.46 |
|             |                                 reagent/reagent |     1.1.0 |     1.2.0 |
|             |                                  ring/ring-core |     1.9.5 |    1.10.0 |
|             |                              ring/ring-defaults |     0.3.3 |     0.3.4 |
|             |                                 ring/ring-devel |     1.9.5 |    1.10.0 |
|             |                                   selmer/selmer |   1.12.50 |   1.12.58 |
|             |                            thheller/shadow-cljs |    2.18.0 |    2.23.3 |

Available changes:
- https://github.com/binaryage/cljs-devtools/compare/v1.0.4...v1.0.7
- https://github.com/funcool/buddy-hashers/blob/2.0.162/CHANGES.md
- https://github.com/funcool/buddy-sign/blob/3.5.346/CHANGES.md
- https://github.com/clojure-emacs/cider-nrepl/blob/v0.30.0/CHANGELOG.md
- https://github.com/dm3/clojure.java-time/blob/1.2.0/CHANGELOG.md
- https://github.com/cognitect/transit-clj/blob/v1.0.333/CHANGES.md
- https://github.com/cognitect/transit-cljs/compare/v0.8.269...v0.8.280
- https://github.com/jakemcc/lein-test-refresh/blob/v0.25.0/CHANGES.md
- https://github.com/luminus-framework/conman/blob/head/CHANGELOG.md
- https://github.com/gnarroway/hato/blob/v0.9.0/CHANGELOG.md
- https://github.com/jonase/eastwood/blob/v1.4.0/changes.md
- https://github.com/luminus-framework/luminus-migrations/blob/head/CHANGELOG.md
- https://github.com/metosin/reitit/blob/0.6.0/CHANGELOG.md
- https://github.com/tolitius/mount/blob/head/CHANGELOG.md
- https://github.com/nrepl/nrepl/blob/1.0.0/CHANGELOG.md
- https://github.com/clojure/clojurescript/blob/r1.11.60/changes.md
- https://github.com/clojure/core.async/compare/v1.5.648...v1.6.673
- https://github.com/clojure/tools.cli/blob/v1.0.219/CHANGELOG.md
- https://github.com/clojure/tools.namespace/blob/v1.4.4/CHANGES.md
- https://github.com/pgjdbc/pgjdbc/blob/REL42.6.0/CHANGELOG.md
- https://github.com/jgthms/bulma/blob/0.9.4/CHANGELOG.md
- https://github.com/marella/material-icons/compare/v1.0.0...v1.13.2
- https://github.com/webjars/webjars-locator/compare/webjars-locator-0.42...webjars-locator-0.46
- https://github.com/reagent-project/reagent/blob/v1.2.0/CHANGELOG.md
- https://github.com/ring-clojure/ring/blob/1.10.0/CHANGELOG.md
- https://github.com/ring-clojure/ring-defaults/compare/0.3.3...0.3.4
- https://github.com/yogthos/Selmer/blob/head/changes.md
- https://github.com/thheller/shadow-cljs/blob/head/CHANGELOG.md
```

## 1.18.0 - 2023-06-01
- tiger.melt nginx の l22 設定見直し。cors 対応はアプリ(l22)に任せる。

## 1.17.3 - 2023-05-31
- update favicon.ico

## 1.17.2 - 2023-05-31

## 1.17.1 - 2023-05-31

```
reports=# \d
               List of relations
 Schema |      Name      |   Type   |  Owner
------- | -------------- | -------- | ---------
 public | goods          | table    | postgres
 public | goods_id_seq   | sequence | postgres
 public | titles         | table    | postgres
 public | titles_id_seq  | sequence | postgres
 public | uploads        | table    | postgres
 public | uploads_id_seq | sequence | postgres
(6 rows)

reports=# delete from goods;
DELETE 1721
reports=# delete from titles;
DELETE 152
reports=# delete from uploads;
DELETE 12835
reports=#
```

## 1.17.0 - 2023-05-31
- answers.md をアップロードさせ、プレビューする。
- upload from https://rp.meltlumius project
- browse from http://hp.melt:8080/ python -m http.server 8080 で。
- reports.* テーブルを初期化して tiger.melt に渡す

restart project

----
# 2022
## 0.16.0 - 2022-08-03

### 期末試験

## 0.14.0 - 2022-06-22

## 0.14.0 - 2022-06-22

## 0.13.1 - 2022-06-10
- 動かしますか？のメッセージ。

## 0.13.0 - 2022-06-09
- stop uploading

## 0.12.6 - 2022-06-07
- messages, abbrev sender/receiver
- uploaded は色付きで、
- uploaded は Upload とは別ページに。

## 0.12.4, 0.12.5 - 2022-06-07
- update message to students

## 0.12.3 - 2022-06-06
- messages
  from/send を省いて、日付とメッセージ本文を。

## 0.12.2 - 2022-06-06
- color div
- check js ?version effect
- bump-version.sh

## 0.12.1 - 2022-06-06
app.js?ver=0.12.1 でキャッシュが外れるか？
- 0.11.2 では効果なし、home.html に app.js?version=0.12.1

## 0.12.0 - 2022-06-06
### Added
- markdown table

## 0.11.2 - 2022-06-06
- キャッシュのクリアをしないでも、0.11.2 の内容が見えるように。
  <meta http-equiv="Pragma" content="no-cache"> は効果あるか？

## 0.11.0 - 2022-06-06
### Added
- Upload のページに uploaded のセクション追加。
  * 全体
  * 自分
  * hkimura
- graph に表示される REPLY を除く。
  -> 簡単にやるにはループの内側で。
  -> 効率を考えるにはループの前にフィルタする。

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

## v2.0.516 / 2024-04-26
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
