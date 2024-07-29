(ns reports.points
  (:require [reports.db.core :as db]
            [clojure.edn]
            [md5.core]))

;; compile error
;; (def ^:private users (db/points))
(defn users [] (db/points))

(defn- alias- [login]
  (str "s" (-> login
               md5.core/string->md5-hex
               (subs 2 6))))

(defn- make-edn [outfile]
  (spit outfile
        (pr-str
         (for [{:keys [id from_user to_user pt timestamp]} (users)]
           {:id        id
            :from      (alias- from_user)
            :to        (alias- to_user)
            :pt        pt
            :timestamp timestamp}))))

(defn- make-csv [outfile]
  (spit outfile
        (apply str
               (for [{:keys [id from_user to_user pt timestamp]}
                     (sort-by :id (users))]
                 (str (apply str
                             (interpose ","
                                        [id
                                         (alias- from_user)
                                         (alias- to_user)
                                         pt
                                         (str timestamp)]))
                      "\n")))))

(comment
  (make-edn "data/point.edn")
  (make-csv "data/points.csv")

  ;; 2024-06-13
  (count users) ; => 1104
  (filter #(and (= (:from_user %) (:to_user %)) (= "A" (:pt %)))
          users)

  (count users)
  (->> users
       (map :from_user)
       set
       count)

  (->> (map :from_user users)
       sort
       (group-by identity)
       (map #(list (key %) (count (val %)))))

  :rcf)
