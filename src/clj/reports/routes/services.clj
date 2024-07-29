(ns reports.routes.services
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [hato.client :as hc]
   [markdown.core :refer [md-to-html-string]]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   ;; [ring.util.response] ;; from ring
   [ring.util.http-response :refer [content-type ok] :as response]))

(defn dest-dir [login subdir]
  (let [public (:upload-to env)]
    (if (= subdir "html")
      (str public "/" login)
      (str public "/" login "/" subdir))))

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

(defn upload!
  "受け取った multiplart-params を login/{type}/filename にセーブする。
   type = html の時は login 直下とする。"
  [{{:strs [type login upload]} :multipart-params :as request}]
  (let [{:keys [filename tempfile size]} upload
        dir (dest-dir login type)
        dest (io/file dir filename)]
    (try
      (when (empty? filename)
        (throw (Exception. "choose a file to upload.")))
      (sh "mkdir" "-p" dir)
      (log/info login type filename size dir)
      (when (zero? size)
        (throw (Exception. "size is 0")))
       ;; 2023-08-23 md ファイル以下には md だけ
      (when (= type "md")
        (when-not (str/ends-with? filename ".md")
          (throw (Exception. "*.md only"))))
      (io/copy tempfile dest)
      (when (zero? (count (slurp dest)))
        (throw (Exception. "saved file length is 0")))
      (db/create-upload! {:login login :filename filename})
      (when (= "index.html" filename)
        (when-let [title (find-title tempfile)]
          (upsert! login title)))
      (log/info login "upload success")
      (response/found (str (reports.config/env :hp-url) login))
      ;; midterm exam, 2023-06-12.
      ;; {:status 200
      ;;  :headers {"content-type" "text/html"}
      ;;  :body "upload success (exam mode)"}
      ;;
      ;; endterm, 2024-07-31.
      (response/found "https://rp.melt.kyutech.ac.jp/r/#/")
      (catch Exception e
        (let [message (.getMessage e)]
          (log/error "upload! error:" login message)
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

(defn report-pt! [{params :body-params}]
  (log/debug "params:" params)
  (response/ok (db/insert-point params)))

(defn- to-map
  "[{:pt p, :count c}...] => {:p :c, ...}"
  [a]
  (apply merge (map (fn [m] {(:pt m), (:count m)}) a)))

(defn points-from [{{:keys [login]} :path-params}]
  (response/ok (-> (db/points-from {:login login}) to-map)))

(defn points-to [{{:keys [login]} :path-params}]
  (response/ok (-> (db/points-to {:login login}) to-map)))

(defn url->path [url]
  )

(defn markdown-path [path]
  (md-to-html-string (slurp path)))

(defn md [request]
  (if-let [login (get-in request [:session :identity])]
    (let [url (str (:hp-url env) (name login) "/md/endterm.md")
          path (str "public/" (name login) "/md/endterm.md")]
      (content-type (ok (markdown-path path)) "text/html"))
    (layout/render request "error.html" {:flash (:flash request)})))

(defn services-routes []
  ["/api" {:middleware [(if (:dev env) identity middleware/wrap-restricted)
                        middleware/wrap-csrf
                        middleware/wrap-formats]}
   ["/md" {:get md}]
   ["/upload" {:post upload!}]
   ["/users"  {:get users}]
   ["/save-message" {:post save-message!}]
   ["/goods"  {:get goods}]
   ["/titles" {:get titles}]
   ["/records" {:get records-all}]
   ["/record/:login" {:get record-login}]
   ["/report-pt" {:post report-pt!}]
   ["/points-from/:login" {:get points-from}]
   ["/points-to/:login" {:get points-to}]])
