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

(defn home-routes []
  ["/r"
   {:middleware [middleware/wrap-restricted
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/never" {:get (fn [_] {:status 200
                            :body "not-authenticated uses shoud never come here"})}]])
