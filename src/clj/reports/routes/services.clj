(ns reports.routes.services
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [clojure.tools.logging :as log]
   [hato.client :as hc]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn dest-dir [login subdir]
  (let [public (:upload-to env)]
    (if (= subdir "html")
      (str public "/" login)
      (str public "/" login "/" subdir))))

(defn mkdir-p [dir]
  (sh "mkdir" "-p" dir))

;; destructuring
(defn upload!
  "受け取った multiplart-params を login/{id}/filename にセーブする。
   id = html の時は login 直下とする。[need polish up]"
  [{{:strs [type login upload]} :multipart-params :as request}]
  (let [{:keys [filename tempfile size]} upload
        dir (dest-dir login type)]
    (log/debug login type filename tempfile size)
    (log/debug dir)
    (try
      (mkdir-p dir)
      (when (empty? filename)
        (throw (Exception. "did not select a file.")))
      (io/copy tempfile (io/file (str dir "/" filename)))
      (db/create-upload! {:login login :filename filename})
      (-> (response/found "/r/#/upload")
          (assoc :flash (str "uploaded " filename)))
      (catch Exception e
        (layout/render [request] "error.html" {:message (.getMessage e)})))))

(defn get-logins [request]
  (let [ret (db/get-logins)]
    (response/ok ret)))

(defn services-routes []
  ["/api"
   {:middleware [middleware/wrap-restricted
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/ping" {:get (fn [_]
                    (response/ok {:status 200
                                  :body "pong"}))}]
   ["/upload" {:post upload!}]
   ["/logins" {:get get-logins}]])
