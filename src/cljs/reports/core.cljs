(ns reports.core
  (:require
   [ajax.core :refer [GET POST]]
   [clojure.string :refer [replace starts-with?]]
   [clojure.set :refer [difference]]
   [markdown.core :refer [md->html]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reitit.core :as reitit]
   [reports.ajax :as ajax]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

;;(set! js/XMLHttpRequest (nodejs/require "xhr2"))

(def ^:private version "1.17.1")
(def ^:private now "2023-05-31 01:51:55")

(defonce session (r/atom {:page :home}))

;; サイトアクセス時にデータベースから取ってくる。
;; atom だと、ブラウザの reload で消えちゃう。
(defonce users     (r/atom []))
(defonce goods     (r/atom []))
(defonce users-all (r/atom []))
(defonce titles    (r/atom {}))

(defonce records-all    (r/atom []))
(defonce record-hkimura (r/atom []))
(defonce record-login   (r/atom []))

(defn- admin?
  "cljs のため。
   本来はデータベーステーブル中の is-admin フィールドを参照すべき。"
  [user]
  (= "hkimura" user))

(defn- abbrev [s]
  (if (admin? js/login)
    s
    (concat (first s) (map (fn [_] "?") (rest s)))))


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
     [:p "〆切間際のやっつけは点数低い。時間をかけて、失敗しながら学ぶってレポート。"]
     [:p "check your report => "
      [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      [:li [:a {:href "#/upload"} "Upload, uploaded"]]
      [:li [:a {:href "#/browse"} "Browse & Comments"]]
      [:li [:a {:href "#/goods"}  "Goods"]
       " | "
       [:a {:href "#/recv-sent"} "Received & Sent"]
       " | "
       [:a {:href "#/messages"} "messages"]]]
     [:hr]
     "hkimura, " version]))

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

(defn- make-table [records]
  (let [s (atom "| date | uploads |\n| :---: | ---: |\n")]
    (doseq [r records]
      (swap! s concat (str "| " (.-rep (:date r)) " | " (:count r) " |\n")))
    [:div {:dangerouslySetInnerHTML
           {:__html (md->html (apply str @s))}}]))

