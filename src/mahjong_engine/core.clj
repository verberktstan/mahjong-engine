(ns mahjong-engine.core)

; TODO: Implement a fn that returns all the close tiles in hand (same suit, within chow range for standard suits)
; TODO: Implement a scoring system & notion of chows, pungs and kongs.
; TODO: Implement generic interaction functions (inform / prompt) for ui
; TODO: Implement claiming of a discarded tile => Reveal a set
; TODO: Implement the execution of a round
; TODO: Separate functions into different namespaces/files

; Definition of available tiles. Can be used as argument to make-get-suit-fn and make-get-rank-fn. It should be possible (in the future) to supply different tile definition sets for a new game.
(def tile-definitions
 {:b {:suit "bamboo"
      :ranks (apply hash-map
              (apply interleave (repeat 2 (range 1 10))))}
  :c {:suit "circle"
      :ranks (apply hash-map
              (apply interleave (repeat 2 (range 1 10))))}
  :d {:suit "dragon"
      :ranks (apply hash-map
              (interleave [\g \r \w] ["green" "red" "white"]))}
  :s {:suit "sign"
      :ranks (apply hash-map
              (apply interleave (repeat 2 (range 1 10))))}
  :w {:suit "wind"
      :ranks (apply hash-map
              (interleave [\e \n \s \w] ["east" "north" "south" "west"]))}})

(defn char-is-number?
 "Returns true if the char is a number."
 [c]
 (let [i (int c)]
  (and (>= i 48) (< i 58))))

; make-get-suit-fn [tile-def-map] => Given a tile definition map, returns a fn that retrieves the suit of tile.
(defn make-get-suit-fn
 "Given a tile definition map, returns a fn that retrieves the suit of tile."
 [tile-def-map]
 (fn get-suit [tile]
  (:suit
   (get tile-def-map
    (keyword (str (first (name tile))))
    nil))))

; make-get-rank-fn [tile-def-map] => Given a tile definition map, returns a fn that retrieves the rank of tile.
(defn make-get-rank-fn
 "Given a tile definition map, returns a fn that retrieves the rank of tile."
 [tile-def-map]
 (fn get-rank [tile]
  (let [rank-coll (:ranks (get tile-def-map (keyword (str (first (name tile)))) nil))
        c (second (name tile))
        rank (if (char-is-number? c) (- (int c) 48) c)]
   (get rank-coll rank nil))))

; make-interactions [inform-fn request-fn] => Returns a function that accepts a keyword. Abstraction of user interaction.
(defn make-interactions [inform-fn request-fn]
 (fn make-interactions [k]
  (let [functionality {:inform (fn [x] (inform-fn x))
                       :request (fn [x] (let [input (do
                                                     (inform-fn x)
                                                     (request-fn))]
                                         input))}]
   ((keyword k) functionality))))
; Create a function to interact with the user.
;(def interact (make-interactions println read-line)) ; Call it when creating a new game?

; tiles [] => Returns a collection of all base-tiles of the mahjong game. A tile is represented by a 2-char keyword, the first char describes the suit (b for bamboo, w for wind f.e.) and the second char describes the rank (1 for one, n for north f.e.)
(defn tiles
 "Returns a collection of all the base-tiles of the Mahjong game. Tiles are represented as keywords."
 []
 (reduce concat (map (fn compose-tile [s c] (map keyword (map str (repeat s) c)))
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
  (fn new-game [k]
   "Things you can do"
   (k {; GETTERS
       :players (fn game-get-players [] (:players @g)),
       :hand (fn game-get-hand [] (:hand (first (:players @g)))),
       :wall (fn game-get-wall [] (:wall @g)),
       :discard-pile (fn game-get-discard-pile [] (sort (frequencies (:discard @g)))), ; sort the discard-pile for better view
       ; SETTERS
       :draw-tile (fn game-draw-tile [] (swap! g (partial draw-tiles 1))),
       :draw-tiles (fn game-draw-tiles [n] (swap! g (partial draw-tiles n))),
       :discard-tile (fn game-discard-tile [t] (swap! g (partial discard-tile t)))
       :rotate-players (fn game-rotate-players [] (swap! g (partial rotate-players)))}))))
