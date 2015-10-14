(ns hermione.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response]]
            [clj-http.client :as client]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (hermione DigestUtil)
           (java.io File)
           (java.text SimpleDateFormat)))

(def base-url "http://7xj0bp.com1.z0.glb.clouddn.com/")
(def base-path "/Users/zjh/Downloads/")

(def sdf (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss"))

(defn gen-url
  [name]
  (str base-url name))

(defn gen-path
  [name]
  (str base-path name))

(defn write-file-s
  [name]
  (clojure.java.io/copy
    (:body (client/get (gen-url name) {:as :stream}))
    (File. (gen-path name))))

(defn response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type"                 "application/json;charset=utf-8"
             "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"}
   :body    data})

(defroutes app-routes
  (GET "/api/wopi/files/:name" [name]
    (do (write-file-s name)
        (let [file (File. (gen-path name))
              size (.length file)
              sha256 (DigestUtil/getFileHash "SHA-256" (gen-path name))
              version (.format sdf (.lastModified file))]
          (response {:BaseFileName name
                     :OwerId       "admin"
                     :Size         size
                     :SHA256       sha256
                     :Version      version}))))
  (GET "/api/wopi/files/:name/contents" [name] (str name " file contents"))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      wrap-json-response))
