; does NOT work in modern java (18 for example)

; copied from examples/import-static.clj
(ns ^{:author "Stuart Sierra",
      :doc "Import static Java methods/fields into Clojure"}
  examples.import-static
  (:use clojure.set))

(defmacro import-static
  "Imports the named static fields and/or static methods of the class
  as (private) symbols in the current namespace.

  Example:
      user=> (import-static java.lang.Math PI sqrt)
      nil
      user=> PI
      3.141592653589793
      user=> (sqrt 16)
      4.0

  Note: The class name must be fully qualified, even if it has already
  been imported.  Static methods are defined as MACROS, not
  first-class fns."
  [class & fields-and-methods]
  (let [only (set (map str fields-and-methods))
        the-class (. Class forName (str class))
        static? (fn [x]
                    (. java.lang.reflect.Modifier
                       (isStatic (. x (getModifiers)))))
        statics (fn [array]
                    (set (map (memfn getName)
                              (filter static? array))))
        all-fields (statics (. the-class (getFields)))
        all-methods (statics (. the-class (getMethods)))
        fields-to-do (intersection all-fields only)
        methods-to-do (intersection all-methods only)
        make-sym (fn [string]
                     (with-meta (symbol string) {:private true}))
        import-field (fn [name]
                         (list 'def (make-sym name)
                               (list '. class (symbol name))))
        import-method (fn [name]
                          (list 'defmacro (make-sym name)
                                '[& args]
                                (list 'list ''. (list 'quote class)
                                      (list 'apply 'list
                                            (list 'quote (symbol name))
                                            'args))))]
    `(do ~@(map import-field fields-to-do)
         ~@(map import-method methods-to-do))))



(ns reader.snake
  (:import (java.awt Color Dimension)
	   (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:use examples.import-static))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)
; END: namespace

; TODO: implement the Snake!



(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 75)
(def win-length 5)
(def dirs { VK_LEFT  [-1  0]
            VK_RIGHT [ 1  0]
            VK_DOWN  [ 0 -1]
            VK_UP    [ 0  1]})

(defn add-points [& pts]
  (vec (apply map + pts)))

(defn point-to-screen-rect [pt]
  (map #(* point-size %)
    [(pt 0) (pt 1) [1 1]]))

(defn create-apple []
  {:location [(rand-int width) (rand-int height)]
    :color (Color. 210 50 90)
    :type :apple})

(defn create-snake []
  {:body (list [1 1])
    :dir [1 0]
    :type :snake
    :color (Color. 15 160 70) })

(defn move [{:keys [body dir] :as snake} & grow]
  (assoc snake :body (cons (add-points (first body) dir)
                           (if grow body (butlast body)))))


(move (create-snake))
; {:body ([2 1]), :dir [1 0], :type :snake, :color #object[java.awt.Color 0x2319f77e "java.awt.Color[r=15,g=160,b=70]"]}

(move (create-snake) :grow)
; {:body ([2 1] [1 1]), :dir [1 0], :type :snake, :color #object[java.awt.Color 0x22bb5cf0 "java.awt.Color[r=15,g=160,b=70]"]}

(defn win? [{body :body}]
  (>= (count body) win-length))

(win? (create-snake))

(def super-snake (move (move (move (move (move (move (move (move (move (move (move (create-snake) :grow) :grow) :grow) :grow) :grow) :grow) :grow) :grow) :grow) :grow) :grow))

(def another-super-snake (nth (iterate #(move %1 :grow) (create-snake)) 15))

(win? {:body [[1 1]]})
; false
(win? {:body [[1 1]]})
; false
(win? {:body [[1 1][2 1][3 1][4 1]]})
; false
(win? {:body [[1 1][2 1][3 1][4 1][5 1][6 1]]})
; true
(win? another-super-snake)
; true

; A snake loses if its head ever comes back into contact with the rest of its
; body.
(defn head-overlaps-body? [{[head & body] :body}]
    (contains? (set body) head))

(def lose? head-overlaps-body?)

(lose? {:body [[1 1] [1 2] [1 3]]})
; false
(lose? {:body [[1 1] [1 2] [1 1]]})
; true


(defn eats? [{[snake-head] :body} {apple :location}]
  (= snake-head apple))

(defn turn [snake newdir]
  (assoc snake :dir newdir))

(turn (create-snake) [0 -1])

; Mutable stuff with STM
(defn reset-game [snake apple]
  (dosync (ref-set apple (create-apple))
        (ref-set snake (create-snake)))
  nil)


(def test-snake (ref nil))
(def test-apple (ref nil))

(reset-game test-snake test-apple)

(defn update-direction [snake newdir]
  (when newdir (dosync (alter snake turn newdir)))
)

(defn update-positions [snake apple]
  (dosync
    (if (eats? @snake @apple)
      (do (ref-set apple (create-apple))
          (alter snake move :grow))
      (alter snake move)))
  nil)

(dosync (alter test-apple assoc :location [1 1]))


; =============================================================
;                         GUI stuff
; =============================================================

(defn fill-point [g pt color]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g color)
    (.fillRect g x y width height)))


(defmulti paint (fn [g object & _] (:type object)))

(defmethod paint :apple [g {:keys [location color]}]
  (fill-point g location color))

(defmethod paint :snake [g {:keys [body color]}]
  (doseq [point body]
  (fill-point g point color)))


(defn game-panel [frame snake apple]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (paint g @apple))
    (actionPerformed [e]
      (update-positions snake apple)
      (when (lose? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You lose!" ))
      (when (win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You win" )))
    (keyPressed [e]
      (update-direction snake (dirs (.getKeyCode e))))
    (getPreferredSize []
      (Dimension. (* (inc width) point-size)
                  (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

(defn game []
  (let [snake (ref (create-snake))
       apple (ref (create-apple))
       frame (JFrame. "snake")
       panel (game-panel frame snake apple)
       timer (Timer. turn-millis panel)]
      (doto panel
        (.setFocusable true)
        (.addKeyListener panel))
      (doto frame
        (.add panel)
        (.pack)
        (.setVisible true))
      (.start timer)
      [snake, apple, timer]))

(defn game []
  (let [snake (ref (create-snake))
  apple (ref (create-apple))
  frame (JFrame. "Snake")
  panel (game-panel frame snake apple)
  timer (Timer. turn-millis panel)]
  (doto panel
  (.setFocusable true)
  (.addKeyListener panel))
  (doto frame
  (.add panel)
  (.pack)
  (.setVisible true))
  (.start timer)
  [snake, apple, timer]))

(game)
