(ns reports.core
  (:require
   [ajax.core :refer [GET POST]]
   [clojure.string :refer [replace starts-with?]]
   [clojure.set :refer [difference]]
   ;;[markdown.core :refer [md->html]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reitit.core :as reitit]
   [reports.ajax :as ajax]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

;;(set! js/XMLHttpRequest (nodejs/require "xhr2"))

(def ^:private version "0.8.8")
(def ^:private now "2022-05-29 14:14:20")

(defonce session (r/atom {:page :home}))

;; サイトアクセス時にデータベースから取ってくる。
;; atom だと、ブラウザの reload で消えちゃう。
(defonce users     (r/atom []))
(defonce goods     (r/atom []))
(defonce users-all (r/atom []))

(defn- admin?
  "cljs のため。
   本来はデータベーステーブル中の is-admin フィールドを参照すべき。"
  [user]
  (= "hkimura" user))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page (:page @session)) "is-active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "#/" :style {:font-weight :bold}} "Reports"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
       [nav-link "/login" "Login"]
       [nav-link "/logout" "Logout"]
       [nav-link "#/about" "About" :about]]]]))

;; -------------------------
;; About

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]
   [:p "program: hkimura" [:br]
    "version: " version [:br]
    "update: " now]])

;; -------------------------
;; Home

