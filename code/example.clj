
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
