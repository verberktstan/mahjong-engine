(ns mahjong-engine.special-moves
 (:require [mahjong-engine.tiles :refer :all]))

(defn is-matching-set?
 "Returns true if exactly n matching items are given. e.g. (is-matching-set? '(1 1 1 1 1) 5) => true"
 [tiles n]
 (if (= (count tiles) n)
  (apply = tiles)
  false))

(defn is-kong?
 "Returns true if exactly 4 equal items are given."
 [tiles]
 (is-matching-set? tiles 4))

(defn is-pung?
 "Returns true if exactly 3 equal items are given."
 [tiles]
 (is-matching-set? tiles 3))

(defn is-sequential?
 "Returns true if each element in coll matches the inc of the previous one. It DOES NOT sort coll. e.g. (is-sequential-set? '(1 2 3)) => true"
 [coll]
 (reduce
   (fn [prev next]
     (when (not (false? prev))
       (if (not= next (inc prev))
         false
         next)))
   coll))

(defn is-chow?
 "Returns logical true (value of the last tile in this case) if exactly 3 sequential suited (so no bonustiles!) tiles are given. Sorts tiles automatically so don't worry about the order."
 [tiles]
 (let [sorted-tiles (sort-tiles tiles)]
  (when (= (count sorted-tiles) 3) ; 3 tiles?
   (when (apply = (map :name sorted-tiles)) ; Do they have the same name?
    (when (number? (get (first sorted-tiles) :value)) ; Are they suited tiles, have a number as value?
          (is-sequential? (map :value sorted-tiles)))))))

; TODO: COMPLETE THIS FN!
; :revealed is a collection of combined tiles. Each combination is a map with a key for the type of combination (:chow, :pung or kong) paired with a symbol representing the score ('melded 'big-melded 'small-melded 'concealed), and a key :tiles paired with a collection of tiles.
; examples for revealed: {:kong 'concealed, :tiles '(..)} or {:pung 'melded, :tiles '(..)}
; 1. Select same-named tiles from the hand
; 2. Check for kong, pung and chow => Return collection of maps {:pung, 'melded}, {:chow, 'melded}, {:kong, 'big-melded},  (or nil)
;revealed (reduce concat (map :tiles (:revealed player))) ; Collect all tiles in revealed combinations. TODO: Implement with mapcat?
(defn can-claim-tile?
 "Returns a collection of keywords that reflects the combinations the player can make with the tile (ordered from high to low score-value)."
 [tile player]
 (let [equal-tiles-in-hand (filter (partial = tile) (:hand player))
       equal-named-tiles-in-hand (filter #(= (:name tile) (:name %)) (:hand player))
       three-equals (partition 3 1 equal-tiles-in-hand)
       two-equals (partition 2 1 equal-tiles-in-hand)
       two-equal-named (partition 2 1 equal-named-tiles-in-hand)
       kongs (map #(is-kong? (conj % tile)) three-equals)
       pungs (map #(is-pung? (conj % tile)) two-equals)
       chows (map #(is-chow? (conj % tile)) two-equal-named)]

  (cond
   (not (empty? kongs)) {:kong 'big-melded}
   (not (empty? pungs)) {:pung 'melded}
   (not (empty? chows)) {:chow 'melded}
   true nil)))
