(ns reports.core
  (:require
   [ajax.core :refer [GET POST]]
   [clojure.string :as str]
   [clojure.set :refer [difference]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [reitit.core :as reitit]
   [reports.ajax :as ajax]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   ;
   md5.core)
  (:import goog.History))

(def ^:private version "v2.10.647")
(def ^:private now "2024-08-27 09:54:24")

;-------------------------------------------
; r/atom
(defonce goods     (r/atom []))
(defonce session   (r/atom {:page :home}))
(defonce titles    (r/atom {}))
(defonce users     (r/atom []))
(defonce users-all (r/atom []))

(defonce random?    (r/atom false))
(defonce type-count (r/atom 0))

(defonce uploads-by-date-all (r/atom []))
(defonce uploads-by-date     (r/atom []))

(def ^:private how-many 10)
(defonce users-selected (r/atom nil))

(defonce pt-sent (r/atom nil))
(defonce pt-recv (r/atom nil))

;; -------------------------
;; Miscellaneous

(defn- wrap-string [^String d] d)

(defn js-date [s] (.-rep (wrap-string s)))

(defn coerce-date-count
  [m]
  (apply merge
         (map (fn [x] {(js-date (:date x)) (:count x)})
              m)))

(defn- report-url [user]
  (str js/hp_url user))

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

(defn- hidden-field [name value]
  [:input {:type "hidden"
           :name name
           :value value}])

;; -------------------------
;; navbar

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
       #_[nav-link "#/browse" "Browse"]
       #_[nav-link "#/goods"  "Goods"]
       #_[nav-link "/login"   "Login"]
       [nav-link "/logout"  "Logout"]
       [nav-link "#/about"  "About" :about]]]]))

;; -------------------------
;; About

(defn about-page
  []
  (fn []
    [:section.section>div.container>div.content
     [:img {:src "/img/warning_clojure.png"}]
     [:p "program: hkimura" [:br]
      "version: " version [:br]
      "update: " now]]))

;; -------------------------
;; Home

(defn home-page
  []
  (fn []
    (let [thing "re-re-exam"
          name js/login
          url (str js/hp_url name)]
      [:section.section>div.container>div.content
       [:p (str
            thing ".zip から取り出した "
            thing ".md に回答を上書き、アップロードする。 => ")
        [:a.button.buttin.is-danger.is-small {:href "/r/#/upload"} "回答"]]
       #_[:p "レポートは作成途中とCSSが評価点。〆切際のやっつけサイトは点数低い。"]
       #_[:p "自分レポート => "
        [:a.button.buttun.is-warning.is-small {:href url} "チェック"]]
       [:p "期末テスト回答（ちゃんとマークダウンできたか） => "
        [:a.button.buttun.is-warning.is-small {:href "/api/md"} "チェック"]]
       #_[:ul
        [:li [:a {:href "#/upload"} "アップロード"]]
        [:li [:a {:href "#/browse"} "ユーザーページ（ABCD 準備完了、6/18 23:59 までに）"]]
        [:li [:a {:href "#/goods"}  "自分が出した goods, 自分に届いた goods"]]
        [:li [:a {:href "#/day-by-day"} "日々の goods"]]
        [:li [:a {:href "#/recv-sent"} "誰から誰へ goods が飛んでるか"]]
        [:li [:a {:href "#/messages"} "Goods の内容一覧（後日、最新の n 件にします）"]]]
       [:hr]
       "hkimura, " version])))


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
    [:div.column [:button.button.is-danger.is-small {:type "submit"} "up"]]]])

