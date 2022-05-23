(ns reports.routes.login
  (:require
   [buddy.hashers :as hashers]
   [hato.client :as hc]
   [reports.config :refer [env]]
   [reports.db.core :as db]
   [reports.layout :as layout]
   [reports.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn please-login-page [request]
  (response/found "/login"))

(defn login-page [{:keys [flash] :as request}]
  (layout/render request "login.html" {:flash flash}))

(defn login! [{{:keys [login password]} :params}]
  (let [url (str (:users-db env) login)
        ret (hc/get url {:as :json})
        body (:body ret)]
    (if (and (some? body) (hashers/check password (:password body)))
      (do
        (-> (response/found "/r/") ; restricted page
            (assoc-in  [:session :identity] (keyword login))))
      (do
        (-> (response/found "/login")
            (assoc :session {}
                   :flash "login failed"))))))

(defn logout! [_]
  (-> (response/found "/login")
      (assoc :session {})))
      ;;(dissoc :session) ; NG.

(defn upload-test [{params :multipart-params}]
 (response/ok params))

(defn login-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get please-login-page}]
   ["/login" {:get  login-page
              :post login!}]
   ["/logout" {:get logout!}]
   ["/upload-test" {:post upload-test}]])
