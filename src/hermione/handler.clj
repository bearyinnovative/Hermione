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
           (com.qiniu.storage BucketManager)
           (com.qiniu.common QiniuException)
           (java.io File)
           (java.text SimpleDateFormat)))

(def base-url (get-property (get-config "hermione") "hermione" "baseurl"))
(def base-path (get-property (get-config "hermione") "hermione" "basepath"))
(def ak (get-property (get-config "hermione") "hermione" "ak"))
(def sk (get-property (get-config "hermione") "hermione" "sk"))
(def bucket (get-property (get-config "hermione") "hermione" "bucket"))
(def auth (Auth/create ak sk))
(def bucket-manager (BucketManager. auth))

(def sdf (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss"))

(defn gen-url
  [name]
  (let [public-url (str base-url name)]
    (.privateDownloadUrl auth public-url)))

(defn gen-path
  [name]
  (str base-path name))

(defn file-exists?
  [name]
  (try
    (do (.stat bucket-manager bucket name)
        true)
    (catch QiniuException _ false)))

(defn write-file-s
  [name]
  (try
    (clojure.java.io/copy
      (:body (client/get (gen-url name) {:as :stream}))
      (File. (gen-path name))
      true)
    (catch Exception _ false)))

(defn response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type"                 "application/json;charset=utf-8"
             "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"}
   :body    data})

(defroutes app-routes
  (GET "/api/wopi/files/:name" [name]
    (if (file-exists? name)
      (do (write-file-s name)
          (let [file (File. (gen-path name))
                size (.length file)
                sha256 (DigestUtil/getFileHash "SHA-256" (gen-path name))
                version (.format sdf (.lastModified file))]
            (response {:BaseFileName name
                       :OwnerId      "admin"
                       :Size         size
                       :SHA256       sha256
                       :Version      version})))
      (response {:error "file not found"})))
  (GET "/api/wopi/files/:name/contents" [name]
    (let [filepath (gen-path name)
          resp (file-response filepath)]
      (header resp "Content-Type" "application/octet-stream")))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      wrap-json-response))
