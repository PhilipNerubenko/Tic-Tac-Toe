interface GameMap {
  map: number[][];
  size: number;
}

type GameStatus = 'WAITING_FOR_PLAYERS' | 'PLAYER_TURN' | 'VICTORY' | 'DRAW' | 'OPPONENT_LEFT';

export interface GameData {
  id: string;
  gameMap: GameMap;
  status: GameStatus;
  playerX?: string;
  playerO?: string;
  currentPlayer?: string;
  winner?: string;
  lastActiveAt?: string;
  createdAt?: string;
}
