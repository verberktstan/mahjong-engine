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
 [& tiles]
 (is-matching-set? tiles 4))

(defn is-pung?
 "Returns true if exactly 3 equal items are given."
 [& tiles]
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
 [& tiles]
 (let [sorted-tiles (sort-tiles tiles)]
  (when (= (count sorted-tiles) 3) ; 3 tiles?
   (when (apply = (map :name sorted-tiles)) ; Do they have the same name?
    (when (number? (get (first sorted-tiles) :value)) ; Are they suited tiles, have a number as value?
          (is-sequential? (map :value sorted-tiles)))))))