(defn- upload-columns []
  (let [url (str js/hp_url js/login)]
    [:div
     [:h2 "Upload"]
     [:div
      [upload-column (str js/login) "/ " "html"]
      [upload-column "" "/css/ " "css"]
      [upload-column "" "/images/ " "images"]
      [upload-column "" "/js/ " "js"]]
     [:div "check your uploads => "
      [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      ;; [:li "*.md ファイルは一番上、'/' からアップロードしてください。
      ;;        プレビューは "
      ;;  [:a {:href (str "/r/preview/" js/login)} "preview"]
      ;;  " から。"]
      [:li "アップロードはファイルひとつずつ。"]
      [:li "フォルダはアップロードできない。"]
      [:li "*.html や *.css, *.png 等のアップロード先はそれぞれ違います。"]
      [:li "同じファイル名でアップロードすると上書きする。"]
      [:li "/js/ はやれる人用。授業では扱っていない。"]
      [:li "アップロードできたからってページが期待通りに見えるとは限らないよ。"]]]))

(defn- upload-ends []
  [:div
   [:h2 "Upload 停止"]
   [:p "Upload は停止中です。テスト回答、あげる時期になったら有効化する。"]])

(defn record-columns []
  [:div
   [:h3#records "Uploaded"]
   [:p "レポート出題は 5/31, レポート〆切は 6/19の正午。"]
   [:div.columns {:style {:margin-left "0rem"}}
    [:div#all.column
     [:h4 "全体"]
     (make-table @records-all)]
    [:div#you.column
     [:h4 js/login]
     (make-table @record-login)]
    [:div#hkim.column
     [:h4 "hkimura"]
     (make-table @record-hkimura)]
    [:div.column]]])

(defn upload-page []
  [:section.section>div.container>div.content
   [upload-columns]
    ;;[upload-ends]
   [:br]
   [record-columns]])

;; -------------------------
;; Browse

;; browse ページローカル。random と shuffle のどちらを表示するか。
;; 関数にローカルにできないか？
(defonce random? (r/atom false))
(def ^:private filters {true shuffle false identity})

;; send-message! と browse-page で参照する。
(def ^:private min-mesg 10)

(defn- post-message [sender receiver message]
  (POST "/api/save-message"
    {:headers {"x-csrf-field" js/csrfToken}
     :params {:snd sender
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
    "平常点は平常につく。"]
   [:ul
    [:li "good を押したあと「送信しました」が表示されない時、
        ページを再読み込みして good し直してください🙏
        再読み込みの前にメッセージはコピーしとくと吉。"]
    [:li "レポート評価基準は下の hkimura から。
          サイト開設日からそこにある。コツコツ書き足した。"]]
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
      [:div.column.is-one-quarter
       [:a {:href (report-url u)
            :class (if (= u "hkimura") "hkimura" "other")}
        u]
       " "
       (get @titles u)]
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

;; FIXME
(defn- time-format [time]
  (let [s (str time)
        date (subs s 28 39)
        time (subs s 40 48)]
    (str date " " time)))

(defn- filter-goods-by [f]
  (reverse (filter #(= js/login (f %)) @goods)))

(defn- reply? [{:keys [snd message]}]
  (when-let [msg (js/prompt "reply?")]
    (if (empty? msg)
      (js/alert "メッセージが空です。")
      (post-message js/login
                    snd
                    (str msg "(Re: " message ")")))))

(defn- abbrev-if-contains-re [s]
  (let [receiver (:rcv s)]
    (if (re-find #"\(Re:" (:message s))
      (abbrev receiver)
      receiver)))

(defn goods-page []
  (let [received (filter-goods-by :rcv)
        sent     (filter-goods-by :snd)]
    [:section.section>div.container>div.content
     [:ul
      [:li "Goods Received に表示される good! には reply で返信できます。"]
      [:li "Not Yet は自分が一度も good! を出してない人のリスト。
            青色のリンクで表示されるのは一度以上アップロードした人（ページが見えるとは限らない）。
            黒はまだ何もアップロードしない人。"]]
     [:div.columns
      [:div.column
       [:h2 "Goods Received (" (count received) ")"]
       (for [[id g] (map-indexed vector received)]
         [:p {:key (str "r" id)}
          "from " [:b (abbrev (:snd g))] ", " (time-format (:timestamp g)) ","
          [:br]
          (:message g)
          [:br]
          [:button.button.is-success.is-small
           {:on-click #(reply? g)}
           "reply"]])]
      [:div.column
       [:h2 "Goods Sent (" (count sent) ")"]
       (for [[id s] (map-indexed vector sent)]
         [:p {:key (str "g" id)}
          "to " [:b (abbrev-if-contains-re s)] ", " (time-format (:timestamp s)) ","
          [:br]
          (:message s)])]
      [:div.column
       [:h2 "Not Yet"]
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
   #_[:p "ログイン名、希望により伏せ字なんだが、どうですか？
        人気のページがどんなページか見たくない？
        たくさん good! をつけてくれる優しいお兄さんお姉さんのページ、見たくない？
        そういうの、刺激になると思うんだけどなあ。"]
   [:p "全 " (count @goods) " goods"]
   (let [snd (goods-f :snd)
         rcv (goods-f :rcv)
         goods (group-by :id (concat snd rcv))]
     (for [[i g] (map-indexed vector goods)]
       (let [name (abbrev (key g))
             r (-> g val (get-count :rcv) good-marks)
             s (-> g val (get-count :snd) good-marks)]
         (when-not (= "REPLY" (key g))
           [:p {:key i} r " → " [:b name] " → " s]))))])

;; 幼児化が進んでいる。
;; 他人から他人へのメッセージを覗き見するのはすけべよね。やめとくか。
;; のレベルではない。好き、嫌いの第一次欲求、漫画好き好きばっかだ。
(defn messages []
  [:section.section>div.container>div.content
   [:h2 "Goods (Messages)"]
   (for [g (-> @goods reverse)]
     [:p {:key (:id g)} (time-format (:timestamp g))
      ", from " [:b (abbrev (:snd g))]
      " to " [:b (abbrev (:rcv g))] ","
      [:br]
      (:message g)])])

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

(defn- setup-titles! [m]
  ;;(.log js/console (str m))
  (doseq [{:keys [login title]} m]
    (swap! titles merge {login title})))

(defn- reset-titles! []
  (GET (str "/api/titles")
    {:handler #(setup-titles! %)
     :error-handler #(.log js/console "reset-titles! error:" %)}))

(defn- reset-users-all! []
  (GET "https://l22.melt.kyutech.ac.jp/api/logins"
    {:headers {"Accept" "application/json"}
     :handler #(reset! users-all (set %))
     :error-handler #(println (str "error:" %))}))

(defn reset-records-all! []
  (GET "/api/records"
    {:handler #(reset! records-all %)
     :error-handler #(.log js/console "reset-records-all! error:" %)}))

(defn reset-record-login! []
  (GET (str "/api/record/" js/login)
    {:handler #(reset! record-login %)
     :error-handler #(.log js/console "reset-records-login! error:" %)}))

(defn reset-record-hkimura! []
  (GET "/api/record/hkimura"
    {:handler #(reset! record-hkimura %)
     :error-handler #(.log js/console "reset-records-hkimura! error:" %)}))

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (reset-users!)
  (reset-goods!)
  (reset-titles!)
  (reset-users-all!)

  (reset-records-all!)
  (reset-record-login!)
  (reset-record-hkimura!)

  (mount-components))
