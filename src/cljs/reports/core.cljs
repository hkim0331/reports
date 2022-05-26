(ns reports.core
  (:require
   [ajax.core :refer [GET POST]]
   [clojure.string :as string]
   ;;[markdown.core :refer [md->html]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reitit.core :as reitit]
   [reports.ajax :as ajax]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

(def ^:private version "0.7.0-SNAPSHOT")
(def ^:private now "2022-05-26 10:50:22")

(defonce session (r/atom {:page :home}))
(defonce users (r/atom []))



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
      ;; 不細工だからやめよう
      ;;[:span "Reports"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
      ;; ちょっとうるさい
      ;;  [nav-link "#/upload" "Upload" :upload]
      ;;  [nav-link "#/browse" "Browse" :browse]
      ;;  [nav-link "#/goods"  "Goods" :goods]
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
      [:br]
      "課題の意味わかってない証拠。"]
     [:p "check your report => "
      [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      [:li [:a {:href "#/upload"} "Upload"]]
      [:li [:a {:href "#/browse"} "Browse"]]
      [:li [:a {:href "#/goods"}  "Goods"]]]]))

(defn hidden-field [name value]
  [:input {:type "hidden"
           :name name
           :value value}])

;; -------------------------
;; Uploads

;; not ajax. form.
(defn upload-column [s1 s2 type]
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
     [:p
      [upload-column (str js/login) "/ " "html"]
      [upload-column "" "/css/ " "css"]
      [upload-column "" "/images/ " "images"]
      [upload-column "" "/js/ " "js"]]
     [:p "check your uploads => "
       [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     [:ul
      [:li "アップロードはファイルひとつずつ。"]
      [:li "フォルダはアップロードできない。"]
      [:li "*.html や *.css, *.png 等のアップロード先はそそれぞれ違います。"]
      [:li "同じファイル名でアップロードすると上書きする。"]
      [:li "/js/ はやれる人用。授業では扱っていない。"]
      [:li "アップロードできたからってページが期待通りに見えるとは限らない。"]]]))

;; -------------------------
;; Browse

(def min-mesg 20)

(defn send-message! [recv mesg]
  (cond (< (count mesg) min-mesg)
        (js/alert (str "メッセージは " min-mesg "文字以上です。"))
        (= recv js/login)
        (js/alert "自分自身へのメッセージは送れません。")
        :else
        (POST "/api/save-message"
          {:headers {"x-csrf-field" js/csrfToken}
           :params {:snd js/login
                    :rcv recv
                    :message mesg}
           :handler #(js/alert (str recv " に " mesg "を送った。"))
           :error-handler #(.log js/console (str %))})))

(defonce random? (r/atom false))

(def filters {true identity false shuffle})

(defn report-url [user]
  (str js/hp_url user))

(defn browse-page []
  [:section.section>div.container>div.content
   [:h2 "Browse"]
   [:p "リストにあるのはアップロードを一度以上実行した人。合計 "
    (str (count @users))
    " 人。"]
   [:div
    [:input {:type "radio"
             :checked (not @random?)
             :on-change #(swap! random? not)}]
    " random "
    [:input {:type "radio"
             :checked @random?
             :on-change #(swap! random? not)}]
    " hot "]
   [:br]
   (for [[i u] (map-indexed vector ((filters @random?) @users))]
     ;; ちょっと上下に開きすぎ
     [:div.columns
      [:div.column.is-one-fifth
       [:a {:href (report-url u)} u]]
      [:div.column
       " "
       [:input {:id i :placeholder "message" :size 60}]
       [:button
        {:on-click
         #(let [obj (.getElementById js/document i)]
            (send-message! u (.-value obj))
             ;;FIXME クリアしない。
            (set! (.-innerHTML obj) ""))} "good!"]]])])

;; -------------------------
;; Goods

(defonce goods (r/atom []))
(defonce sents (r/atom []))

(def users-all
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

(defn time-format [time]
  (let [s (str time)
        date (subs s 28 39)
        time (subs s 40 48)]
    (str date " " time)))

(defn goods-page []
  [:section.section>div.container>div.content
   [:div.columns
    [:div.column
     [:h2 "Goods Received"]
     (for [[id g] (map-indexed vector @goods)]
       [:p {:key id}
        (time-format (:timestamp g))
        [:br]
        (:message g)])]
    [:div.column
     [:h2 "Goods Sent"]
     (for [[id s] (map-indexed vector @sents)]
       [:p {:key id}
        "To " (:rcv s) ", " (time-format (:timestamp s))
        [:br]
        (:message s)])]
    [:div.column
     [:h3 "Not Yet"]
     (for [[i u] (map-indexed vector (sort (disj users-all @sents)))]
       [:p {:key i} [:a {:href (report-url u)} u]])]]])

;; -------------------------
;; Pages

(def pages
  {:home   #'home-page
   :about  #'about-page
   :upload #'upload-page
   :browse #'browse-page
   :goods  #'goods-page})

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
    ["/goods"  :goods]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
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
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn reset-users! []
  (GET "/api/users"
    {:handler #(reset! users %)}
    {:error-handler #(.log js/console "error:" %)}))

(defn reset-goods! []
  (GET (str "/api/goods/" js/login)
    {:handler #(reset! goods %)
     :error-handler #(.log js/console "error:" %)}))

(defn reset-sents! []
  (GET (str "/api/sents/" js/login)
    {:handler #(reset! sents %)
     :error-handler #(.log js/console "error:" %)}))

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (reset-users!)
  (reset-goods!)
  (reset-sents!)
  (mount-components))
