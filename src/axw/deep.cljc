(ns axw.deep
  (:require
   [clojure.set :as set]
   ))

(defn deep-merge [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn deep-diff [old new]
  (reduce-kv
   (fn [acc k new-val]
     (let [old-val (get old k)]
       (if (and (= old-val new-val)
                (contains? old k))
         acc
         (if (and (map? old-val) (map? new-val))
           (if-some [child (deep-diff old-val new-val)] (assoc acc k child) acc)
           (assoc acc k new-val)))))
   {}
   new))

(defn deep-diff-2 [old new]
  (reduce-kv
   (fn [acc k new-val]
     (let [old-val (get old k)]
       (if (= old-val new-val)
         acc
         (if (and (map? old-val) (map? new-val))
           (if-some [child (deep-diff-2 old-val new-val)] (assoc acc k child) acc)
           (assoc acc k new-val)))))
   (if (and (map? old) (map? new))
     (reduce (fn [acc k] (assoc acc k nil)) {} (set/difference (set (keys old)) (set (keys new))))
     {})
   new))

(defn deep-diff-keep [ks old new]
  (merge (deep-diff old new) (select-keys new ks)))
