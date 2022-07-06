
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


(declare my-even? my-odd?)
(defn my-odd? [n]
    (if (= n 0)
      false
    (my-even? (dec n)))
)
(defn my-even? [n]
    (if (= n 0)
      true
    (my-odd? (dec n)))
)
(my-even? 10)

(defn parity [n]
  (loop [n n par 0]
    (if (= n 0)
      par
      (recur (dec n) (- 1 par)))))

(defn my-even? [n]  (= 0 (parity n)) )
(defn my-odd? [n]  (= 1 (parity n)) )


;  ------------------------ Trampolining
; A trampoline is a special-purpose solution to a specific problem. It requires
; doctoring your original functions to return a different type to indicate recur-
; sion. If one of the other techniques presented here provides a more elegant
; implementation for a particular recursion, that is great. If not, you will be
; happy to have trampoline in your box of tools.
; You can convert these broken, stack-consuming implementations to use
; trampoline using the same approach you used to convert tail-fibo: simply prepend
; a # to any recursive tail calls:

(trampoline + 3 2)
; 5

; Example only. Don't write code like this.
(defn trampoline-fibo [n]
  (let [fib (fn fib [f-2 f-1 current]
              (let [f (+ f-2 f-1 )]
                (if (= n current)
                  f
                  #(fib f-1 f (inc current)))))]
    (cond
      (= n 0) 0
      (= n 1) 1
      :else (fib 0N 1 2))))

; feasible, but still takes some seconds
(rem (trampoline trampoline-fibo 1000000) 1000)
; 875N

(declare my-odd? my-even?)
(defn my-odd? [n]
  (if (= n 0)
    false
    #(my-even? (dec n))))
(defn my-even? [n]
  (if (= n 0)
    true
    #(my-odd? (dec n))))

(defn deeply-nested [n]
  (loop [n n
          result '(bottom)]
    (if (= n 0)
      result
      (recur (dec n) (list result)))))


; overly-literal port, do NOT use
(declare replace-symbol replace-symbol-expression)
(defn replace-symbol [coll oldsym newsym]
  (if (empty? coll)
    ()
    (cons (replace-symbol-expression
            (first coll) oldsym newsym)
          (replace-symbol
            (rest coll) oldsym newsym))))
(defn replace-symbol-expression [symbol-expr oldsym newsym]
  (if (symbol? symbol-expr)
    (if (= symbol-expr oldsym)
      newsym
      symbol-expr)
    (replace-symbol symbol-expr oldsym newsym)))



(replace-symbol (deeply-nested 1000)  'bottom 'deepa)
; (((((((((((((((((((((((((#)))))))))))))))))))))))))
(replace-symbol (deeply-nested 100000)  'bottom 'deepa)
; Execution error (StackOverflowError) at user/replace-symbol (REPL:2).


(defn- coll-or-scalar [x & _] (if (coll? x) :collection :scalar ))
(defmulti replace-symbol coll-or-scalar)
(defmethod replace-symbol :collection [coll oldsym newsym]
  (lazy-seq
    (when (seq coll)
      (cons (replace-symbol (first coll) oldsym newsym)
        (replace-symbol (rest coll) oldsym newsym)))))

(defmethod replace-symbol :scalar [obj oldsym newsym]
  (if (= obj oldsym) newsym obj))

; hofstadter male and female sequences

(declare m f)
(defn m [n]
  (if (zero? n)
    0
    (- n (f (m (dec n))))))
(defn f [n]
  (if (zero? n)
    1
    (- n (m (f (dec n))))))

; takes already some time
(time (f 150))
; "Elapsed time: 3332.139946 msecs"
; 93


; if we rebind to a memoized function of themselves
(def m (memoize m))
(def f (memoize f))

; now it returns very fast
(time (m 200))
; "Elapsed time: 1.650714 msecs"
; 124

; The final trick is to guarantee that the cache is built from the ground up by
; exposing sequences, instead of functions. Create m-seq and f-seq by mapping
; m and f over the whole numbers:
(def m-seq (map m (iterate inc 0)))
(def f-seq (map f (iterate inc 0)))

(nth m-seq 250)
; 155
(time (nth m-seq 10000))
; "Elapsed time: 103.465194 msecs"
; 6180
