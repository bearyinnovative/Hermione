(ns hermione.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (hermione DigestUtil)
           (java.io File)
           (java.text SimpleDateFormat)))

(def test-url "http://7xj0bp.com1.z0.glb.clouddn.com/test.docx")
(def test-path "/Users/zjh/Downloads/test.docx")

(defn write-file-s []
  (clojure.java.io/copy
    (:body (client/get test-url {:as :stream}))
    (File. test-path)))

(defroutes app-routes
  (GET "/api/wopi/files/:name" [name]
    (do
      (write-file-s)
      (let [file (File. test-path)
            sdf (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss")]
        (println "length -> " (.length file))
        (println "last modify -> " (.format sdf (.lastModified file))))
      (println "sha256 -> " (DigestUtil/getFileHash "SHA-256" test-path))
      "hehe"))
  (GET "/api/wopi/files/:name/contents" [name] (str name " file contents"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
