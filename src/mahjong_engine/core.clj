(ns mahjong-engine.core
 (:require [mahjong-engine.tiles :refer :all]
  [mahjong-engine.special-moves :refer :all]))

; TODO: Implement a scoring system.
; TODO: Implement special moves.
; TODO: Implement the execution of a round?

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
   :discard (conj discard-pile t)
   :last-discarded t ; keep the last discarded tile in a special place
   :players new-players)))

(defn check-special-moves
 [game]
 (do (println "Joehoe")
  (map (partial can-claim-tile? (:last-discarded game)) (:players game))))


; new-game [] => Returns a map with a closure around it that refs to the newly created game.
(defn new-game
           "Returns a fn with a closure around a map representing a game. The returned function accepts one arg (k) which should be a keyword. It returns the function matching with the keyword."
           []
           (let [g (atom {:wall (new-wall) :players (new-players) :discard nil :last-discarded nil})]
            (fn new-game [k]
             "Things you can do"
             (k {; GETTERS
                 :players (fn game-get-players [] (:players @g)),
                 :hand (fn game-get-hand [] (:hand (first (:players @g)))),
                 :wall (fn game-get-wall [] (:wall @g)),
                 :discard-pile (fn game-get-discard-pile [] (sort (frequencies (:discard @g)))), ; sort the discard-pile for better view
                 :special-moves (fn game-special-moves [] (check-special-moves @g))
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
