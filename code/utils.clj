
; BULLSHIT, just use (time (f value))
; Find out how much time a function requires in ns
; (let [start (System/nanoTime)
    ; ret (stack-consuming-fibonacci 40)
      ; end (System/nanoTime)]
   ; (println (str "Elapsed time: " (- end start) " ns"))
   ; ret)


; limit repl printing to 10 elements
(set! *print-length* 10
; 10
(iterate inc 1)
; (1 2 3 4 5 6 7 8 9 10 ...)

(deeply-nested 100)
; (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((bottom)))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))
(set! *print-level* 25)
(defn deeply-nested [n]
  (loop [n n
          result '(bottom)]
    (if (= n 0)
      result
      (recur (dec n) (list result)))))
(set! *print-level* 25)
; 25
(deeply-nested 5)
; ((((((bottom))))))
(deeply-nested 100)
; (((((((((((((((((((((((((#)))))))))))))))))))))))))


; Sleep for 100 milliseconds
(defn ^:dynamic slow-double [n]
  (Thread/sleep 100)
  (* n 2))
