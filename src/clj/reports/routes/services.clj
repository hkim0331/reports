(ns reports.routes.services
  (:require
   [hato.client :as hc]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

;; destructuring
(defn upload!
  "受け取った multiplart-params を login/{id}/filename にセーブする。
   id = html の時は login 直下とする。[need polish up]"
  [{{:strs [type login upload]} :multipart-params}]
  (let [{:keys [filename tempfile size]} upload]
    (println type login filename tempfile size)
    (response/ok {:status 200 :body "under construction"})))

(defn services-routes []
  ["/api"
   {:middleware [middleware/wrap-restricted
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/ping" {:get (fn [_]
                    (response/ok {:status 200
                                  :body "pong"}))}]
   ["/upload" {:post upload!}]])
