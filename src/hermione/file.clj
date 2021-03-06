(ns hermione.file
  (:require [hermione.config :refer :all]
            [clj-http.client :as client])
  (:import (java.io File)
           (java.text SimpleDateFormat)
           (hermione DigestUtil)
           (com.qiniu.util Auth)
           (com.qiniu.storage BucketManager)
           (com.qiniu.common QiniuException)))

(def sdf (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss"))

(def auth (Auth/create ak sk))
(def bucket-manager (BucketManager. auth))

(defn gen-public-url
  [name]
  (str baseurl name))

(defn gen-private-url
  [name]
  (->> (gen-public-url name)
       (.privateDownloadUrl auth)))

(defn gen-pdf-url
  [name]
  (->> (str (gen-public-url name) "?odconv/pdf")
       (.privateDownloadUrl auth)))

(defn gen-pdf-download-url
  [name]
  (->> (str (gen-public-url name) "?odconv/pdf&attname=abc.pdf")
       (.privateDownloadUrl auth)))

(defn gen-avinfo-url
  [name]
  (->> (str (gen-public-url name) "?avinfo")
       (.privateDownloadUrl auth)))

(defn gen-path
  [name]
  (str basepath name))

(defn gen-file-info
  [name]
  (let [file (File. (gen-path name))]
    {:BaseFileName name
     :OwnerId      "admin"
     :Size         (.length file)
     :SHA256       (DigestUtil/getFileHash "SHA-256" (gen-path name))
     :Version      (.format sdf (.lastModified file))}))

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
      (:body (client/get (gen-private-url name) {:as :stream}))
      (File. (gen-path name))
      true)
    (catch Exception _ false)))
