(ns reports.routes.services
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [hato.client :as hc]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn dest-dir [login subdir]
  (let [public (:public-dir env)]
    (if (= subdir "html")
      (str public "/" login)
      (str public "/" login "/" subdir))))

(defn mkdir-p [dir]
  (sh "mkdir" "-p" dir))

(defn copy! [src dest]
  (println "copy " src " " dest)
  (io/copy src dest))

;; destructuring
(defn upload!
  "受け取った multiplart-params を login/{id}/filename にセーブする。
   id = html の時は login 直下とする。[need polish up]"
  [{{:strs [type login upload]} :multipart-params :as request}]
  (let [{:keys [filename tempfile size]} upload
        dir (dest-dir login type)]
    (println login type filename tempfile size)
    (println dir)
    (mkdir-p dir)
    (try
      (when (empty? filename)
        (throw (Exception. "did not select a file.")))
      (copy! tempfile (str dir "/" filename))
      (response/ok {:status 200 :body "under construction"})
      (catch Exception e
        (layout/render [request] "error.html" {:message (.getMessage e)})))))


(defn services-routes []
  ["/api"
   {:middleware [middleware/wrap-restricted
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/ping" {:get (fn [_]
                    (response/ok {:status 200
                                  :body "pong"}))}]
   ["/upload" {:post upload!}]])
