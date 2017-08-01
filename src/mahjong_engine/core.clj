(ns mahjong-engine.core)

; TODO: Implement a scoring system & notion of chows, pungs and kongs.
; TODO: Implement generic interaction fnunctions (inform / prompt) for ui
; TODO: Implement claiming of a discarded tile.
; TODO: Implement the execution of a round

; tiles [] => Returns a collection of all base-tiles of the mahjong game. A tile is represented by a 2-char keyword, the first char describes the suit (b for bamboo, w for wind f.e.) and the second char describes the rank (1 for one, n for north f.e.)
(defn tiles
 "Returns a collection of all the base-tiles of the Mahjong game. Tiles are represented as keywords."
 []
 (reduce concat (map (fn [s c] (map keyword (map str (repeat s) c)))
                 ['b 'c 's 'd 'w]
                 [(range 1 10) (range 1 10) (range 1 10) ['g 'r 'w] ['e 'n 's 'w]])))

; new-wall [] => Returns a shuffled collection of tiles, representing a wall of Mahjong-tiles.
(defn new-wall
 "Returns a shuffled collection of tiles, representing a wall of Mahjong-tiles."
 [] (shuffle (reduce concat (repeat 4 (tiles)))))

; make-players [] => Returns a collection of maps representing players. It contains a name, a hand and revealed (sets).
(defn new-players
 "Retuns a collection of maps representing players."
 []
 (let [p {:name nil :hand nil :revealed nil}]
  (map
   (partial assoc p :name)
   (list "Aart" "Bert" "Ernie" "Pino"))))

; draw-tile [n g] => Returns map g with n tiles moved from the wall to the hand of the first-player.
(defn draw-tiles [n g]
 "Returns map g with n tiles moved from the wall to the hand of the first of the players."
 (let [[drawn-tiles c] (split-at n (:wall g)) ; Split wall
       player (first (:players g))
       hand (concat (:hand player) drawn-tiles)
       ; Let the new player be the old player with the drawn-tiles concatenated to its hand
       new-player (assoc player :hand (sort hand))]
  (assoc g
   :players (conj (rest (:players g)) new-player) ; Replace the first player
   :wall c)))

; rotate-players [g] => Returns map g with the first player moved to the back of the collection of players. Used to rotate the players at the end of a turn.
(defn rotate-players
 "Returns map g with the first player moved to the back of the collection of players."
 [g]
 (let [f (first (:players g))
       r (rest (:players g))]
  (assoc g
   :players (concat r (list f)))))

; discard-tile [t g] => Returns map g, moves tile t from the hand of the first player to the discard-pile.
(defn discard-tile [t g]
 "Returns map g with the first occurance of t in the hand of the first player moved to the discard-pile."
 (let [player (first (:players g))
       hand (:hand player)
       discard-pile (:discard g)
       ; split the hand at the first occurance of t.
       [hand-a hand-b] (split-with (partial not= t) hand)
       new-hand (concat hand-a (rest hand-b))
       new-players (conj
                    (rest (:players g))
                    (assoc player :hand new-hand))]
  (assoc g
   :discard (conj discard-pile t) ; is there a need to sort here?
   :players new-players)))

; new-game [] => Returns a map with a closure around it that refs to the newly created game.
(defn new-game
 "Returns a fn with a closure around a map representing a game. The returned function accepts one arg (k) which should be a keyword. It returns the function matching with the keyword."
 []
 (let [g (atom {:wall (new-wall) :players (new-players) :discard nil})]
  (fn [k]
   "Things you can do"
   (k {; GETTERS
       :players (fn [] (:players @g)),
       :hand (fn [] (:hand (first (:players @g)))),
       :wall (fn [] (:wall @g)),
       :discard-pile (fn [] (sort (frequencies (:discard @g)))), ; sort the discard-pile for better view
       ; SETTERS
       :draw-tile (fn [] (swap! g (partial draw-tiles 1))),
       :draw-tiles (fn [n] (swap! g (partial draw-tiles n))),
       :discard-tile (fn [t] (swap! g (partial discard-tile t)))
       :rotate-players (fn [] (swap! g (partial rotate-players)))}))))
