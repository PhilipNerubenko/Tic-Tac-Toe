interface GameMap {
  map: number[][];
  size: number;
}

type GameStatus = 'WAITING_FOR_PLAYERS' | 'PLAYER_TURN' | 'VICTORY' | 'DRAW';

export interface GameData {
  id: string;
  gameMap: GameMap;
  status: GameStatus;
  currentPlayer?: string; // UUID текущего игрока
  winner?: string; // UUID победителя
}
