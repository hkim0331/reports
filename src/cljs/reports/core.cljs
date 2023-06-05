(ns reports.core
  (:require
   [ajax.core :refer [GET POST]]
   [clojure.string :as str]
   [clojure.set :refer [difference]]
   [markdown.core :refer [md->html]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reitit.core :as reitit]
   [reports.ajax :as ajax]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   #_[cheshire.core :as json])
  (:import goog.History))

;; これは？
;; (set! js/XMLHttpRequest (nodejs/require "xhr2"))

(def ^:private version "1.18.11")
(def ^:private now "2023-06-05 21:14:39")

;-------------------------------------------
; r/atom
(defonce session   (r/atom {:page :home}))
(defonce users     (r/atom []))
(defonce goods     (r/atom []))
(defonce users-all (r/atom []))
(defonce titles    (r/atom {}))

(defonce random?    (r/atom false))
(defonce type-count (r/atom 0))


(defonce updates-by-date-all (r/atom []))
(defonce updates-by-date     (r/atom []))


(defn admin?
  "cljs のため。本来はデータベーステーブル中の is-admin フィールドを参照すべき。"
  [user]
  (= "hkimura" user))

(defn abbrev
  "いいね送信者を隠す。"
  [s]
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
       ;;[nav-link "#/" "Home" :home]
       [nav-link "#/upload" "Upload"]
       [nav-link "#/browse" "Browse"]
       [nav-link "#/goods"  "Goods"]
       [nav-link "/login"   "Login"]
       [nav-link "/logout"  "Logout"]
       [nav-link "#/about"  "About" :about]]]]))

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
     [:p "作成途中を評価するレポート。〆切際のやっつけサイトは点数低い。"]
     [:p "自分レポート => "
      [:a.button.buttun.is-warning.is-small {:href url} "チェック"]]
     [:ul
      [:li [:a {:href "#/upload"} "アップロード"]]
      [:li [:a {:href "#/browse"} "ユーザーページ、コメント送信"]]
      [:li [:a {:href "#/goods"}  "Goods"]
       [:ul
        [:li [:a {:href "#/recv-sent"} "誰から誰へ"]]
        [:li [:a {:href "#/messages"} "一覧"]]]]]
     [:hr]
     "hkimura, " version]))

(defn- hidden-field [name value]
  [:input {:type "hidden"
           :name name
           :value value}])

;; -------------------------
;; Uploads

;; not ajax. form.
(defn- upload-column
  [s1 s2 type accept]
  [:form {:method "post"
          :action "/api/upload"
          :enc-type "multipart/form-data"}
   [hidden-field "__anti-forgery-token" js/csrfToken]
   [hidden-field "type" type]
   [hidden-field "login" js/login]
   [:div.columns
    [:div.column.is-one-fifth s1]
    [:div.column s2 [:input
                     (merge {:type "file" :name "upload"} accept)]]
    [:div.column [:button.button.is-info.is-small {:type "submit"} "up"]]]])

(defn- wrap-string [^String d] d)

(defn- make-table [records]
  (let [s (atom "| date | uploads |\n| :---: | ---: |\n")]
    (doseq [r records]
      (swap! s
             concat
             (str "| "
                  (.-rep (wrap-string (:date r)))
                  " | "
                  (str (:count r))
                  " |\n")))
    [:div {:dangerouslySetInnerHTML
           {:__html (md->html (apply str @s))}}]))

