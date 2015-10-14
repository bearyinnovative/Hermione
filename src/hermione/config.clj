(ns hermione.config
  (:require [mimina.config :refer :all]))

(defmacro gen-config
  [name]
  `(def ~name
     (get-property
       (get-config "hermione")
       "hermione"
       (str '~name))))

(defmacro gen-configs
  [names]
  (let [map-fn (fn [name]
                 `(gen-config ~name))]
    `(do ~@(map map-fn names))))

(gen-configs [baseurl basepath ak sk bucket])