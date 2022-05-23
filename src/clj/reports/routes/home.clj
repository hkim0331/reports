(ns reports.routes.home
  (:require
   [hato.client :as hc]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (if-let [login (get-in request [:session :identity])]
    (layout/render [request] "home.html" {:login (name login)})
    (layout/render [request] "error.html")))


(defn upload-file [request]
  (println (:multipart-params request))
  (response/ok "ok"))


(defn home-routes []
  ["/r"
   {:middleware [middleware/wrap-restricted
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/upload" {:post upload-file}]])
