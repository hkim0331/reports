(ns reports.routes.services
  (:require
   [hato.client :as hc]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn services-routes []
  ["/api"
   {:middleware [middleware/wrap-restricted
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/ping" {:get (fn [_]
                    (response/ok {:status 200
                                  :body "pong"}))}]])
