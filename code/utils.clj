
; Find out how much time a function requires in ns
(let [start (System/nanoTime)
    ret (stack-consuming-fibonacci 40)
      end (System/nanoTime)]
   (println (str "Elapsed time: " (- end start) " ns"))
   ret)


; limit repl printing to 10 elements
(set! *print-length* 10
; 10
(iterate inc 1)
; (1 2 3 4 5 6 7 8 9 10 ...)
