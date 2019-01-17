(ns axw.keyword-constructor)

(defn create [args & [known required internal]]
  (let [req (if (= required :all) known required)]
    (reduce
     (fn [acc kw]
       (let [ukw (keyword (name kw))
             v (get args ukw)]
         (if (contains? req kw)
           (assert (some? v) (str "Missing " ukw)))
         (-> acc
             (dissoc ukw)
             (assoc kw v)))) (merge internal args) known)))
