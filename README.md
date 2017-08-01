# mahjong-engine

A Clojure library implementing a simple Mahjong game-engine.

## Usage

### Creating a new game
new-game returns a fn that takes a keyword as argument.
```clojure
(def game (new-game))
```

### Some GETTERS...
Return the players of the game
```clojure
((game :players))
```

#### Return the hand of the current player
```clojure
((game :hand))
```

#### Return the wall of Mahjong-tiles
```clojure
((game :wall))
```

#### Return a frequency-map of the discarded-tiles
```clojure
((game :discard-pile))
```

### Some SETTERS...
Update the game -> Draw 14 tiles to the hand of the current player
```clojure
((game :draw-tiles) 14)
```

#### Update the game -> Discard a tile (requires user input!)
```clojure
((game :discard-tile) (first ((game :hand))))
```

#### Update the game -> Rotate the players (next player's turn)
```clojure
((game :rotate-players))
```

#### Let's say current player has already 13 tiles in is hand
```clojure
((game :draw-tiles) 13)
```

#### Usually you'd draw a tile at the beginning of a turn
```clojure
((game :draw-tile))
```

#### Inspect your hand
```clojure
((game :hand))
```

#### Discard a tile
```clojure
((game :discard-tile) (first ((game :hand))))
```

### More to come!

## License

Copyright © 2017 Stan Verberkt

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
