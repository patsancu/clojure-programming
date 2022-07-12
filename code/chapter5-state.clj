; STM -> software transactional memory

(def current-track (ref "Mars, the Bringer of War"))
(deref current-track)
@current-track

(ref-set current-track "Venus, the Bringer of Peace")

; Works when wrapped in a transaction
(dosync (ref-set current-track "Venus, the Bringer of Peace"))

(def current-track (ref "Venus, the Bringer of Peace"))
(def current-composer (ref "Holst"))


(dosync
  (ref-set current-track "Credo")
  (ref-set current-composer "Byrd"))


(defrecord Message [sender text])

(user.Message. "aaron" "hello")

(def messages (ref ()))

(defn add-message [msg]
  (dosync (alter messages conj msg)))

(add-message (user.Message. "user 1" "hello"))
(add-message (user.Message. "user 2" "howdi"))

@messages


; the last in-transaction value you see from a commute will NOT always match the
; end-of-transaction value of a ref, because of reordering.
; You should use them only when you do not care about ordering.

(defn add-message-commute [msg]
  (dosync (commute messages conj msg)))
; Literally speaking, this is not true for a chat application. The list of
; messages most certainly has an order, so if two message adds get reversed, the
; resulting list will not correctly show the order in which the messages arrived.
; Practically speaking, chat message updates are commutative enough. STM-
; based reordering of messages will likely happen on time scales of microseconds
; or less. For users of a chat application, there are already reorderings on
; much larger time scales due to network and human latency. (Think about times
; that you have “spoken out of turn” in an online chat because another
; speaker’s message had not reached you yet.) Since these larger reorderings
; are unfixable, it is reasonable for a chat application to ignore the smaller
; reorderings that might bubble up from Clojure’s STM.

(def counter (ref 0))

(defn next-counter [] (dosync (alter counter inc)))

; ------------------ adding validations to refs
(def validate-message-list
  (partial every? #(and (:sender %) (:text %))))

(def messages (ref () :validator validate-message-list))

(add-message "not a valid message")
; Execution error (IllegalStateException) at user/add-message (REPL:2).
; Invalid reference state

(add-message (user.Message. "user a" "howdi"))
(add-message (user.Message. "user b" "gaudi"))
(def simbolo (add-message (user.Message. "user c" "guantanamera"))) ; copies the value at that moment
simbolo
; (#user.Message{:sender "user c", :text "guantanamera"} #user.Message{:sender "user b", :text "gaudi"} #user.Message{:sender "user a", :text "howdi"})

(add-message (user.Message. "user d" "marafaca"))
; (#user.Message{:sender "user d", :text "marafaca"} #user.Message{:sender "user c", :text "guantanamera"} #user.Message{:sender "user b", :text "gaudi"} #user.Message{:sender "user a", :text "howdi"})
simbolo
; (#user.Message{:sender "user c", :text "guantanamera"} #user.Message{:sender "user b", :text "gaudi"} #user.Message{:sender "user a", :text "howdi"})

; ------------------ atoms

(atom initial-state options?)
; options include:
; :validator validate-fn
; :meta metadata-map

(def current-track (atom "Venus, the Bringer of Peace") )
(def current-composer (atom "Creedence Clearwater Revival") )

@current-track
; "Venus, the Bringer of Peace"

(reset! current-track "I heard it through the grapevine")
(reset! current-composer "CCR")

; With atoms, you cannot more than one atom in a dosync IN A TRANSACTION
(dosync
  (reset! current-track "Ramble tamble") (reset! current-composer "CCR"))
; this works! But it's not part of a transaction


; Recommended way from the book is to tranform to a map, like this
(def current-track (atom {:title "Credo" :composer "Byrd"}))

(reset! current-track {:title "Spem in Alium" :composer "Tallis"})

; swap! updates an-atom by calling function f on the current value of an-atom, plus
; any additional args.
(swap! current-track assoc :title "Sancte Deus")

(defn titleToUpper [x]
  (assoc x :title (str/upper-case (:title x))))

; Creates a new map with value of the title key set to upper
(swap! current-track titleToUpper)


; ------------------ Use agents for Asynchronous Updates
(def counter (agent 0))

; NEVER send a blocking function, or you may unneces-
; sarily prevent other agents from making progress.
(send counter inc)
@counter

; These functions will cause the current thread to block until all actions sent
; from the current thread or agent have completed. await-for will return nil if the
; timeout expires and will return a non-nil value otherwise. await has no timeout,
; so be careful: await is willing to wait forever.
(await-for 1000 (send counter inc))

; WARNING: This waits forever
; (await (send counter inc))

(def counter (agent 0 :validator number?))
; #'user/counter
(send counter (fn [_] "boo"))
; #object[clojure.lang.Agent 0x1ca633c3 {:status :failed, :val 0}]


(agent-errors counter)
; (#error {
 ; :cause "Invalid reference state"
 ; :via
 ; [{:type java.lang.IllegalStateException
   ; :message "Invalid reference state"
   ; :at [clojure.lang.ARef validate "ARef.java" 33]}]
 ; :trace
 ; [[clojure.lang.ARef validate "ARef.java" 33]
  ; [clojure.lang.ARef validate "ARef.java" 46]
  ; [clojure.lang.Agent setState "Agent.java" 177]
  ; [clojure.lang.Agent$Action doRun "Agent.java" 115]
  ; [clojure.lang.Agent$Action run "Agent.java" 163]
  ; [java.util.concurrent.ThreadPoolExecutor runWorker "ThreadPoolExecutor.java" 1136]
  ; [java.util.concurrent.ThreadPoolExecutor$Worker run "ThreadPoolExecutor.java" 635]
  ; [java.lang.Thread run "Thread.java" 833]]})

(clear-agent-errors counter)


(def backup-agent (agent "./output/messages-backup.clj"))

; This uses
(defn add-message-with-backup [msg]
  (dosync
    (let [snapshot (commute messages conj msg)]
      (send-off backup-agent (fn [filename]
                                (spit filename (str snapshot "\n"))
                                filename))
    snapshot)))

(add-message-with-backup (user.Message. "Jane" "Message Two"))
(add-message-with-backup (user.Message. "Jane" "Message One"))

(add-message-with-backup (user.Message. "Adele" "Rolling in the deep"))


; ------------------ Managing Per-Thread State with Vars
(def ^:dynamic foo 10)

(.start (Thread. (fn [] (println foo))))
; nil
; 10

(binding [foo 42] foo)


(defn print-foo [] (println foo))

(binding [foo "bound foo"] (print-foo))
; bound foo
; nil

(let [foo "let foo"] (print-foo))
; 10
; nil

(defn ^:dynamic slow-double [n]
  (Thread/sleep 100)
  (* n 2))

(defn calls-slow-double []
  (map slow-double [1 2 1 2 1 2]))


(time (dorun (calls-slow-double )))
; "Elapsed time: 602.068528 msecs"
; nil

(defn demo-memoize []
  (time
    (dorun
      (binding [slow-double (memoize slow-double)]
        (calls-slow-double)))))

(demo-memoize)
; "Elapsed time: 200.444706 msecs"

; Just for my testing
; (defn demo-memoize []
;    (time
;     (dorun
;       (binding [slow-double println]
;         (calls-slow-double)))))

(demo-memoize)
; 1
; 2
; 1
; 2
; 1
; 2
; "Elapsed time: 5.612006 msecs"
; nil
