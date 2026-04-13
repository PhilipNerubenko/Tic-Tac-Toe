interface GameMap {
  map: number[][];
  size: number;
}

type GameStatus = 'WAITING_FOR_PLAYERS' | 'PLAYER_TURN' | 'VICTORY' | 'DRAW' | 'OPPONENT_LEFT';

export interface GameData {
  id: string;
  gameMap: GameMap;
  status: GameStatus;
  playerX?: string; // UUID первого игрока
  playerO?: string; // UUID второго игрока
  currentPlayer?: string; // UUID текущего игрока (управляется сервером)
  winner?: string; // UUID победителя
  lastActiveAt?: string; // Время последнего действия игрока
}