(defn- upload-columns []
  (let [url (str js/hp_url js/login)]
    [:div
     [:h2 "Upload"]
     [:div
      [upload-column (str js/login) "/ " "html" {:accept "text/html"}]
      [upload-column "" "/css/ " "css" {:accept "text/css"}]
      [upload-column "" "/images/ " "images" {:accept "image/*"}]
      [upload-column "" "/js/ " "js" {:accept "text/javascript"}]]
     [:div "check your uploads => "
      [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      ;; [:li "*.md ファイルは一番上、'/' からアップロードしてください。
      ;;        プレビューは "
      ;;  [:a {:href (str "/r/preview/" js/login)} "preview"]
      ;;  " から。"]
      [:li "アップロードはファイルひとつずつ。フォルダはアップロードできない。"]
      [:li "*.html や *.css, *.png 等のアップロード先はそれぞれ違います。"]
      [:li "同じファイル名でアップロードすると上書き。"]
      [:li "アップロードできたからってページが期待通りに見えるとは限らない。"]
      [:li "アップロードが反映されない時、アレ思い出せ。"]
      [:li "/js/ は授業ではやらない JavaScript。好きもん用。"]]]))

(defn record-columns []
  [:div
   [:h3#records "Uploaded"]
   [:p "レポート〆切は 6/19 の正午。出来上がりじゃなく過程を評価するレポート。"]
   [:div.columns {:style {:margin-left "0rem"}}
    [:div#all.column
     [:h4 "全体"]
     (make-table @updates-by-date-all)]
    [:div#you.column
     [:h4 js/login]
     (make-table @updates-by-date)]
    [:div.column]]])

(defn upload-page []
  [:section.section>div.container>div.content
   [upload-columns]
   [:br]
   [record-columns]])

;; -------------------------
;; Browse
(def ^:private filters {true shuffle false identity})

;; mesg must have `min-mesg` length.
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
   [:ul
    [:li "現在までのアップロードは " (str (count @users)) "人。"
     "約" (str (- 165 (count @users))) "人はレポート平常点つかないよ。"
     [:span.red "出来上がりを評価するレポートではない"]
     "。"]
    [:li "新しいアップロードほど上。random を選ぶと順番がバラバラになる。"]
    [:li "何を目標とするレポートなのか？"
     "「写真がきれいでよかった」だと、隣の小学生と疑われないか？"
     "アップロードした人もそれで十分か？"
     "ホームページを作りながら、なんかを学ばせようと思ってんだよね、主催者は。"]
    [:li "もちろーん、接点のなかったクラスメートとこのレポートを通じて知り合えた、
          なんてサイコーと思ってるよ。"]
    [:li "メッセージのコピペ使い回しは不可。当たり前に超失礼だろ？悪質点数稼ぎじゃね？"
     "ちゃんと見て、きちんと批判しよう。批判と非難とは別物だ。"]]
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
   (doall (for [[i u] ((filters @random?) (map-indexed vector @users))]
            [:div.columns {:key i}
             [:div.column.is-one-quarter
              [:a {:href (report-url u)
                   :class (if (= u "hkimura") "hkimura" "other")}
               u]
              " "
              (get @titles u)]
             [:div.column
              " "
              [:input
               {:on-key-up #(swap! type-count inc)
                :id i
                :placeholder (str min-mesg " 文字以上のメッセージ")
                :size 80}]
              [:button
               {:on-click
                #(let [obj (.getElementById js/document i)]
                   (when (< 9 @type-count)
                     (send-message! u (.-value obj))
                     (reset! type-count 0)
                     ;; クリアしない方が誰にコメントしたかわかる。
                     (set! (.-innerHTML obj) "")))}
               "good!"]]]))])

;; -------------------------
;; Goods

;; FIXME, dirty
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
      [:li "Not Yet は自分が一度も good! を出してない人。
            青色は一度以上アップロードした人。黒はまだアップロードしない人。"]]
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
          "to "
          [:b (abbrev-if-contains-re s)]
          ", "
          (time-format (:timestamp s)) ","
          [:br]
          (:message s)])]
      [:div.column.is-one-fifth
       [:h2 "Not Yet"]
       (doall
        (for [[id u] (map-indexed
                      vector
                      (difference (set @users-all)
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

;; 他人から他人へのメッセージを覗き見するのはすけべよね。やめとくか。
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
  (->> (or (not-empty (str/replace uri #"^.*#" "")) "/")
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
  (GET "https://l22.melt.kyutech.ac.jp/api/subj/literacy"
    {:handler #(reset! users-all (->> %
                                      :users
                                      (map :login)))
     :error-handler #(.log js/console "reset-users-all!! error:" %)}))

;----------
(defn reset-updates-by-date-all! []
  (GET "/api/records"
    {:handler #(reset! updates-by-date-all %)
     :error-handler #(.log js/console "reset-updates-by-date-all! error:" %)}))

(defn reset-updates-by-date! []
  (GET (str "/api/record/" js/login)
    {:handler #(reset! updates-by-date %)
     :error-handler #(.log js/console "reset-records-login! error:" %)}))
;-----------

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)

  (reset-users!)
  (reset-goods!)
  (reset-titles!)
  (reset-users-all!)

  (reset-updates-by-date-all!)
  (reset-updates-by-date!)

  (mount-components))