(defn- upload-columns []
  (let [url (str js/hp_url js/login)]
    [:div
     [:h2 (str "Upload " js/login)]
     [:p "上書きした re-re-exam.md のセーブを確認後、up すること。"]
     [:div
      ;; re-re-exam のため、不必要な upload-column を見せない。
      ;;             loigin placeholder type accept
      ;; [upload-column (str js/login) "/ " "html" {:accept "text/html"}]
      ;; [upload-column "" "/css/ " "css" {:accept "text/css"}]
      ;; [upload-column "" "/images/ " "images" {:accept "image/*"}]
      ;; [upload-column "" "/movies/ " "movies" {:accept "video/*"}]
      ;; [upload-column "" "/sounds/" "sounds" {:accept "audio/mp3"}]
      ;; [upload-column "" "/js/ " "js" {:accept "text/javascript"}]
      ;; [upload-column "" "zip " "zip" {:accept "application/zip"}]
      [upload-column "" "md "  "md"   {:accept "text/markdown"}]]
     #_[:div "check your uploads => "
        [:a.button.buttun.is-warning.is-small {:href url} "check"]]
     #_[:div "check your markdown =>"
        [:a.button.button.is-warning.is-small {:href "/api/md"} "endterm.md"]]
     #_[:ul
        [:li "アップロードはファイルひとつずつ。フォルダはアップロードできない。"]
        [:li "*.html や *.css, *.png 等のアップロード先はそれぞれ違います。"]
        [:li "同じファイル名でアップロードすると上書き。"]
        [:li "アップロードできたからってページが期待通りに見えるとは限らない。"]
        [:li "アップロードが反映されない時、ブラウザの履歴（キャッシュ）をクリア。"]
        [:li "/js/ は授業ではやらない JavaScript。好きもん用。"]
        [:li "md から markdown 以外をアップロードするのは間違いです。"]]
     [:hr]
     "hkimura, " version]))

