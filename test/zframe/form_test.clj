(ns zframe.form-test
  (:require
   [zframe.core :as zf]
   [zframe.form :as f]
   [matcho.core :as matcho]
   [clojure.test :refer :all]))


{:select/load {:method 'do/load-options
               :mock [{:id "o1"} {:id "o2"}]}}

(zf/defx load-options
  [{db :db} opts]
  (println ::load-options opts))

(def schema
  {:name {:validate {:zf/required {}
                     :zf/min-length {:value 5}}}
   :select {:init {:event ::load-options}}
   :zf/submit {:event ::submit}})



(deftest test-zform
  (is (= "a.b.c"
         (f/get-id {:zf/root [:a :b] :zf/path [:c]})
         ))
  (is (= "fqn/keyword.b.c"
         (f/get-id {:zf/root [:fqn/keyword :b] :zf/path [:c]})
         ))


  (matcho/match
   (f/form-init-actions [] {:zf/root [::form] :zf/path []} schema)
   [[:zf.form-test/load-options
     {:event :zf.form-test/load-options,
      :opts #:zf{:root [:zf.form-test/form], :path [:select]}}]])

  (f/form-init {:zf/root [::form] :schema schema})

  (def name-opts {:zf/root [::form] :zf/path [:name]})

  (f/set-value (assoc name-opts :value "aaa"))

  (is (= "aaa" @(zf/subscribe [:zf/value name-opts])))


  (matcho/match
   (f/set-schema {} {:zf/root [::form] :zf/path [:name]} :VALUE [:subpath])
   #:zf.form-test{:form {:schema {:name {:subpath :VALUE}}}})



  (matcho/match
   @(zf/subscribe [:zf/error name-opts])
   #:zf{:min-length "Should be longer then 5"})

  (f/set-value (assoc name-opts :value ""))

  (matcho/match
   @(zf/subscribe [:zf/error name-opts])
   #:zf{:required "Field is required"})

  (f/submit name-opts)


  (matcho/match
   @(zf/subscribe [:zf/form-errors name-opts])
   {:name #:zf{:required "Field is required"}})

  (f/set-value (assoc name-opts :value "aaabbb"))

  (nil? @(zf/subscribe [:zf/form-errors name-opts]))


  (f/form-init {:zf/root [::form] :schema schema})

  (nil? @(zf/subscribe [:zf/form-errors name-opts]))

  (f/submit name-opts)

  (matcho/match
   @(zf/subscribe [:zf/form-errors name-opts])
   {:name #:zf{:required "Field is required"}})


  (matcho/match
   (f/calculate-errors {} schema {:name nil})
   {:name #:zf{:required "Field is required"}})


  (matcho/match
   (f/calculate-errors {} schema {:name "aaabbb"})
   nil?)

  (matcho/match
   (f/calculate-errors {:name {:zf/required "Ups"}} schema {:name "aaabbb"})
   nil?)


  (is
   (->
    (f/set-state {} {:zf/root [:root] :zf/path [:name]} [:popup] true)
    (f/state {:zf/root [:root] :zf/path [:name]} [:popup])))


  )
