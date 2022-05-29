(ns reports.core
  (:require
   [ajax.core :refer [GET POST]]
   [clojure.string :refer [replace starts-with?]]
   ;;[markdown.core :refer [md->html]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reitit.core :as reitit]
   [reports.ajax :as ajax]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

;;(set! js/XMLHttpRequest (nodejs/require "xhr2"))

(def ^:private version "0.8.7-SNAPSHOT")
(def ^:private now "2022-05-29 10:11:08")

(defonce session (r/atom {:page :home}))

;; サイトアクセス時にデータベースから取ってくる。
(defonce users (r/atom []))
(defonce goods (r/atom []))

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
       [:a {:href "#/recv-sent"} "graph"]]]]))

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
    " 人。残りは？"
    "やっつけでいけると思っていたら、それは誤解です。"
    "ページが出ません、イメージ出ません、リンクできませんって必ずなるだろう。"
    "〆切間際の質問にはじゅうぶんに答えられない。勉強にもならない。"
    "大好きな「平常点」も毎日失ってることにも気づこうな。"
    "平常点は平常につくんだ。"]
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
       [:a {:href (report-url u)} u]]
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
            #_(set! (.-innerHTML obj) ""))} "good"]]])])

;; -------------------------
;; Goods

;; 2022-05-26 時点の select login from users;
(def ^:private users-all
  #{"TyanA"
    "Iota"
    "user1"
    "user2"
    "user3"
    "ashikari"
    "hkimura"
    "nobody"
    "azangy"
    "agdp5623"
    "noppo"
    "ryo"
    "manzju"
    "hide"
    "yutaro"
    "tomas"
    "K4ZE"
    "yuzu"
    "io2"
    "sy_607"
    "kake"
    "bigblue"
    "noya04"
    "yata"
    "PASUTA"
    "nagi"
    "kyutech1"
    "Acaciapc"
    "okaneman"
    "Kotarou"
    "tatu"
    "tairanto"
    "tmkrshi"
    "username"
    "yossi"
    "maron"
    "mona"
    "kunimon"
    "yucaron"
    "erida"
    "meychan"
    "ken"
    "a1234"
    "every"
    "ri"
    "ejieji"
    "naru"
    "pocchama"
    "gagagajp"
    "smallcat"
    "yoneshan"
    "thios238"
    "Ke15"
    "hono345"
    "syotyan"
    "hayato"
    "mmkk"
    "yuto"
    "nanagawa"
    "Rice"
    "aira.4_"
    "tommy"
    "mikan"
    "uuucha"
    "da.vinch"
    "so-so"
    "soiya0"
    "alto"
    "omoti"
    "ck"
    "iree"
    "Tokei"
    "taro"
    "paru7"
    "mu"
    "Ryuuuuuu"
    "aki"
    "sonnnshi"
    "nya_ko"
    "agdy7774"
    "Kkoga"
    "jrvj82g7"
    "Watako"
    "harapeko"
    "inari"
    "hisaka64"
    "mikiya"
    "sazaesan"
    "ryusetsu"
    "makiken"
    "01pima"
    "Asagi02"
    "G.master"
    "q"
    "reishi"
    "R"
    "deees"
    "magane3"
    "ryoya121"
    "lara"
    "Feno"
    "mntzksn"
    "tikuwa"
    "nyan5103"
    "unknown"
    "yakuoto"
    "tanaka"
    "konbu"
    "AN"
    "coron"
    "AE86"
    "U1"
    "yusuke"
    "Nagassy"
    "yukinobu"
    "otokoume"
    "zjgg6h"
    "zono"
    "FK06"
    "taro0"
    "sabakan"
    "Q-taro"
    "kamera26"
    "t_ryoya"
    "tomato"
    "koosee"
    "kei"
    "mejia"
    "komatsu"
    "nabe"
    "ta-ku46"
    "takuto"
    "yuyuyu"
    "yota"
    "banane"
    "Ellla"
    "sa-mon"
    "my"
    "nanasi"
    "ramenman"
    "hibiscus"
    "waaai"
    "fd0213"
    "WiMorio"
    "dansa"
    "Badmin"
    "aryy6428"
    "masatogn"
    "hyotenup"
    "yuuuuu"
    "rayleigh"
    "taneri"
    "kitiden"
    "cheese"
    "sibuiwa"
    "burger"
    "matsusou"
    "ochi3"
    "John Doe"
    "irohasu"
    "rei"
    "harahi"
    "shiro"
    "mh"
    "593"
    "nekoneko"
    "abc"
    "tanatana"
    "marusou"
    "sirokuma"
    "tourzz"
    "Tensen"
    "monchi"
    "kouta"
    "yuchan"
    "birdman"})

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
      [:li "good! が飛ばない時はページを再読み込みしてからやり直し（ゴメンの絵文字）"]
      [:li "good! に reply で返信できます。"]
      [:li "返信のメッセージには (REPLY) がつき、
            Goods Sent に記録されず、再返信できません。"]
      [:li "Not Yet Send To は自分が一度も good! を出してない人のリスト。バグってるな。"]]
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
                      (shuffle (disj users-all (map #(:snd %) sent))))]
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

;; (defn histogram [f]
;;   (map-indexed vector (->> (group-by f @goods)
;;                            (map (fn [x] [(first x) (count (second x))])))))

;; (defn- histogram-received-page []
;;   [:section.section>div.container>div.content
;;    [:h2 "Goods " [:a {:href "/r/#/sent"} "Sent"] "/Received"]
;;    [:p "誰が何通「いいね」を受け取っているか。"]
;;    (for [[id [nm ct]] (histogram :rcv)]
;;      [:p {:key id} (good-marks ct) " → " (abbrev nm)])])

;; (defn- histogram-sent-page []
;;   [:section.section>div.container>div.content
;;    [:h2 "Goods Sent/" [:a {:href "/r/#/received"} "Received"]]
;;    [:p "誰が何通「いいね」を送ってくれたか。"]
;;    (for [[id [nm ct]] (histogram :snd)]
;;      [:p {:key id} (abbrev nm) " → " (good-marks ct)])])

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
   (let [snd (goods-f :snd)
         rcv (goods-f :rcv)
         goods (group-by :id (concat snd rcv))]
     (for [[i g] (map-indexed vector goods)]
       (let [name (abbrev (key g))
             r (-> g val (get-count :rcv) good-marks)
             s (-> g val (get-count :snd) good-marks)]
         [:p {:key i} r " → " name " → " s])))])

;; -------------------------
;; Pages

(def pages
  {:home   #'home-page
   :about  #'about-page
   :upload #'upload-page
   :browse #'browse-page
   :goods  #'goods-page
  ;;  :histogram-sent #'histogram-sent-page
  ;;  :histogram-received #'histogram-received-page
   :histogram-both #'histogram-both})

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
    ;; ["/sent" :histogram-sent]
    ;; ["/received" :histogram-received]
    ["/recv-sent" :histogram-both]]))

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

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (reset-users!)
  (reset-goods!)
  (mount-components))
