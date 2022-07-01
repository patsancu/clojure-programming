
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
