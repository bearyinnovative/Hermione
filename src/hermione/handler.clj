(ns hermione.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [file-response header]]
            [clj-http.client :as client]
            [mimina.config :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (hermione DigestUtil)
           (com.qiniu.util Auth)
           (java.io File)
           (java.text SimpleDateFormat)))

(def base-url (get-property (get-config "hermione") "hermione" "baseurl"))
(def base-path (get-property (get-config "hermione") "hermione" "basepath"))
(def ak (get-property (get-config "hermione") "hermione" "ak"))
(def sk (get-property (get-config "hermione") "hermione" "sk"))
(def auth (Auth/create ak sk))

(def sdf (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss"))

(defn gen-url
  [name]
  (let [public-url (str base-url name)]
    (.privateDownloadUrl auth public-url)))

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
                     :OwnerId      "admin"
                     :Size         size
                     :SHA256       sha256
                     :Version      version}))))
  (GET "/api/wopi/files/:name/contents" [name]
    (let [filepath (gen-path name)
          resp (file-response filepath)]
      (header resp "Content-Type" "application/octet-stream")))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      wrap-json-response))