(defn home-page []
  (let [name js/login
        url (str js/hp_url name)]
    [:section.section>div.container>div.content
     [:p "〆切間際のやっつけレポートは点数低い。"
      "課題の意味わかってない証拠。"]
     [:p "check your report => "
      [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      [:li [:a {:href "#/upload"} "Upload"]]
      [:li [:a {:href "#/browse"} "Browse & Comments"]]
      [:li [:a {:href "#/goods"}  "Goods"]
      ;;  " | "
      ;;  [:a {:href "#/sent"} "histogram"]
       " | "
       [:a {:href "#/recv-sent"} "graph"]
       " | "
       [:a {:href "#/messages"} "all messages"]]]]))

(defn- hidden-field [name value]
  [:input {:type "hidden"
           :name name
           :value value}])

;; -------------------------
;; Uploads

;; not ajax. form.
(defn- upload-column [s1 s2 type]
  [:form {:method "post"
          :action "/api/upload"
          :enc-type "multipart/form-data"}
   [hidden-field "__anti-forgery-token" js/csrfToken]
   [hidden-field "type" type]
   [hidden-field "login" js/login]
   [:div.columns
    [:div.column.is-one-fifth s1]
    [:div.column s2 [:input {:type "file" :name "upload"}]]
    [:div.column [:button.button.is-info.is-small {:type "submit"} "up"]]]])

(defn upload-page []
  (let [url (str js/hp_url js/login)]
    (.log js/console "url:" url)
    [:section.section>div.container>div.content
     [:h2 "Upload"]
     [:div
      [upload-column (str js/login) "/ " "html"]
      [upload-column "" "/css/ " "css"]
      [upload-column "" "/images/ " "images"]
      [upload-column "" "/js/ " "js"]]
     [:div "check your uploads => "
      [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      [:li "アップロードはファイルひとつずつ。"]
      [:li "フォルダはアップロードできない。"]
      [:li "*.html や *.css, *.png 等のアップロード先はそれぞれ違います。"]
      [:li "同じファイル名でアップロードすると上書きする。"]
      #_[:li "up ボタン押した後、
            Choose.. で選んだファイル名が No file.. に戻ったらアップロード完了。
            ただし、正しい場所にアップロードするのはユーザの責任。"]
      [:li "/js/ はやれる人用。授業では扱っていない。"]
      [:li "アップロードできたからってページが期待通りに見えるとは限らない。"]]]))

;; -------------------------
;; Browse

;; browse ページローカル。random と shuffle のどちらを表示するか。
;; 関数にローカルにできないか？
(defonce random? (r/atom false))
(def ^:private filters {true shuffle false identity})

;; send-message! と browse-page で参照する。
(def ^:private min-mesg 10)

(defn- post-message [sender receiver message & reply?]
  (POST "/api/save-message"
    {:headers {"x-csrf-field" js/csrfToken}
     :params {:snd (if reply? "REPLY" js/login)
              :rcv receiver
              :message message}
     :handler #(js/alert (str "メッセージ「" message "」を送りました。"))
     :error-handler #(.log js/console (str %))}))

(defn send-message! [recv mesg]
  (cond (< (count mesg) min-mesg)
        (js/alert (str "メッセージは " min-mesg "文字以上です。"))
        (= recv js/login)
        (js/alert "自分自身へのメッセージは送れません。")
        :else
        (post-message js/login recv mesg)))

(defn- report-url [user]
  (str js/hp_url user))

(defn browse-page []
  [:section.section>div.container>div.content
   [:h2 "Browse & Comments"]
   [:p "リストにあるのはアップロードを一度以上実行した人。合計 "
    (str (count @users))
    " 人。残りはいったい？"
    "やっつけでいけると思っていたらそれは誤解です。"
    "ページが出ません、イメージ出ません、リンクできませんって必ずなるだろう。"
    "〆切間際の質問にはじゅうぶんに答えられない。勉強にもならない。"
    "大好きな「平常点」も毎日失ってることにも気づこうな。"
    "平常点は平常につくんだ。"]
   [:ul
    [:li "good を押したあと「送信しました」が表示されない時、
        ページを再読み込みして good し直してください🙏
        再読み込みの前にメッセージはコピーしとくといいぞ。"]]
   [:div
    [:input {:type "radio"
             :checked @random?
             :on-change #(swap! random? not)}]
    " random "
    [:input {:type "radio"
             :checked (not @random?)
             :on-change #(swap! random? not)}]
    " hot "]
   [:br]
   (for [[i u] ((filters @random?) (map-indexed vector @users))]
     [:div.columns {:key i}
      [:div.column.is-one-fifth
       [:a {:href (report-url u)
            :class (if (= u js/login) "x" "y")}
           u]]
      [:div.column
       " "
       [:input {:id i
                :placeholder (str min-mesg " 文字以上のメッセージ")
                :size 80}]
       [:button
        {:on-click
         #(let [obj (.getElementById js/document i)]
            (send-message! u (.-value obj))
             ;; クリアしない方が誰にコメントしたかわかる。
            #_(set! (.-innerHTML obj) ""))} "good!"]]])])

;; -------------------------
;; Goods

(defn- time-format [time]
  (let [s (str time)
        date (subs s 28 39)
        time (subs s 40 48)]
    (str date " " time)))

(defn- filter-goods-by [f]
  (reverse (filter #(= js/login (f %)) @goods)))

(defn- reply? [sender]
  (when-let [msg (js/prompt "reply?")]
    (if (empty? msg)
      (js/alert "メッセージが空です。")
      (post-message js/login sender (str "(REPLY) " msg) true))))

(defn goods-page []
  (let [received (filter-goods-by :rcv)
        sent     (filter-goods-by :snd)]
    [:section.section>div.container>div.content
     [:ul
      [:li "Goods Received に表示される good! には reply で返信できます。"]
      [:li "返信のメッセージは Goods Sent に記録されない。"]
      [:li "goods! から届いたメッセージと違って、返信メッセージには再返信できない。
            reply ボタンないはず。"]
      [:li "Not Yet Send To は自分が一度も good! を出してない人のリスト。"]
      [:li "青色のリンクで表示されるのは一度以上アップロードした人。
            黒はまだ何もアップロードしない人。"]]
     [:div.columns
      [:div.column
       [:h2 "Goods Received"]
       (for [[id g] (map-indexed vector received)]
         [:p {:key (str "r" id)}
          (time-format (:timestamp g))
          [:br]
          (:message g)
          [:br]
          (when-not (starts-with? (:message g) "(REPLY)")
            [:button.button.is-success.is-small
             {:on-click #(reply? (:snd g))}
             "reply"])])]
      [:div.column
       [:h2 "Goods Sent"]
       (for [[id s] (map-indexed vector sent)]
         [:p {:key (str "g" id)}
          "to " [:b (:rcv s)] ", " (time-format (:timestamp s))
          [:br]
          (:message s)])]
      [:div.column
       [:h2 "Not Yet Send To"]
       (doall
        (for [[id u] (map-indexed
                      vector
                      (difference @users-all
                                  (set (map #(:rcv %) sent))))]
          [:p {:key (str "n" id)}
           (if (neg? (.indexOf @users u))
             u
             [:a {:href (report-url u)} u])]))]]]))

;; -------------------------
;; Histgram

(defn good-marks [n]
  (repeat n "👍"))

(defn abbrev [s]
  (if (admin? js/login)
    s
    (concat (first s) (map (fn [_] "?") (rest s)))))

(defn- goods-f [f]
  (->> (group-by f @goods)
       (map (fn [x] {:id (first x) f (count (second x))}))))

(defn- get-count [v key]
  (cond
    (empty? v) 0
    (get (first v) key) (get (first v) key)
    :else (get-count (rest v) key)))

;; FIXME: too complex. make this simpler.
(defn histogram-both []
  [:section.section>div.container>div.content
   [:h2 "Goods (Reveived → Who → Sent)"]
   [:p "ログイン名、伏せ字にしない方が良くね？"
    "伏字のまま人気のページにリンクで飛ばしたいんだけど、"
    "ユーザ名、リンク先でわかっちゃうんだよね。"
    "授業で学生番号や名前わかんないようにしててそれでも不十分かね？"
    "俺はそういう匿名の使い方は悪と思う。"
    "自分の作品に自信のある人は実名で、実名出せない、うしろめたい理由がある人は匿名にする？"
    "いいのはどっちか？"]
   (let [snd (goods-f :snd)
         rcv (goods-f :rcv)
         goods (group-by :id (concat snd rcv))]
     (for [[i g] (map-indexed vector goods)]
       (let [name (abbrev (key g))
             r (-> g val (get-count :rcv) good-marks)
             s (-> g val (get-count :snd) good-marks)]
         [:p {:key i} r " → " name " → " s])))])

(defn messages []
 [:section.section>div.container>div.content
  [:p "飛び交った goods を送信者、受信者を外して時系列の逆順で表示する。"]
  [:p "作成中。"]
  [:p "この前の users-all の変更 (0.8.8) がシステム上、大きかったので、
       その影響をしばらく確認する。"]
  [:p "しかし、他人から他人へのメッセージを覗き見するのはすけべよね。やめとくか。"]])
;; -------------------------
;; Pages

(def pages
  {:home   #'home-page
   :about  #'about-page
   :upload #'upload-page
   :browse #'browse-page
   :goods  #'goods-page
   :histogram-both #'histogram-both
   :messages #'messages})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :home]
    ["/about"  :about]
    ["/upload" :upload]
    ["/browse" :browse]
    ["/goods"  :goods]
    ["/recv-sent" :histogram-both]
    ["/messages"  :messages]]))

(defn match-route [uri]
  (->> (or (not-empty (replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))

;; -------------------------
;; History
;; must be called after routes have been defined

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [^js/Event.token event]
       (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-components []
  (rdom/render [#'navbar] (.getElementById js/document "navbar"))
  (rdom/render [#'page]   (.getElementById js/document "app")))

(defn- reset-users! []
  (GET "/api/users"
    {:handler #(reset! users %)}
    {:error-handler #(.log js/console "error:" %)}))

(defn- reset-goods! []
  (GET (str "/api/goods")
    {:handler #(reset! goods %)
     :error-handler #(.log js/console "reset-goods! error:" %)}))

(defn- reset-users-all! []
  (GET "https://l22.melt.kyutech.ac.jp/api/logins"
    {:headers {"Accept" "application/json"}
     :handler #(reset! users-all (set %))
     :error-handler #(println (str "error:" %))}))

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (reset-users!)
  (reset-goods!)
  (reset-users-all!)
  (mount-components))
