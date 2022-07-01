
; Find out how much time a function requires in ns
(let [start (System/nanoTime)
    ret (stack-consuming-fibonacci 40)
      end (System/nanoTime)]
   (println (str "Elapsed time: " (- end start) " ns"))
   ret)
