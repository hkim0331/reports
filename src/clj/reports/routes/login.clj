(ns reports.routes.login
  (:require
   [buddy.hashers :as hashers]
   [hato.client :as hc]
   [reports.config :refer [env]]
   ;;[reports.db.core :as db]]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   ;; [ring.util.response]
   [ring.util.http-response :as response]))

(defn please-login-page [_]
  (response/found "/login"))

(defn login-page [{:keys [flash] :as request}]
  (layout/render request "login.html" {:flash flash}))

(defn- redirect-to-reports
  [login]
  (-> (response/found "/r/") ; restricted page
      (assoc-in  [:session :identity] (keyword login))))

(defn login! [{{:keys [login password]} :params}]
  (if (reports.config/env :dev)
    (redirect-to-reports login)
    (let [url (str (:users-db env) login)
          ret (hc/get url {:as :json})
          body (:body ret)]
      (if (and (some? body) (hashers/check password (:password body)))
        (redirect-to-reports login)
        (-> (response/found "/login")
            (assoc :session {}
                   :flash "login failed"))))))

(defn logout! [_]
  (-> (response/found "/login")
      (assoc :session {})))
      ;;(dissoc :session))) ; NG. マジで？ マジのようだ。

(defn login-routes []
  ["" {:middleware [middleware/wrap-csrf
                    middleware/wrap-formats]}
   ["/" {:get please-login-page}] ;; login-page にするとエラーの理由は？
   ["/ping" {:get (fn [_] {:status 200
                           :body {:ping "pong"}})}]
   ["/login"  {:get  login-page
               :post login!}]
   ["/logout" {:get  logout!}]])
