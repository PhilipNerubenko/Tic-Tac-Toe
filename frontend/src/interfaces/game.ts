interface GameMap {
  map: number[][];
  size: number;
}

type GameStatus = 'PLAYING' | 'CROSS_WIN' | 'ZERO_WIN' | 'DRAW';

export interface GameData {
  id: string;
  gameMap: GameMap;
  status: GameStatus;
}
