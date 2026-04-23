import { useState, useCallback, useEffect, useRef } from 'react';

const checkOpponentAbortControllerRef = { current: null as AbortController | null };
import type { GameData } from '../interfaces/game';
import { useAuth } from '../contexts/AuthContext';
import { authorizedFetch } from '../utils/api';

const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // ms
const POLLING_INTERVAL = 2500; // ms - опрос каждые 2.5 секунды

interface UseGameReturn {
  gameData: GameData | null;
  loading: boolean;
  makingMove: boolean;
  error: string | null;
  startNewGame: (vsAi?: boolean) => Promise<void>;
  makeMove: (row: number, col: number) => Promise<void>;
  joinGame: (sessionId: string) => Promise<void>;
  checkOpponentLeft: () => Promise<void>; // проверка, покинул ли соперник игру
  isNotYourTurn: boolean; // флаг для отображения ошибки 403
  resetGame: () => void; // сброс состояния игры и остановка polling
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
  const [isNotYourTurn, setIsNotYourTurn] = useState(false);
   const { user } = useAuth();
  const pollingRef = useRef<number | null>(null);
  const currentGameIdRef = useRef<string | null>(null);

   // Функция для получения состояния игры
   const fetchGameState = useCallback(
     async (gameId: string): Promise<GameData | null> => {
       try {
         const response = await authorizedFetch(`/game/${gameId}`);

         if (response.status === 403) {
           setIsNotYourTurn(true);
           throw new Error('Not your turn!');
         }

         if (!response.ok) {
           throw new Error(`Server error: ${response.status}`);
         }

         const data = await response.json();
         if (isValidGameData(data)) {
           setIsNotYourTurn(false);
           return data;
         }
         return null;
       } catch (err) {
         if (err instanceof Error && err.message.includes('Not your turn')) {
           throw err;
         }
         console.error('Fetch game state error:', err);
         return null;
       }
     },
     []
   );

