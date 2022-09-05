(ns examples.gulp
  (:import (java.io FileInputStream InputStreamReader BufferedReader)))

(defn gulp [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (-> src
                           FileInputStream.
                           InputStreamReader.
                           BufferedReader.)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))


(ns examples.expectorate
  (:import (java.io FileOutputStream OutputStreamWriter BufferedWriter)))

(defn expectorate [dst content]
  (with-open [writer (-> dst
                         FileOutputStream.
                         OutputStreamWriter.
                         BufferedWriter.)]
    (.write writer (str content))))

(def path-to-write-to "/home/code/programming-clojure-book/output/example.clj")
(expectorate path-to-write-to "SOME RANDOM STUFF")

(gulp path-to-write-to)


(defn make-reader [src]
  (-> src FileInputStream. InputStreamReader. BufferedReader.)
)

(defn make-writer [dst]
  (-> dst FileOutputStream. OutputStreamWriter. BufferedWriter.)
)



; Refactor to use the new functions
(defn gulp [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (make-reader src)]
      (loop [c (.read reader)]
        (if neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader)))))))

(defn expectorate [dst content]
  (with-open [writer (make-writer dst)]
    (.write writer (str content))))


; =========================================

; FAILS!! Interface methods must not contain '-'
; (definterface IOFactory
  ; (^java.io.BufferedReader make-reader [this])
  ; (^java.io.BufferedWriter make-writer [this]))

; REALLY NOT THE POINT, JUST AS AN EXAMPLE
(definterface IOFactory
  (^java.io.BufferReader makeReader [this])
  (^java.io.BufferedWriter makeWriter [this]))


(defprotocol IOFactory
  "A protocol for things that can be read from and written to."
  (make-reader [this] "Creates a BufferedReader.")
  (make-writer [this] "Creates a BufferedWriter."))


(extend InputStream
  IOFactory
  {:make-reader (fn [src]
                  (-> src InputStreamReader. BufferedReader.))
  :make-writer (fn [dst]
                  (throw (IllegalArgumentException. "Can't open as an InputStream.")))})

(extend OutputStream
  IOFactory
  {:make-reader (fn [src]
                  (throw (IllegalArgumentException.
                    "Can't open as an OutputStream.")))
  :make-writer (fn [dst]
                  (-> dst OutputStreamWriter. BufferedWriter.))})

(extend-type File
  IOFactory
  (make-reader [src]
    (make-reader (FileInputStream. src)))
  (make-writer [dst]
    (make-writer (FileOutputStream. dst)))
)

; extend-protoco is like multiple times extend-type
(extend-protocol IOFactory
  InputStream
  (make-reader [src]
    (-> src InputStreamReader. BufferedReader.))
  (make-writer [dst]
    (throw
      (IllegalArgumentException.
        "Can't open as an InputStream.")))

  OutputStream
  (make-reader [src]
    (throw
      (IllegalArgumentException.
        "Can't open as an OutputStream.")))
  (make-writer [dst]
    (-> dst OutputStreamWriter. BufferedWriter.))

  File
  (make-reader [src]
    (make-reader (FileInputStream. src)))
  (make-writer [dst]
    (make-writer (FileOutputStream. dst)))

  Socket
  (make-reader [src]
    (make-reader (.getInputStream src)))
  (make-writer [dst]
    (make-writer (.getOutputStream dst)))

  URL
  (make-reader [src]
    (make-reader
      (if (= "file" (.getProtocol src))
        (-> src .getPath FileInputStream.)
        (.openStream src)
        )))
  (make-writer [src]
    (make-writer
      (if (= "file" (.getProtocol src))
        (-> dst .getPath FileInputStream.)
        (throw (IllegalArgumentException. "Can't write to non-file URL")))))
  )



; ===================================================================
; ============================ DATATYPES ============================
(ns examples.cryptovault-complete
  (:require [clojure.java.io :as io]
            [examples.protocols.io :as proto])
  (:import (java.security KeyStore KeyStore$SecretKeyEntry
                          KeyStore$PasswordProtection)
           (javax.crypto Cipher KeyGenerator CipherOutputStream

                         CipherInputStream)

           (java.io FileInputStream FileOutputStream)))

