(ns mahjong-engine.core
 (:require [clojure.string :as s]))

; TODO: Split implementation of tiles to suits (bamboos, dots & signs) and bonus (winds, dragins, seasons, flowers). Each tile has a name and a value.
; Functions concerned: make-get-suit-fn, make-get-rank-fn, make-same-suit-fn, tiles and the tile-definitions map
; TODO: Implement a fn that returns all the close tiles in hand (same suit, within chow range for standard suits)
; TODO: Implement a scoring system & notion of chows, pungs and kongs.
; TODO: Implement generic interaction functions (inform / prompt) for ui
; TODO: Implement claiming of a discarded tile => Reveal a set
; TODO: Implement the execution of a round
; TODO: Separate functions into different namespaces/files

; Sorting tiles by name first, and value second.
(def sort-tiles (partial sort-by (juxt :name :value)))

; tiles [] => Returns a collection of all base-tiles of the mahjong game. A tile is represented by a 2-char keyword, the first char describes the suit (b for bamboo, w for wind f.e.) and the second char describes the rank (1 for one, n for north f.e.)
(defn tiles
 "Returns a collection of tiles with name tile-name. (tile-name :bamboo) => list of all bamboos etc."
 [tile-name]
 (let [c (get {:dragon ["green" "red" "white"]
               :wind ["east" "north" "south" "west"]
               :season ["spring" "summer" "autumn" "fall"]
               :flower ["bamboo" "chrysantemum" "orchid" "plumb"]}
          (keyword tile-name)
          (range 1 10))]
  (map
   (partial assoc {:name (name tile-name)} :value)
   c)))

; new-wall [] => Returns a shuffled collection of tiles, representing a wall of Mahjong-tiles.
(defn new-wall
 "Returns a shuffled collection of tiles, representing a wall of Mahjong tiles."
 []
 (let [all-single-tiles (reduce concat (map tiles [:bamboo :character :dot :dragon :wind]))
       bonus-tiles (reduce concat (map tiles [:season :flower]))]
  (shuffle (concat
            (reduce concat (repeat 4 all-single-tiles)) ; Repeat suited tiles 4 times
            bonus-tiles)))) ; Bonus tiles not repeated

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
       new-player (assoc player :hand (sort-tiles hand))]
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
                 :discard-tile (fn game-discard-tile [t] (swap! g (partial discard-tile t))),
                 :rotate-players (fn game-rotate-players [] (swap! g (partial rotate-players)))}))))

(defn draw-hands
 [game]
 (loop [g game]
  (if (not (empty? ((g :hand))))
   g
   (recur (dosync
           ((g :draw-tiles) 13)
           ((g :rotate-players))
           g)))))

(defn main
 [game]
 (dosync
  (draw-hands game)
  game))
