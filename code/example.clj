
(defn hello [name] (str "Hello, " name))

(hello "Mexinaco")

; empty set:
#{}
; add element to empty set
(def setWithRichie (conj #{} "Ricardo"))

(def visitors (atom #{}))