;; FIXME: @uploads-by-date は nil のケースがある。
(defn uploaded-column
  []
  [:div
   [:h3#records "Uploaded"]
   [:p "中間試験は6/5って。その1週間前は5/29だ。"]
   [:div.columns
    [:div.column.is-one-third
     [:table.table.is-striped
      [:thead [:tr [:th "date"] [:th "全体"] [:th js/login]]]
      [:tbody
       (for [date (sort (keys @uploads-by-date-all))]
         [:tr
          [:td date]
          [:td (@uploads-by-date-all date)]
          [:td (when-not (empty? @uploads-by-date)
                 (@uploads-by-date date))]])]]]]])

(defn upload-page
  []
  (fn []
    [:section.section>div.container>div.content
     [upload-columns]
     [:br]
     #_[uploaded-column]]))

;; -------------------------
;; Browse & Comments

(def ^:private filters {true shuffle false identity})

;; mesg must have `min-mesg` length.
(def ^:private min-mesg 10)

(defn- post-message! [sender receiver message]
  (POST "/api/save-message"
    {:headers {"x-csrf-field" js/csrfToken}
     :params {:snd sender
              :rcv receiver
              :message message}
     :handler #(js/alert (str "メッセージ「" message "」を送りました。"))
     :error-handler #(do
                       (js/alert "送信失敗。時間をおいて再送信してください。")
                       (.log js/console (str %)))}))

(defn- browse-comments
  []
  [:div
   [:h2 "Browse & Comments"]
   [:ul
    [:li "現在までのアップロードは " (str (count @users)) "人。"]
    [:li "新しいアップロードほど上。random を選ぶと順番がバラバラになる。"]
    [:li "ホームページのプログラム内容に関係するコメント、質問、回答が
            ボコボコ交換されるのを期待してます。"]]])

(defn browse-page
  []
  (fn []
    [:section.section>div.container>div.content
     [browse-comments]
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
     (doall
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
          [:input
           {:on-key-up #(swap! type-count inc)
            :id i
            :placeholder (str min-mesg " 文字以上のメッセージ")
            :size 80}]
          [:button
           {:on-click
            #(let [mesg (.-value (.getElementById js/document i))]
               (cond (< (count mesg) min-mesg)
                     (js/alert (str "メッセージは " min-mesg " 文字以上です。"))
                     (= u js/login)
                     (js/alert "自分自身へのメッセージは送れません。")
                     :else
                     (post-message! js/login u mesg)))}
           "good!"]]]))]))

;; -------------------------
;; Student Page

(defn- send-report-point!
  [from to pt]
  (POST "/api/report-pt"
    {:headers {"x-csrf-field" js/csrfToken}
     :params {:from from
              :to to
              :pt pt}
     ;; :handler #(js/alert (str "send " from "->" to ": " pt))
     :handler #(swap! pt-sent update pt inc)
     :error-handler #(js/alert "送信失敗。時間をおいて再送信してください。")}))

(defn- send-students-pt
  ;; no use opt if use (random-uuid)
  [from to pt opt]
  [:button.button
   {:on-click #(do
                 (send-report-point! from to pt)
                 ;; remove to from @users-selected
                 (swap! users-selected disj to))
    :key (random-uuid)} pt])

(defn students-page
  []
  (fn []
    [:section.section>div.container>div.content
     [:div.columns
      [:div.column
       [:h3 "please send your pt"]
       (doall
        (for [[i u] (map-indexed vector @users-selected)]
          [:div.columns {:key i}
           [:div.column
            [:a {:href (report-url u)
                 :class "other"
                 :target "_blank"}
             u]
            " "
            (get @titles u)]
           [:div.column
            (for [[i p] (map-indexed vector ["A" "B" "C" "D"])]
              (send-students-pt js/login u p {:key i}))]]))]
      [:div.column
       [:h3 "points sent"]
       [:p (str (sort @pt-sent))]
       [:br]
       [:h3 "points received"]
       [:p (str (sort @pt-recv))]
       [:button.button.
        {:on-click
         (fn [e]
           (GET (str "/api/points-to/" js/login)
             {:handler #(reset! pt-recv %)
              :error-handler #(js/alert "can not set pt-recv")}))}
        "update"]]]]))

;; -------------------------
;; Exam Page
(defn exam-page
  []
  (fn []
    [:section.section>div.container>div.content
     [:div
      [:h2 "中間試験"]
      [:ul
       [:li "試験中は他の人のページを見れません。"]
       [:li "自分回答は Reports あるいは Upload の check ボタンから。"]]]]))

(defn secret-page
  []
  (fn []
    [:section.section>div.container>div.content
     [:div
      [:h2 "おめでとう " js/login " !"]
      [:p "secret は" (-> (md5.core/string->md5-hex js/login)
                         (subs 0 6))]]]))

;; -------------------------
;; Goods

(defn- time-format [time]
  (let [s (str time)
        date (subs s 28 39)
        time (subs s 40 48)]
    (str date " " time)))

(defn- shorten
  "shorten string `s` max `n` chars."
  ([s] (shorten s 20))
  ([s n] (let [pat (re-pattern (str "^(.{" n "}).*"))]
           (str/replace-first s pat "$1..."))))

(defn- reply? [{:keys [snd message]}]
  (when-let [msg (js/prompt "reply?")]
    (if (empty? msg)
      (js/alert "メッセージが空です。")
      (post-message! js/login
                     snd
                     (str msg "(Re: " (shorten message) ")")))))

(defn- abbrev-if-contains-re [s]
  (let [receiver (:rcv s)]
    (if (re-find #"\(Re:" (:message s))
      (abbrev receiver)
      receiver)))

(defn- filter-goods-by [f]
  (->> @goods
       (filterv #(= js/login (f %)))))

(defn- received-column
  [received]
  [:div.column
   [:h2 "Goods Received (" (count received) ")"]
   (doall
    (for [g received]
      [:p {:key (str "r" (:id g))}
       "from " [:b (abbrev (:snd g))] ", " (time-format (:timestamp g)) ","
       [:br]
       (:message g)
       [:br]
       [:button.button.is-success.is-small
        {:on-click #(reply? g)}
        "reply"]]))])

(defn- sent-column
  [sent]
  [:div.column
   [:h2 "Goods Sent (" (count sent) ")"]
   (doall
    (for [s sent]
      [:p {:key (str "g" (:id s))}
       "to "
       [:b (abbrev-if-contains-re s)]
       ", "
       (time-format (:timestamp s)) ","
       [:br]
       (:message s)]))])

(defn- not-yet-column
  [sent]
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
         [:a {:href (report-url u)} u])]))])

(defn goods-page
  []
  (fn []
    (let [received (filter-goods-by :rcv)
          sent     (filter-goods-by :snd)]
      [:section.section>div.container>div.content
       [:ul
        [:li "Goods Received に表示される good! には reply で返信できます。"]
        [:li "Not Yet は自分が一度も good! を出してない人。
            青色は一度以上アップロードした人。黒はまだアップロードしない人。
              ほぼ、去年の情報リテラシー受講生だな。"]]
       [:div.columns
        [received-column received]
        [sent-column sent]
        [not-yet-column sent]]])))

;; -------------------------------------
;; messages received-sent

(defn- goods-f [f]
  (->> (group-by f @goods)
       (map (fn [x] {:id (first x) f (count (second x))}))))

(defn- get-count [v key]
  (cond
    (empty? v) 0
    (get (first v) key) (get (first v) key)
    :else (get-count (rest v) key)))

;; FIXME: too complex. make this simpler.
(defn recv-sent
  []
  [:section.section>div.container>div.content
   [:h2 "Goods (Reveived → Who → Sent)"]
   [:p "全 " (count @goods) " goods"]
   (let [snd (goods-f :snd)
         rcv (goods-f :rcv)
         goods (group-by :id (concat snd rcv))]
     (for [[i g] (map-indexed vector goods)]
       (let [name (key g)
             r (-> g val (get-count :rcv) (repeat "🌞"))
             s (-> g val (get-count :snd) (repeat "🌳"))]
         (when-not (= "REPLY" (key g))
           [:p {:key i} r " → "
            [:a {:href (report-url name)
                 :class (if (= name js/login)
                          "me"
                          "other")}
             name]
            ;; (if (= name js/login)
            ;;   [:a {:href (report-url name)} name]
            ;;   (abbrev name))
            " → " s]))))])

(defn messages []
  [:section.section>div.container>div.content
   [:h2 "Goods (Messages)"]
   (for [g @goods]
     [:p {:key (:id g)} (time-format (:timestamp g))
      ", from " [:b (abbrev (:snd g))]
      " to " [:b (abbrev (:rcv g))] ","
      [:br]
      (:message g)])])

;; -------------------------
;; messages day by day

(defn- sent-goods
  [login]
  (->> (filter #(= login (:snd %)) @goods)
       (map #(.-rep (wrap-string (:timestamp %))))
       (map #(subs % 0 10))
       sort
       (group-by identity)
       (map (fn [x] [(key x) (count (val x))]))
       sort))

(defn day-by-day
  ([] (day-by-day js/login))
  ([who]
   [:section.section>div.container>div.content
    [:h2 "Goods sent, day by day(" who ")"]
    (for [g (sent-goods who)]
      [:li (first g) ", " (second g)])]))

;; -------------------------
;; Pages

;; FIXME: does not determine the value of js/rp_mde in compile time.
(def pages
  {:home   #'home-page
   :secret #'secret-page
   :about  #'about-page
   :upload #'upload-page
   :browse (case js/rp_mode
             "exam" #'exam-page
             "student" #'students-page
             #'browse-page)
   :goods  #'goods-page
   :recv-sent #'recv-sent
   :messages  #'messages
   :day-by-day #'day-by-day})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :home]
    ["/secret" :secret]
    ["/about"  :about]
    ["/upload" :upload]
    ["/browse" :browse]
    ["/goods"  :goods]
    ["/recv-sent" :recv-sent]
    ["/messages"  :messages]
    ["/day-by-day" :day-by-day]]))

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
    {:handler #(do
                 (reset! users %)
                 (reset! users-selected
                         (apply sorted-set (take how-many (shuffle @users)))))}
    {:error-handler #(.log js/console "error:" %)}))

(defn- reset-goods! []
  (GET "/api/goods"
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

;------------------------------------------------

(defn reset-uploads-by-date-all! []
  (GET "/api/records"
    {:handler #(reset! uploads-by-date-all (coerce-date-count %))
     :error-handler #(.log js/console "reset-uploads-by-date-all! error:" %)}))

(defn reset-uploads-by-date!
  [user]
  (GET (str "/api/record/" user)
    {:handler #(reset! uploads-by-date (coerce-date-count %))
     :error-handler #(.log js/console "reset-records-login! error:" %)}))

;----------------------------------------------------------------

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)

  (reset-users!)
  (reset-goods!)
  (reset-titles!)
  (reset-users-all!)
  (reset-uploads-by-date-all!)
  (reset-uploads-by-date! js/login)

  (GET (str "/api/points-from/" js/login)
    {:handler #(reset! pt-sent %)
     :error-handler #(js/alert "can not set pt-sent")})

  (GET (str "/api/points-to/" js/login)
    {:handler #(reset! pt-recv %)
     :error-handler #(js/alert "can not set pt-recv")})

  (mount-components))
