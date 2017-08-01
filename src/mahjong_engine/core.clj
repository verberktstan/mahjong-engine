(ns mahjong-engine.core)

; TODO: Update comments & docstrings
; TODO: Update todo-list ;-)

(defn tiles []
 (reduce concat (map (fn [s c] (map keyword (map str (repeat s) c)))
                 ['b 'c 's 'd 'w]
                 [(range 1 10) (range 1 10) (range 1 10) ['g 'r 'w] ['e 'n 's 'w]])))

(defn new-wall [] (shuffle (reduce concat (repeat 4 (tiles)))))


; make-players [] => Returns a collection of maps representing players. It contains a name, a hand and revealed (sets).
(defn new-players
 []
 (let [p {:name nil :hand nil :revealed nil}]
  (map
   (partial assoc p :name)
   (list "Bert" "Ernie" "Corinne" "Stan"))))

; draw-tile [n g] => Returns map g with n tiles moved from the wall to the hand of the first-player.
(defn draw-tiles [n g]
 (let [[drawn-tiles c] (split-at n (:wall g)) ; Split wall
       player (first (:players g))
       hand (concat (:hand player) drawn-tiles)
; Let the new player be the old player with the drawn-tiles concatenated to its hand
       new-player (assoc player :hand (sort hand))]
  (assoc g
   :players (conj (rest (:players g)) new-player) :wall c)))

(defn rotate-players
 [g]
 (let [f (first (:players g))
       r (rest (:players g))]
  (assoc g
   :players (concat r (list f)))))

; discard-tile [t g] => Returns map g, moves tile t from the hand of the first player to the discard-pile.
(defn discard-tile [t g]
 (let [player (first (:players g))
       hand (:hand player)
       discard-pile (:discard g)
       [hand-a hand-b] (split-with ; split the list at the first occurance of t
                        (partial not= t) hand)
       new-hand (concat hand-a (rest hand-b))
       new-players (conj
                    (rest (:players g))
                    (assoc player :hand new-hand))]
  (assoc g
   :discard (conj discard-pile t)
   :players new-players)))


; new-game [] => Returns a map with a closure around it that refs to the newly created game.
(defn new-game
 []
 (let [g (atom {:wall (new-wall) :players (new-players) :discard nil})]
  (fn [k]
   "Things you can do"
   (k {; GETTERS
       :players (fn [] (:players @g)),
       :hand (fn [] (:hand (first (:players @g)))),
       :wall (fn [] (:wall @g)),
       :discard-pile (fn [] (frequencies (:discard @g))),
       ; SETTERS
       :draw-tile (fn [] (swap! g (partial draw-tiles 1))),
       :draw-tiles (fn [n] (swap! g (partial draw-tiles n))),
       :discard-tile (fn [t] (swap! g (partial discard-tile t)))
       :rotate-players (fn [] (swap! g (partial rotate-players)))}))))
