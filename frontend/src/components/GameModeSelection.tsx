import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import type { GameData } from '../interfaces/game';

interface GameModeSelectionProps {
  onStartGame: (vsAi: boolean) => void;
  onJoinGame: (sessionId: string) => void;
}

export const GameModeSelection: React.FC<GameModeSelectionProps> = ({ onStartGame, onJoinGame }) => {
  const [activeGames, setActiveGames] = useState<GameData[]>([]);
  const [loading, setLoading] = useState(false);
  const { getAuthHeader, user } = useAuth();

  const fetchActiveGames = async () => {
    try {
      setLoading(true);
      const response = await fetch('/game/active', {
        headers: {
          ...getAuthHeader(),
        },
      });
      if (response.ok) {
        const gamesData = await response.json();
        // API возвращает Map<UUID, GameSessionDTO>, конвертируем в массив
        const gamesList = Object.values(gamesData) as GameData[];
        setActiveGames(gamesList);
      }
    } catch (error) {
      console.error('Error fetching active games:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // Fetch active games immediately
    fetchActiveGames();
    
    // Set up interval to refresh every 3 seconds (polling)
    const interval = setInterval(fetchActiveGames, 3000);
    
    // Cleanup interval on component unmount
    return () => clearInterval(interval);
  }, []);

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
    <div className="game-mode-selection">
      <h1 className="welcome-title">Choose Game Mode</h1>
      <div className="mode-buttons">
        <button
          className="mode-btn ai-mode-btn"
          onClick={() => onStartGame(true)}
        >
          Play vs AI
        </button>
        <button
          className="mode-btn player-mode-btn"
          onClick={() => onStartGame(false)}
        >
          Play vs Human
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
                  <span>Size: {game.gameMap.size}x{game.gameMap.size}</span>
                  <span>Players: {game.playerX && game.playerO ? '2/2' : game.playerX ? '1/2' : '0/2'}</span>
                  <span>Status: {game.status}</span>
                </div>
                <button
                  className="join-game-btn"
                  onClick={() => onJoinGame(game.id)}
                  disabled={!canJoinGame(game)}
                  style={{
                    opacity: canJoinGame(game) ? 1 : 0.5,
                    cursor: canJoinGame(game) ? 'pointer' : 'not-allowed'
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
  );
};