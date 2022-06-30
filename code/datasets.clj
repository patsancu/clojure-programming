(require '[clojure.string :as str])

(def aseq [1 2 3])

; DO NOT use it directly without `take` or something like that
(def whole-numbers (iterate inc 1))

(take 10 whole-numbers)

(def fruits ["apples", "bananas", "grapes", "oranges"])

(def letters ["A" "B" "C" "D" "E"])


(def upperLetters (for [numero (range 26)] (str (char (+ 65 numero)))))

(def lowerLetters (for [numero (range 26)] (str (char (+ 97 numero)))))

(def letters (concat lowerLetters upperLetters ))

(def vowels-raw [\a \e \i \o \u])
(def vowels (for [letter vowels-raw]
  (str letter)
))

(def vowel? #{\a\e\i\o\u})

; Exercise: try to generate consonants from vowels
