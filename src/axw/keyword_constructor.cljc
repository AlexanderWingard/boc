(ns axw.keyword-constructor)

(defn create [args & [known required defaults]]
  (let [req (if (= required :all) known required)
        defaults-merged (merge defaults args)]
    (reduce
     (fn [acc kw]
       (let [ukw (keyword (name kw))
             v (get defaults-merged ukw)]
         (if (contains? req kw)
           (assert (some? v) (str "Missing " ukw)))
         (-> acc
             (dissoc ukw)
             (assoc kw v)))) defaults-merged known)))
