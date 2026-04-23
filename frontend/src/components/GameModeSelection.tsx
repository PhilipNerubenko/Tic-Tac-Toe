import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import type { GameData } from '../interfaces/game';
import { TopMasters } from './TopMasters';
import { GameHistory } from './GameHistory';
import { authorizedFetch } from '../utils/api';

interface GameModeSelectionProps {
  onStartGame: (vsAi: boolean) => void;
  onJoinGame: (sessionId: string) => void;
}

export const GameModeSelection: React.FC<GameModeSelectionProps> = ({
  onStartGame,
  onJoinGame,
}) => {
  const [activeGames, setActiveGames] = useState<GameData[]>([]);
  const [loading, setLoading] = useState(false);
  const [showTopMasters, setShowTopMasters] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const { user } = useAuth();
  const isFetchingRef = useRef(false);
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchActiveGames = useCallback(async () => {
    // Защита от параллельных запросов
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;

    // Отменяем предыдущий запрос, если он ещё выполняется
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    try {
      setLoading(true);
      const response = await authorizedFetch('/game/active', {
        signal: abortControllerRef.current.signal,
      });
      if (response.ok) {
        const gamesData = await response.json();
        // API возвращает Map<UUID, GameSessionDTO>, конвертируем в массив
        const gamesList = Object.values(gamesData) as GameData[];
        setActiveGames(gamesList);
      }
    } catch (error) {
      if (error instanceof Error && error.name !== 'AbortError') {
        console.error('Error fetching active games:', error);
      }
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, []);

  useEffect(() => {
    // Fetch active games immediately
    fetchActiveGames();

    // Set up timeout to refresh after 3 seconds (avoids overlapping requests)
    let timeoutId: ReturnType<typeof setTimeout>;
    const scheduleRefresh = () => {
      timeoutId = setTimeout(() => {
        fetchActiveGames().then(scheduleRefresh);
      }, 3000);
    };
    scheduleRefresh();

    // Cleanup on component unmount
    return () => {
      clearTimeout(timeoutId);
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchActiveGames]);

  const canJoinGame = (game: GameData) => {
    // Можно присоединиться если:
    // - игра в статусе WAITING_FOR_PLAYERS
    // - есть место (playerO отсутствует)
    // - текущий пользователь не является создателем (playerX)
    if (!user) return false;
    if (game.status !== 'WAITING_FOR_PLAYERS') return false;
    if (game.playerO) return false; // уже есть второй игрок
    if (game.playerX === user.userId) return false; // уже создатель
    return true;
  };

  return (
    <>
      <div className="game-mode-selection">
        <h1 className="welcome-title">Choose Game Mode</h1>
        <div className="mode-buttons">
          <button className="mode-btn ai-mode-btn" onClick={() => onStartGame(true)}>
            Play vs AI
          </button>
          <button className="mode-btn player-mode-btn" onClick={() => onStartGame(false)}>
            Play vs Human
          </button>
          <button className="mode-btn top-masters-btn" onClick={() => setShowTopMasters(true)}>
            Top Masters
          </button>
          <button className="mode-btn history-btn" onClick={() => setShowHistory(true)}>
            History
          </button>
        </div>

        {/* Active Games Section */}
        <div className="active-games-section">
          <h2>Join Active Games</h2>
          {loading ? (
            <p>Loading active games...</p>
          ) : activeGames.length > 0 ? (
            <ul className="active-games-list">
              {activeGames.map((game) => (
                <li key={game.id} className="active-game-item">
                  <div className="game-info">
                    <span>ID: {game.id.substring(0, 8)}...</span>
                    <span>
                      Size: {game.gameMap.size}x{game.gameMap.size}
                    </span>
                    <span>
                      Players: {game.playerX && game.playerO ? '2/2' : game.playerX ? '1/2' : '0/2'}
                    </span>
                    <span>Status: {game.status}</span>
                  </div>
                  <button
                    className="join-game-btn"
                    onClick={() => onJoinGame(game.id)}
                    disabled={!canJoinGame(game)}
                    style={{
                      opacity: canJoinGame(game) ? 1 : 0.5,
                      cursor: canJoinGame(game) ? 'pointer' : 'not-allowed',
                    }}
                  >
                    {game.playerX === user?.userId ? 'Your Game' : 'Join Game'}
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p>No active games available</p>
          )}
        </div>
      </div>
      {showTopMasters && <TopMasters onClose={() => setShowTopMasters(false)} />}
      {showHistory && <GameHistory onClose={() => setShowHistory(false)} />}
    </>
  );
};
