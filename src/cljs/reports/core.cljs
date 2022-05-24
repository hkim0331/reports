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

(def ^:private version "0.5.1-SNAPSHOT")
(def ^:private now (.toLocaleString (js/Date.)))

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

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]
   [:p "program: hkimura" [:br]
    "version: " version [:br]
    "update: " now]])

(defn home-page []
  (let [name js/login
        url (str js/hp_url name)]
    [:section.section>div.container>div.content
     [:h1 "UNDER CONSTRUCTION"]
     [:p "check your report => " [:a {:href url} "check"]]
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
    [:div.column s1]
    [:div.column s2 [:input {:type "file" :name "upload"}]]
    [:div.column [:button {:type "submit"} "up"]]]])

(defn upload-page []
  (let [url (str js/hp_url js/login)]
    (.log js/console "url:" url)
    [:section.section>div.container>div.content
     [:h2 "Upload"]
     [upload-column (str js/login) "/" "html"]
     [upload-column "" "/css/" "css"]
     [upload-column "" "/images/" "images"]
     [upload-column "" "/js/" "js"]
     [:p "check your report => "
      [:a {:href url} "check"]]]))

;; -------------------------
;; Browse

(defonce random? (r/atom false))

(def filters {true identity false shuffle})

(defn browse-page []
  [:section.section>div.container>div.content
   [:h2 "Browse"]
   [:p "under constrution"]
   [:p "random/hot が選びにくい。メッセージはまだ送信できない。"]

   [:div
    [:input {:type "radio"
             :checked (not @random?)
             :on-change #(swap! random? not)}]
    " radom "
    [:input {:type "radio"
             :checked @random?
             :on-change #(swap! random? not)}]
    " hot "]
   [:br]
   (for [u ((filters @random?) @users)]
     ;; ちょっと上下に開きすぎ
     [:div.columns
      [:div.column
       [:a {:href (str js/hp_url u)} u]]
      [:div.column
       " "
       [:input {:placeholder "message"}]
       [:button "send"]]])])

;; -------------------------
;; Goods

(defn goods-page []
  (let [name js/login]
    [:section.section>div.container>div.content
     [:h2 "Goods"]
     [:p "UNDER CONSTRUCTION"]]))

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
    {:error-handler (.log js/console "error: %")}))

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (reset-users!)
  (mount-components))
