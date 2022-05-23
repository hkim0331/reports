(ns reports.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   [markdown.core :refer [md->html]]
   [reports.ajax :as ajax]
   [ajax.core :refer [GET POST]]
   [reitit.core :as reitit]
   [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:page :home}))

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
       [nav-link "#/upload" "Upload" :upload]
       [nav-link "#/browse" "Browse" :browse]
       [nav-link "#/goods"  "Goods" :goods]
       [nav-link "/login" "Login"]
       [nav-link "/logout" "Logout"]
       [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  (let [name js/login]
    [:section.section>div.container>div.content
     [:h1 "UNDER CONSTRUCTION"]
     [:ul
      [:li [:a {:href "#/upload"} "Upload"]]
      [:li [:a {:href "#/browse"} "Browse"]]
      [:li [:a {:href "#/goods"}  "Goods"]]]]))

(defn button-up [id]
  [:button
   {:type "button"
    :on-click #(.log js/console "click " id)}
   "up"])

(defn anti-forgery-field []
  [:input {:type "hidden"
           :name "__anti-forgery-token"
           :value js/csrfToken}])

(defn upload-test []
  [:form {:method "post"
          :action "/r/upload"
          :enc-type "multipart/form-data"}
   [anti-forgery-field]
   [:input {:type "file" :name "upload-test"}]
   [:input {:type "submit"}]])

;; not ajax. form.  405 - Not allowed error.
(defn upload-column [s1 s2 id]
  [:form {:method "post"
          :action "/r/upload"
          :enc-type "multipart/form-data"}
   [anti-forgery-field]
   [:div.columns
     [:div.column s1]
     [:div.column s2 [:input {:type "file" :name "upload-test"}]]
     [:div.column [:input {:type "submit"}]]]])

(defn upload-page []
  [:section.section>div.container>div.content
   [:h2 "Upload"]
   [upload-column (str js/login) "/" "html"]
  ;;  [upload-column "" "/css" "css"]
  ;;  [upload-column "" "/images" "images"]
  ;;  [upload-column "" "/js" "js"]
   [upload-test]])

(defn browse-page []
  [:section.section>div.container>div.content
   [:h2 "Browse"]])

(defn goods-page []
  (let [name js/login]
    [:section.section>div.container>div.content
     [:p "you are " name]
     [:h2 "Goods"]
     [upload-test]]))

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
    ["/about" :about]
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

(defn init! []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
