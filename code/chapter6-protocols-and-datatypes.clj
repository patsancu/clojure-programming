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

