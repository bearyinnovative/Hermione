(ns hermione.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [pandect.algo.sha256 :refer :all])
  (:import (java.io File)
           (org.apache.commons.codec.binary Base64)
           (java.security MessageDigest)))

(def test-url "http://7xj0bp.com1.z0.glb.clouddn.com/test.docx")
(def test-path "/Users/zjh/Downloads/test.docx")

(defn sha256-encode
  [plain-str]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.digest digest (.getBytes plain-str))))

(defn base64-encode-str
  [plain-str]
  (->> (.getBytes plain-str)
       (.encode (Base64.))
       (String.)))

(defn base64-encode-bytes
  [plain-bytes]
  (->> plain-bytes
       (.encode (Base64.))
       (String.)))

(defn write-file-s []
  (clojure.java.io/copy
    (:body (client/get test-url {:as :stream}))
    (File. test-path)))

(def ^{:dynamic true} *default-hash* "SHA-256")

(defn hexdigest
  "Returns the hex digest of an object. Expects a string as input."
  ([input] (hexdigest input *default-hash*))
  ([input hash-algo]
   (if (string? input)
     (let [hash (MessageDigest/getInstance hash-algo)]
       (. hash update (.getBytes input))
       (let [digest (.digest hash)]
         (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))
     (do
       (println "Invalid input! Expected string, got" (type input))
       nil))))

(defroutes app-routes
  (GET "/api/wopi/files/:name" [name]
    (do
      (write-file-s)
      (let [file (File. test-path)]
        (println "length -> " (.length file)))
      (with-open [in (io/input-stream test-path)]
        (println "sha256 ->" (base64-encode-str (sha256 in))))
      (println "sha256 -> " (base64-encode-bytes (sha256-encode (slurp test-path))))
      "hehe"))
  (GET "/api/wopi/files/:name/contents" [name] (str name " file contents"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
