# mahjong-engine

A Clojure library implementing a simple Mahjong game-engine.

## Usage

### Require the library in the repl
```clojure
(require '[mahjong-engine.core :as m])
```

### Creating a new game
new-game returns a fn that takes a keyword as argument.
```clojure
(def game (m/new-game))
```

### Some GETTERS...
Return a collection of maps representing players of the game. Such a map contains :name (string) :hand (coll), :revealed (coll)
```clojure
((m/game :players))
```

Return the hand of the current player, should be empty at the start of a new game.
```clojure
((m/game :hand))
```

Return the wall of Mahjong-tiles. Tiles are represented as a keyword, e.g. :b1 represents 1 of bamboos, :dg represents green dragon, :wn represents northern wind.
```clojure
((m/game :wall))
```

Return a frequency-map of the discarded-tiles
```clojure
((m/game :discard-pile))
```

### Some SETTERS...
Update the game -> Draw 14 tiles to the hand of the current player
```clojure
((m/game :draw-tiles) 14)
```

Update the game -> Discard a tile (requires user input!)
```clojure
((m/game :discard-tile) (first ((m/game :hand))))
```

Update the game -> Rotate the players (next player's turn)
```clojure
((m/game :rotate-players))
```

Let's say current player has already 13 tiles in is hand
```clojure
((m/game :draw-tiles) 13)
```

Usually you'd draw a tile at the beginning of a turn
```clojure
((m/game :draw-tile))
```

Return current player's hand
```clojure
((m/game :hand))
```

Discard a tile
```clojure
((m/game :discard-tile) (first ((m/game :hand))))
```

### More to come!

## License

Copyright © 2017 Stan Verberkt

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
