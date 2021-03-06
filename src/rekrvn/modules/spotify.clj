(ns rekrvn.modules.spotify
  (:require [rekrvn.hub :as hub])
  (:require [http.async.client :as c])
  (:use [cheshire.core]))

(def modName "spotify")

(defn niceify [track]
  (str "♫ " (-> track :track :artists first :name) " - " (:name (:track track))" ♫"))

(defn apiLookup [id]
  (with-open [client (c/create-client)]
    (let [url (str "http://ws.spotify.com/lookup/1/.json?uri=spotify:track:" id)
          response (c/GET client url)]
      (c/await response)
      (c/string response))))

(defn trackStr [[linktype id] reply]
  (when-let [jsn (apiLookup id)]
    (let [parsed (parse-string jsn true)
          msg (niceify parsed)]
      (if (re-matches #"spotify:track:" linktype)
        (reply modName (str msg "   http://open.spotify.com/track/" id))
        (reply modName msg)))))

(hub/addListener
  modName #"(http://open.spotify.com/track/|spotify:track:)([\d\w]+)" trackStr)
