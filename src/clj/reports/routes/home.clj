(ns reports.routes.home
  (:require
   [markdown.core :refer [md-to-html-string]]
   ;;[hato.client :as hc]
   [reports.config :refer [env]]
   ;;[reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   #_[ring.util.response]
   [ring.util.http-response :refer [content-type ok] :as response]))

(defn home-page [request]
  (if-let [login (get-in request [:session :identity])]
    (layout/render request "home.html" {:login  (name login) ;; string
                                        :hp-url (:hp-url env)
                                        :rp-mode (:rp-mode env)})
    (layout/error-page {:status 404
                        :title "not login"
                        :message "you need login"})))

;; ex1 answers.md
;; ex2 ex2-python.md
;; ex3 ex3-python.md
;; ex4 ex4-python.md
;; endterm endterm.md
(defn preview [{{:keys [login]} :path-params}]
  (let [path (str (:upload-to env) "/" login "/endterm.md")]
    (content-type
     (ok (md-to-html-string (slurp path)))
     "text/html")))


(defn home-routes []
  ["/r" {:middleware [middleware/wrap-restricted
                      middleware/wrap-csrf
                      middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/preview/:login" {:get preview}]])
