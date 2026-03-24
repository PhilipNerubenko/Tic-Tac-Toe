import { useState, useCallback } from 'react';
import type { GameData } from '../interfaces/game';
import { useAuth } from '../contexts/AuthContext';

const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // ms

interface UseGameReturn {
  gameData: GameData | null;
  loading: boolean;
  makingMove: boolean;
  error: string | null;
  startNewGame: (vsAi?: boolean) => Promise<void>;
  makeMove: (row: number, col: number) => Promise<void>;
}

const isValidGameData = (data: unknown): data is GameData => {
  return !!(
    data &&
    typeof data === 'object' &&
    'id' in data &&
    'gameMap' in data &&
    'status' in data
  );
};

export function useGame(): UseGameReturn {
  const [gameData, setGameData] = useState<GameData | null>(null);
  const [loading, setLoading] = useState(true);
  const [makingMove, setMakingMove] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { getAuthHeader, user } = useAuth();

  // Fetch with retry logic
  const fetchWithRetry = useCallback(
    async (url: string, options?: RequestInit, retries = MAX_RETRIES): Promise<Response> => {
      try {
        const response = await fetch(url, options);
        if (!response.ok) throw new Error(`Server error: ${response.status}`);
        return response;
      } catch (err) {
        if (retries > 0) {
          await new Promise((resolve) => setTimeout(resolve, RETRY_DELAY));
          return fetchWithRetry(url, options, retries - 1);
        }
        throw err;
      }
    },
    []
  );

  const startNewGame = useCallback(async (vsAi: boolean = true) => {
    setLoading(true);
    setError(null);
    try {
      // Проверяем, что пользователь авторизован
      if (!user) {
        throw new Error('User not authenticated');
      }
      
      const response = await fetchWithRetry(`/game?creatorId=${user.userId}&vsAi=${vsAi}`, {
        method: 'POST',
        headers: {
          ...getAuthHeader(),
        },
      });
      const data = await response.json();

      if (!isValidGameData(data)) {
        throw new Error('Invalid game data structure');
      }
      setGameData(data as GameData);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to connect to server';
      setError(errorMsg);
      console.error('Game start error:', err);
    } finally {
      setLoading(false);
    }
  }, [fetchWithRetry, getAuthHeader, user]);

  const makeMove = useCallback(
    async (row: number, col: number) => {
      if (
        !gameData ||
        gameData.gameMap.map[row][col] !== 0 ||
        gameData.status !== 'PLAYER_TURN' ||  // Убрали 'WAITING_FOR_PLAYERS', т.к. нельзя ходить, пока ждем игрока
        makingMove
      ) {
        return;
      }

      // Deep copy the game board
      const newMap = gameData.gameMap.map.map((r: number[]) => [...r]);
      newMap[row][col] = 1; // Player move (cross)

      setMakingMove(true);
      setError(null);
      try {
        const response = await fetchWithRetry(`/game/${gameData.id}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...getAuthHeader(),
          },
          body: JSON.stringify({
            ...gameData,
            gameMap: { ...gameData.gameMap, map: newMap },
            currentPlayer: user?.userId, // Передаем информацию о текущем игроке
          }),
        });
        const updated = await response.json();

        if (!isValidGameData(updated)) {
          throw new Error('Invalid response structure');
        }
        setGameData(updated as GameData);
      } catch (err) {
        const errorMsg = err instanceof Error ? err.message : 'Move failed';
        setError(errorMsg);
        console.error('Move error:', err);
      } finally {
        setMakingMove(false);
      }
    },
    [gameData, makingMove, fetchWithRetry, user]
  );

  return {
    gameData,
    loading,
    makingMove,
    error,
    startNewGame,
    makeMove,
  };
}