(defprotocol Vault
  (init-vault [vault])
  (vault-output-stream [vault])
  (vault-input-stream [vault]))
(defn vault-key [vault]
  (let [password (.toCharArray (.password vault))]
    (with-open [fis (FileInputStream. (.keystore vault))]

      (-> (doto (KeyStore/getInstance "JCEKS")
            (.load fis password))
          (.getKey "vault-key" password)))))
(deftype CryptoVault [filename keystore password]
  Vault
  (init-vault [vault]
    (let [password (.toCharArray (.password vault))
          key (.generateKey (KeyGenerator/getInstance "AES"))
          keystore (doto (KeyStore/getInstance "JCEKS")

                     (.load nil password)
                     (.setEntry "vault-key"

                                (KeyStore$SecretKeyEntry. key)
                                (KeyStore$PasswordProtection. password)))]

      (with-open [fos (FileOutputStream. (.keystore vault))]
        (.store keystore fos password))))
  (vault-output-stream [vault]
    (let [cipher (doto (Cipher/getInstance "AES")

                   (.init Cipher/ENCRYPT_MODE (vault-key vault)))]
      (CipherOutputStream. (io/output-stream (.filename vault)) cipher)))
  (vault-input-stream [vault]
    (let [cipher (doto (Cipher/getInstance "AES")

                   (.init Cipher/DECRYPT_MODE (vault-key vault)))]
      (CipherInputStream. (io/input-stream (.filename vault)) cipher)))
  proto/IOFactory
  (make-reader [vault]
    (proto/make-reader (vault-input-stream vault)))
  (make-writer [vault]
    (proto/make-writer (vault-output-stream vault))))
(extend CryptoVault
  clojure.java.io/IOFactory
  (assoc io/default-streams-impl
         :make-input-stream (fn [x opts] (vault-input-stream x))
         :make-output-stream (fn [x opts] (vault-output-stream x))))


; ===================== RECORDS =====================

(defrecord Note [pitch octave duration])
(->Note :D# 4 1/2)

(.pitch (->Note :D# 4 1/2))

(map? (->Note :D# 4 1/2))

; Modify stuff
(assoc (->Note :D# 4 1/2) :pitch :Db :duration 1/4)
(update-in (->Note :D# 4 1/2) [:octave] inc)

; Records are open, we can add fields
(assoc (->Note :D# 4 1/2)  :velocity 180)

(defprotocol MidiNote
  (to-msec [this tempo])
  (key-number [this])
  (play [this tempo midi-channel]))

(import 'javax.sound.midi.MidiSystem)
(extend-type Note
  MidiNote
  (to-msec [this tempo]
    (let [duration-to-bpm {1 240, 1/2 120, 1/4 60, 1/8 30, 1/16 15}]
      (* 1000 (/ duration-to-bpm (:duration this))
         tempo))))

(defn key-number [this]
            (let [scale {:C 0, :C# 1, :Db 1, :D 2,
                         :D# 3, :Eb 3, :E 4, :F 5,
                         :F# 6, :Gb 6, :G 7, :G# 8,
                         :Ab 8, :A 9, :A# 10, :Bb 10,
                         :B 11}]
              (+ (* 12 (inc (:octave this)))
                 (scale (:pitch this)))))

(defn play [this tempo midi-channel]
  (let [velocity (or (:velocity this) 64)]
    (.noteOn midi-channel (key-number this) velocity)
    (Thread/sleep (to-msec this tempo))))

(defn perform [notes & {:keys [tempo] :or {tempo 120}}]
  (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
    (let [channel (aget (.getChannels synth) 0)]
      (doseq [note notes]
        (play note tempo channel)))))

(def close-encounters [(->Note :D 3 1/2)
                       (->Note :E 3 1/2)
                       (->Note :C 3 1/2)
                       (->Note :C 2 1/2)
                       (->Note :G 2 1/2)])

(def jaws (for [duration [1/2 1/2 1/4 1/4 1/8 1/8 1/8 1/8]
                pitch [:E :F]]
            (Note. pitch 2 duration)))