  // Polling для автоматического обновления
  useEffect(() => {
    // Stop polling if user is not authenticated
    if (!user) {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
      return;
    }

    const startPolling = () => {
      if (!gameData || !gameData.id) {
        if (pollingRef.current) {
          clearInterval(pollingRef.current);
          pollingRef.current = null;
        }
        return;
      }

      // Сохраняем ID текущей игры
      currentGameIdRef.current = gameData.id;

      // Немедленно обновляем состояние
      fetchGameState(gameData.id).then((updatedData) => {
            if (updatedData && updatedData.id === currentGameIdRef.current) {
              setGameData(updatedData);
            } else {
              return;
            }
      }).catch((err) => {
        console.error("Polling fetch error:", err);
      });

      // Запускаем интервал опроса
      pollingRef.current = setInterval(() => {
        if (currentGameIdRef.current) {
          fetchGameState(currentGameIdRef.current).then((updatedData) => {
            if (updatedData && updatedData.id === currentGameIdRef.current) {
              setGameData(updatedData);
              
              // Если сейчас ход соперника и игра не завершена, проверяем, не покинул ли он игру
              if (updatedData.status === 'PLAYER_TURN' && updatedData.currentPlayer !== user?.userId) {
                // Abort previous check if it's still running
                if (checkOpponentAbortControllerRef.current) {
                  checkOpponentAbortControllerRef.current.abort();
                }

                // Create a new AbortController for this check
                checkOpponentAbortControllerRef.current = new AbortController();
                const signal = checkOpponentAbortControllerRef.current.signal;

                 // Автоматически проверяем, покинул ли соперник игру
                 authorizedFetch(`/game/${currentGameIdRef.current}/check-opponent-left?timeoutSeconds=30`, {
                   method: 'POST',
                   signal,
                 })
                  .then((response) => {
                    if (response.ok) {
                      return response.json();
                    }
                    return null;
                  })
                  .then((data) => {
                    if (data && isValidGameData(data)) {
                      if (data.id !== currentGameIdRef.current) {
                        return;
                      }
                      setGameData(data as GameData);
                    }
                  })
                  .catch((err) => {
                    console.error('Auto check opponent left error:', err);
                  })
                  .finally(() => {
                    // Clear the AbortController after the request completes
                    checkOpponentAbortControllerRef.current = null;
                  });
              }
            }
          });
        }
      }, POLLING_INTERVAL);
    };

    // Останавливаем предыдущий polling
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }

    // Запускаем polling
    startPolling();

    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };
   }, [gameData?.id, fetchGameState, user]);

   // Fetch with retry logic
   const fetchWithRetry = useCallback(
     async (url: string, options?: RequestInit, retries = MAX_RETRIES): Promise<Response> => {
       try {
         const response = await authorizedFetch(url, options);
         if (!response.ok) {
           if (response.status === 403) {
             setIsNotYourTurn(true);
           }
           throw new Error(`Server error: ${response.status}`);
         }
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
     // Останавливаем polling предыдущей игры
     if (pollingRef.current) {
       clearInterval(pollingRef.current);
       pollingRef.current = null;
     }
     currentGameIdRef.current = null;

     setLoading(true);
     setError(null);
     setIsNotYourTurn(false);
     try {
       if (!user) {
         throw new Error('User not authenticated');
       }

       // Преобразуем userId в UUID формат
       const response = await fetchWithRetry(`/game?vsAi=${vsAi}`, {
         method: 'POST',
       });
       const data = await response.json();

       if (!isValidGameData(data)) {
         throw new Error('Invalid game data structure');
       }
       setGameData(data as GameData);
       currentGameIdRef.current = data.id;
     } catch (err) {
       const errorMsg = err instanceof Error ? err.message : 'Failed to connect to server';
       setError(errorMsg);
       console.error('Game start error:', err);
     } finally {
       setLoading(false);
     }
   }, [fetchWithRetry, user]);

   const makeMove = useCallback(
     async (row: number, col: number) => {
       if (
         !gameData ||
         !user ||
         gameData.gameMap.map[row][col] !== 0 ||
         gameData.status !== 'PLAYER_TURN' ||
         gameData.currentPlayer !== user.userId ||
         makingMove
       ) {
         return;
       }


       const mySymbol = gameData.playerX === user.userId ? 1 : 2;

       // Deep copy the game board
       const newMap = gameData.gameMap.map.map((r: number[]) => [...r]);
       newMap[row][col] = mySymbol;

       setMakingMove(true);
       setError(null);
       setIsNotYourTurn(false);
       try {
         const response = await fetchWithRetry(`/game/${gameData.id}/move`, {
           method: 'POST',
           body: JSON.stringify({
             gameMap: { ...gameData.gameMap, map: newMap },
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
     [gameData, user, makingMove, fetchWithRetry]
   );

   const joinGame = useCallback(async (sessionId: string) => {
     // Останавливаем polling предыдущей игры
     if (pollingRef.current) {
       clearInterval(pollingRef.current);
       pollingRef.current = null;
     }
     currentGameIdRef.current = null;

     setLoading(true);
     setError(null);
     setIsNotYourTurn(false);
     try {
       if (!user) {
         throw new Error('User not authenticated');
       }

       // Преобразуем userId в UUID формат
       const guestId = user.userId;
       const response = await fetchWithRetry(`/game/${sessionId}/join?guestId=${guestId}`, {
         method: 'POST',
       });
       const data = await response.json();

       if (!isValidGameData(data)) {
         throw new Error('Invalid game data structure');
       }
       setGameData(data as GameData);
       currentGameIdRef.current = data.id;
     } catch (err) {
       const errorMsg = err instanceof Error ? err.message : 'Failed to join game';
       setError(errorMsg);
       console.error('Join game error:', err);
     } finally {
       setLoading(false);
     }
   }, [fetchWithRetry, user]);

   const checkOpponentLeft = useCallback(async () => {
     if (!gameData || !gameData.id) {
       return;
     }

     try {
       const response = await authorizedFetch(`/game/${gameData.id}/check-opponent-left?timeoutSeconds=30`, {
         method: 'POST',
       });

       if (!response.ok) {
         throw new Error(`Server error: ${response.status}`);
       }

       const data = await response.json();
       if (isValidGameData(data)) {
         setGameData(data as GameData);
       }
     } catch (err) {
       console.error('Check opponent left error:', err);
     }
   }, [gameData]);

  const resetGame = useCallback(() => {
    // Останавливаем polling
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
    currentGameIdRef.current = null;
    setGameData(null);
    setLoading(false);
    setMakingMove(false);
    setError(null);
    setIsNotYourTurn(false);
  }, []);

  return {
    gameData,
    loading,
    makingMove,
    error,
    isNotYourTurn,
    startNewGame,
    makeMove,
    joinGame,
    checkOpponentLeft,
    resetGame,
  };
}
