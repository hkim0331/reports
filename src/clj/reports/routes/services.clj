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
   取れないときは戻りは nil"
  [f]
  (try
    (-> (re-find #"<title>[^<]*" (slurp f))
        (subs (count "<title>")))
    (catch Exception _ nil)))

(defn upsert! [login title]
 (if-let [_ (db/find-title {:login login})]
   (db/update-title! {:login login :title title})
   (db/insert-title! {:login login :title title})))

(comment
  (count (slurp "/tmp/hello.txt"))
  :rcf)

(defn upload!
  "受け取った multiplart-params を login/{type}/filename にセーブする。
   type = html の時は login 直下とする。"
  [{{:strs [type login upload]} :multipart-params :as request}]
  (let [{:keys [filename tempfile size]} upload
        dir (dest-dir login type)
        dest (io/file dir filename)]
    (log/info "dir:" dir "dest:" dest)
    (try
      (when (empty? filename)
        (throw (Exception. "choose a file to upload.")))
      (sh "mkdir" "-p" dir)
 ;;;;
      (log/info type login filename size dir tempfile)
      (when (zero? size)
        (throw (Exception. "upload parameter size is 0")))
      (when (zero? (count (slurp tempfile)))
        (throw (Exception. "upload file length is 0")))
      (io/copy tempfile dest)
      (when (zero? (count (slurp dest)))
        (throw (Exception. "saved file length is 0")))

      (db/create-upload! {:login login :filename filename})
      (when (= "index.html" filename)
        ;; (log/info "uploaded index.html:" (slurp tempfile))
        (when-let [title (find-title tempfile)]
          ;; (log/info "upload! found title" title)
          (upsert! login title)))
      (log/info login "upload success")

      ;; FIXME URL
      (response/found (str "https://hp.melt.kyutech.ac.jp/" login))

      (catch Exception e
        (let [message (.getMessage e)]
          (log/error "upload! error:" message)
          (layout/render [request] "error.html" {:message message}))))))

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

(defn records-all [request]
  (response/ok (db/records)))

(defn record-login [{{:keys [login]} :path-params}]
  (log/debug "record-login login" login)
  (response/ok (db/record {:login login})))

(defn services-routes []
  ["/api" {:middleware [middleware/wrap-restricted
                        middleware/wrap-csrf
                        middleware/wrap-formats]}
   ["/upload" {:post upload!}]
   ["/users"  {:get users}]
   ["/save-message" {:post save-message!}]
   ["/goods"  {:get goods}]
   ["/titles" {:get titles}]
   ["/records" {:get records-all}]
   ["/record/:login" {:get record-login}]])
