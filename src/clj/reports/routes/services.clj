(ns reports.routes.services
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [clojure.tools.logging :as log]
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

;; (defn mkdir-p [dir]
;;   (sh "mkdir" "-p" dir))

(defn find-title
  "テキストファイル f 中の <title> ~ </title> に挟まれる文字列を返す。
   取れないときは戻りは空文字列。一般化する？"
  [f]
  (try
    (-> (re-find #"<title>[^<]*" (slurp f))
        (subs (count "<title>")))
    (catch Exception _ "")))

(defn upsert! [login title]
 (if-let [_ (db/find-title {:login login})]
   (db/update-title! {:login login :title title})
   (db/insert-title! {:login login :title title})))

(defn upload!
  "受け取った multiplart-params を login/{id}/filename にセーブする。
   id = html の時は login 直下とする。"
  [{{:strs [type login upload]} :multipart-params :as request}]
  (let [{:keys [filename tempfile size]} upload
        dir (dest-dir login type)]
    (log/debug login type filename tempfile size)
    (log/debug dir)
    (try
      (when (empty? filename)
        (throw (Exception. "did not select a file.")))
      (sh "mkdir" "-p" dir)
      (io/copy tempfile (io/file (str dir "/" filename)))
      (db/create-upload! {:login login :filename filename})

      ;; 0.9.0, insert title into `titles` table
      (when (= "index.html" filename)
        (log/debug "when")
        (when-let [title (find-title tempfile)]
          (log/debug  "when-let")
          (upsert! login title)))

      ;; is this flash displayed?
      (-> (response/found "/r/#/upload")
          (assoc :flash (str "uploaded " filename)))
      (catch Exception e
        (layout/render [request] "error.html" {:message (.getMessage e)})))))

(defn users
  "distinct users order by `uploaded_at`"
  [_]
  (->> (db/logins-by-reverse-uploaded)
       (map :login)
       (distinct)
       (response/ok)))

(defn save-message! [{{:keys [snd rcv message]} :params}]
  (log/debug snd rcv message)
  (db/save-message! {:snd snd
                     :rcv rcv
                     :message message})
  (response/ok "sent"))

(defn goods [_]
  (response/ok (db/goods)))

(defn titles [_]
  (response/ok (db/titles)))

(defn services-routes []
  ["/api" {:middleware [middleware/wrap-restricted
                        middleware/wrap-csrf
                        middleware/wrap-formats]}
   ["/upload" {:post upload!}]
   ["/users"  {:get users}]
   ["/save-message" {:post save-message!}]
   ["/goods"  {:get goods}]
   ["/titles" {:get titles}]])
