
: Stack consuming fibonacci
; bad idea
; already takes a lot of time with 40
(defn stack-consuming-fibonacci [n]
  (if (or (= n 0) (= n 1))
    1
    (+ (stack-consuming-fibonacci (- n 1)) (stack-consuming-fibonacci (- n 2)))
    )
  )

(for [numero (range 10)]
  (stack-consuming-fibonacci numero)
  )

; This takes a few seconds
(stack-consuming-fibonacci 35)
; This takes quite some time
(stack-consuming-fibonacci 40)


; This is several orders of magnitude faster,
; fibonacci of 1000 computed almost instantly
(defn tail-fibo [n]
  (letfn [(fib
            [current next n]
            (if (zero? n)
              current
              (fib next (+ current next) (dec n))))]
    (fib 0N 1N n)
    ))

; No real recursion, basically it's a for loop in imperative
(defn recur-fibo [n]
  (letfn [(fib
            [current next n]
            (if zero? n)
            current
            (recur next (+ current next) (dec n)))]
    (fib 0N 1N n)))


;  ------------------------ lazy sequences
(defn lazy-seq-fibo
  ([]
   (concat [0 1] (lazy-seq-fibo 0N 1N)))
  ([a b]
   (let [n (+ a b)]
     (lazy-seq (cons n (lazy-seq-fibo b n))))))

(defn fibo []
  (map first (iterate (fn [[a b]] [b (+ a b)]) [0N 1N])))


;  ------------------------ losing your head
(def head-fibo (lazy-cat [0N 1N] (map + head-fibo (rest head-fibo))))
(take 10 head-fibo)
; (nth head-fibo 1000000)
; Execution error (OutOfMemoryError) at java.math.BigInteger/add (BigInteger.java:1423).
; Java heap space

;  ------------------------ lazier than lazy

; You are giving a sequence of coin toss results, where heads is :h and tails is :t
; [:h :t :t :h :h :h]
; How many times in the sequence does heads come up twice in a row? In the
; previous example, the answer is two. Toss 3 and toss 4 are both heads, and
; toss 4 and toss 5 are both heads.
; The sequence of coin tosses might be very large, but it will be finite.

(defn count-head-pairs [coll]
  (loop [cnt 0 coll coll]
    (if (empty? coll)
      cnt
      (recur (if (= :h (first coll) (second coll))
               (inc cnt)
               cnt)
             (rest coll)))))

; So, let’s transform the sequence. When you see this:
; [:h :t :t :h :h :h]
; you should mentally translate that into a sequence of every adjacent pair:
; [[:h :t] [:t :t] [:t :h] [:h :h] [:h :h]]

; Write a function named by-pairs that performs this transformation. Because
; the output of by-pairs varies based on the size of its input, by rule 3 you should
; build this sequence lazily:


; mine, has mistakes
(defn by-pairs
  [element1 element2 & rest-of-list]
  (cons [element1 element2] (by-pairs rest-of-list))
  [lista]
  (if empty? lista
    [])
  )

; copied from book
; overly complex, better approaches follow...
(defn by-pairs [coll]
  (let [take-pair (fn [c]
                    (when (next c) (take 2 c)) )]
    (lazy-seq
      (when-let [pair (seq (take-pair coll))]
        (cons pair (by-pairs (rest coll)))))))

(defn count-head-pairs [coll]
  (count (filter (fn [pair] (every? #(= :h %) pair))
                 (by-pairs coll))))

; does the same as by-pairs
; 2 is size
; 1 is step
(partition 2  1 aSeq )


(def ^{:doc "Count items matching a filter"}
  count-if (comp count filter))

(count-if odd? [1 2 3 4 5])
; 3


(defn count-runs
  [n pred coll]
  (count-if #(every? pred %) (partition n 1 coll))
  )

(count-runs 2 #(= :h %) aSeq )
; 2

; Three tails in a row
(count-runs 3 #(= :t %) [:h :t :t :h :h :t :t :t :h] )
; 1


(def count-heads-pairs
  (partial count-runs 2 #(= :h %)))
; previous line is a nicer way of doing this:
; (fn [coll] (count-runs 2 #(= % :h) coll))

(def count-heads-pairs (partial count-runs 2 #(= % :h)))
