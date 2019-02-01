(ns axw.deep)

(defn deep-merge [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn deep-diff [old new]
  (reduce-kv
   (fn [acc k new-val]
     (let [old-val (get old k)]
       (if (= old-val new-val)
         acc
         (if (map? new-val)
           (if-some [child (deep-diff old-val new-val)] (assoc acc k child) acc)
           (assoc acc k new-val)))))
   {}
   new))

(defn deep-diff-keep [ks old new]
  (merge (deep-diff old new) (select-keys new ks)))
