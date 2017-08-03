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

(defn is-sequential-set?
 "Returns true if each element in coll matches the inc of the previous one. It DOES NOT sort coll. e.g. (is-sequential-set? '(1 2 3)) => true"
 [coll sequential-fn]
 (reduce
   (fn [last next]
     (when last
       (if (not= next (sequential-fn last))
         false
         next)))
   tiles))
