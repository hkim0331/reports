(ns reports.middleware
  (:require
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth.backends.session :refer [session-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [clojure.tools.logging :as log]
   [muuntaja.middleware :refer [wrap-format wrap-params]]
   #_[reports.config :refer [env]]
   [reports.env :refer [defaults]]
   [reports.layout :refer [error-page render]]
   [reports.middleware.formats :as formats]
   [ring.adapter.undertow.middleware.session :refer [wrap-session]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.flash :refer [wrap-flash]]
   ))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    (error-page
     {:status 403
      :title "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

;; (defn on-error [request response]
;;   (error-page
;;    {:status 403
;;     :title (str "Access to " (:uri request) " is not authorized")}))

(defn on-error [_request _response]
  ;; (render request
  ;;         "login.html"
  ;;         {:flash (str "Access to " (:uri request) " is not authorized")}))
 {:status 304
  :location "/login"
  })

(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(defn wrap-auth [handler]
  (let [backend (session-backend)]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))
      wrap-internal-error))
