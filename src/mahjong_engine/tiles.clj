(ns mahjong-engine.tiles)

; Sorting tiles by name first, and value second.
(def sort-tiles (partial sort-by (juxt :name :value)))

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

(defn new-wall
 "Returns a shuffled collection of tiles, representing a wall of Mahjong tiles."
 []
 (let [all-single-tiles (reduce concat (map tiles [:bamboo :character :dot :dragon :wind]))
       bonus-tiles (reduce concat (map tiles [:season :flower]))]
  (shuffle (concat
            (reduce concat (repeat 4 all-single-tiles)) ; Repeat suited tiles 4 times
            bonus-tiles)))) ; Bonus tiles not repeated
