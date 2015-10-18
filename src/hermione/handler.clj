(ns hermione.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [file-response header]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hermione.file :refer :all]))

(defn response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type"                 "application/json;charset=utf-8"
             "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"}
   :body    data})

(defn get-file-info
  [name]
  (if (file-exists? name)
    (do (write-file-s name)
        (response (gen-file-info name)))
    (response {:error "file not found"})))

(defn get-file-content
  [name]
  (let [filepath (gen-path name)
        resp (file-response filepath)]
    (header resp "Content-Type" "application/octet-stream")))

(defn get-pdf-file
  [name]
  (gen-pdf-url name))

(defroutes app-routes
  (GET "/api/wopi/files/:name" [name]
    (get-file-info name))
  (GET "/api/wopi/files/:name/contents" [name]
    (get-file-content name))
  (GET "/api/pdf/files/:name" [name]
    (get-pdf-file name))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      wrap-json-response))
