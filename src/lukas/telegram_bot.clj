(ns
    lukas.telegram-bot
    (:require
     [clojure.edn :as edn]
     [compojure.core :as c]
     [compojure.route :as route]
     [morse.api :as t]
     [muuntaja.middleware :as middleware]
     [ring.adapter.jetty :as jetty]
     ;; [morse.handlers :as h]
     ))

(def config
  (merge
   (edn/read-string (slurp "tokens.edn"))
   {}))

(def token (get-in config [:telegram :token]))

;; (h/defhandler bot-api
;;   (h/command-fn
;;    "start"
;;    (fn [{{id :id :as chat} :chat}]
;;      (println "Bot joined new chat: " chat)
;;      (t/send-text token id "Welcome!")))
;;   (h/command "help" {{id :id :as chat} :chat}
;;     (println "Help was requested in " chat)
;;     (t/send-text token id "Help is on the way"))
;;   (h/message message (println "Intercepted message:" message)))

(defn
  bot-api-2
  [{{{{chat-id :id} :chat
      {first-name :first_name} :from
      :keys [text]} :message} :body-params}]
  (t/send-text
   token
   chat-id
   (str first-name " said " text)))

(c/defroutes
  app-routes
  (c/POST (str "/" token)
          req
          (bot-api-2 req))
  (route/not-found "Not Found"))

(def server (atom nil))

(defn run-jetty! []
  (reset!
   server
   (jetty/run-jetty
    (-> app-routes middleware/wrap-format)
    {:join? false :port 8080})))

(defn stop-jetty! []
  (.stop @server)
  (reset! server nil))

(comment
  (run-jetty!)
  (t/set-webhook
   token
   (str "https://helpless-yak-98.loca.lt" "/" token)))
