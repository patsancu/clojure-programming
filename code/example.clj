
(defn hello [name] (str "Hello, " name))

(hello "Mexinaco")

; empty set:
#{}
; add element to empty set
(def setWithRichie (conj #{} "Ricardo"))

(def visitors (atom #{}))
; add element to atom (set)

(swap! visitors conj "Elvis")
(swap! visitors conj "Little Richard")

(def visitors (atom #{}))


(def visitors (atom #{"first" "second"}))

(require 'examples.introduction)

; in order not to have the ugly examples.introduction/fibs
(refer 'examples.introduction)

; this is the same as require+refer
(use 'examples.introduction)

(use :reload 'examples.introduction)

(use 'clojure.repl)
(source identity)

(find-doc "reduce")


; ===========
;  Functions
; ===========
(defn greeting
"Returns a greeting in the form of 'Hello, {username}'"
[username]
(str "Hello, " username))


(defn greeting
"Returns a greeting in the form of 'Hello, {username}'"
([] (greeting "world"))
([username] (str "Hello, " username))
)

(defn date [person-1 person-2 & chaperones]
  (println person-1 "and" person-2 "went out with" (count chaperones) "chaperones")
)

(defn patata [a b c & d]
  (str a b c d (vector? d) (list? d)))
(patata "first" "second" "third" "fourth" "fifth")

; --------------------
; anonymous functions
; --------------------
(filter
  (fn [w] (> (count w) 2)) ; anonymous function
  (str/split "A fine day it is" #"\W+"))


; with implicit param names: %1 (or just %), %2, ...
(filter
  #(> (count %) 2) ; anonymous function
  (str/split "A fine day it is" #"\W+"))

(defn make-greeter [greeting-prefix]
  (fn [username] (str greeting-prefix ", " username)))

; let
(defn square-corners [bottom left size]
  (let [top (+ bottom size)
        right (+ left size)]
  [[bottom left] [top left] [top right] [bottom right]]))

; destructuring
(defn greet-author-1 [author]
  (println "Hello, " (:first-name author)))

(greet-author-1 {:last-name "Vinge" :first-name "Vernor" })

(defn greet-author-2 [{fname :first-name}]
  (println "Hello, " fname)
)

(greet-author-2 {:last-name "Vinge" :first-name "Vernor" })

; Bind only two first values from vector
(let [ [x y] [1 2 3]]
  [x y])

; Bind only third value from vector
(let [ [_ _ z] ["a" "b" "c" "d" "e" "f"]]
  [z])

; Bind both individual values and whole collection
(let [[x y :as coords] [1 2 3 4 5 6]]
  (str "x: " x, " y: " y, ", total dimensions: " (count coords )))

; Exercise:
; Try using destructuring to create an ellipsize function. ellipsize should take a
; string and return the first three words followed by ....
(defn ellipsize [words]
  (let [ [w1 w2 w3] (str/split words #"\s+") ]
  (str/join " "  [w1 w2 w3 "…"] )))
(ellipsize "first second third fourth")


; Namespaces
; switch namespaces
(def foo "bar")
(in-ns 'my-cool-namespace)
foo ; not available
(def foo "bar")
foo

; If you switch namespaces, maybe a good idea to import clojure core. Otherwise basic stuff like `defn` don't work
(clojure.core/use 'clojure.core)
; Any java package outside java.lang must be fully qualified
File/separator ; Not gonna work
java.io.File/separator

; Or, better still import the package
(import '(java.io InputStream File))
File/separator

(.exists (File. "/tmp"))

(ns examples.patrick
  (:require [clojure.string :as str])
  (:import (java.io File))
)
(.exists (File. "/tmp"))


; Calling Java
(def rnd (new java.util.Random))
(. rnd nextInt)

(. rnd nextInt 120)

(. Math PI)

(import '(java.util Random Locale)
        '(java.text MessageFormat))

Random

; Flow control
(defn is-small? [number]
  (if (< number 100) "yes" (str "no, it's not")))

; Side effects with `do`
(defn is-small? [number]
  (if (< number 100)
    "yes"
    (do
      (println "Saw a big number" number)
      "no")
  ))

(loop [result [] x 5]
  (if (zero? x)
    result
    (recur (conj result x) (dec x))))

(defn countdown [result x]
  (if (zero? x)
    result
    (recur (conj result x) (dec x))))
(countdown [] 10)

(into [] (take 5 (iterate dec 5)) )

(into [] (drop-last (reverse (range 6))) )

(vec (reverse (rest (range 10))))

; Where's the `for` loop

; public static int indexOfAny(String str, char[] searchChars) {
;   if (isEmpty(str) || ArrayUtils.isEmpty(searchChars)) {
;     return -1;
;   }
;   for (int i = 0; i < str.length(); i++) {
;     char ch = str.charAt(i);
;     for (int j = 0; j < searchChars.length; j++) {
;       if (searchChars[j] == ch) {
;         return i;
;       }
;     }
;   }
;   return -1;
; }

(defn indexed [coll] (map-indexed vector coll))
(indexed "abcde")

(defn index-filter [pred coll]
  (when pred
    (for [[idx elt] (indexed coll) :when (pred elt)] idx)))
(index-filter #{\a \b} "abcdbbb")
(index-filter #{\a \b} "xyz")

(defn index-of-any [pred coll]
  (first (index-filter pred coll))
)

(index-of-any #{\z \a} "zzabyycdxx")
(index-of-any #{\b \y} "zzabyycdxx")
(index-of-any #{\h \p} "zzabyycdxx")

; Metadata

(defn ^{:tag String} shout [^{:tag String} s]
  (.toUpperCase s))

; or, more concisely:
(defn ^String shout [^String s])

(meta #'shout)
; examples.patrick=> (meta #'shout)
; {:tag java.lang.String, :arglists ([s]), :line 1, :column 1, :file "/tmp/form-init12562403159401401501.clj", :name shout, :ns #object[clojure.lang.Namespace 0x572ffe02 "examples.patrick"]}







; ====================
; Chapter 3: Sequences
; ====================
(def aseq [1 2 3])

(type aseq)
(class aseq)
(first aseq)
(rest aseq)
(class (rest aseq))

(cons 7 aseq)
; (7 1 2 3)

(class aseq)
(class (seq aseq))

(next aseq)

(first {:fname "John" :flast "Wayne"})
;[:fname "John"]
(rest {:fname "John" :flast "Wayne"})
;([:flast "Wayne"])
(cons [:mname "MF"] {:fname "John" :flast "Wayne"})

; also for sets
(rest #{:the :quick :brown :fox})
;(:the :quick :brown)
(first #{:the :quick :brown :fox})
;:fox
(cons :jumped #{:the :quick :brown :fox})
;(:jumped :fox :the :quick :brown)

(conj '(1 2 3) :a)
;(:a 1 2 3)
(into '(1 2 3) '(:a :b))
;(:b :a 1 2 3)
(into '(1 2 3) [9 99 999])
;(999 99 9 1 2 3)


(repeat 10 "x")
;("x" "x" "x" "x" "x" "x" "x" "x" "x" "x")

; DON'T RUN ALONE! HANGS
; (iterate inc 1)
; iterate begins with a value x and continues forever, applying a function f to each value to calculate the next.
(take 200 (iterate inc 1))

(take 5 (iterate #(+ 3 %1) 5))

; Powers of two
(take 20 (iterate #(* 2 %1) 2))

; Odd numbers
(take 20 (iterate #(+ 2 %1) 1))

(take 10 (iterate #(str "a" %1) ""))
























































; ==============================
; ==============================
; My playground
; ==============================
; ==============================
(defn factorial [n]
  (if (= n 0)
      1
      (* n (factorial (- n 1)))
  )
)

(defn fibonacci [n]
  (println "computing number", n)
  (if (or (= n 1) (= n 0))
      1
      (+ (fibonacci (- n 1)) (fibonacci (- n 2)))
  )
)

(def memo-fibonacci (memoize fibonacci))


(time (memo-fibonacci 10))
(time (fibonacci 10))

; Copied from da internet
(defn fibo-recur-java-bigint
  [^long n]
  (loop [curr (BigInteger/valueOf 0)
         next (BigInteger/valueOf 1)
         n n]
    (if-not (zero? n)
      (recur next (.add curr next) (dec n))
      curr)))
(def fib-memo fibo-recur-java-bigint)
(time (fib-memo 1000))
(time (fib-memo 1000))


; List comprehensions
(for [x (range 1 10)
      :let [ y (square x) ]
      :when (odd? x) ]
  y)

;   cartesian product
(for [x [1 2 3 4 5]
      y ["a" "b" "c" "d"]]
    [x y]
)
; ([1 "a"] [1 "b"] [1 "c"] [1 "d"] [2 "a"] [2 "b"] [2 "c"] [2 "d"] [3 "a"] [3 "b"] [3 "c"] [3 "d"] [4 "a"] [4 "b"] [4 "c"] [4 "d"] [5 "a"] [5 "b"] [5 "c"] [5 "d"])

; Foobar
(defn aux [x]
  (if (= 0 (mod x 5))
    (if (= 0 (mod x 3))
      "foobar"
      "bar"
    )
    (if (= 0 (mod x 3))
      "foo"
      x
    )
    )
)

(defn foobar [limit]
  (for [i (range 1, limit)
    :let [x (aux i)]]
  x
)
)
