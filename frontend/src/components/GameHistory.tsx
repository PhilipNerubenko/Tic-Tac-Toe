import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authorizedFetch } from '../utils/api';

interface GameHistoryEntry {
  id: string;
  status: string;
  playerX?: string;
  playerO?: string;
  winner?: string;
  createdAt?: string;
  gameMap: {
    size: number;
  };
}

interface GameHistoryProps {
  onClose: () => void;
}

export const GameHistory: React.FC<GameHistoryProps> = ({ onClose }) => {
  const [history, setHistory] = useState<GameHistoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { user } = useAuth();

  const fetchHistory = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await authorizedFetch('/game/history');

      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }

      const data = await response.json();
      setHistory(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch game history');
      console.error('Game history fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  const getResultText = (game: GameHistoryEntry) => {
    if (!game.winner) return 'Draw';
    if (game.winner === user?.userId) return 'Win';
    return 'Loss';
  };

  const getResultClass = (game: GameHistoryEntry) => {
    if (!game.winner) return 'result-draw';
    if (game.winner === user?.userId) return 'result-win';
    return 'result-loss';
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'Unknown';
    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return 'Unknown';
      return (
        date.toLocaleDateString() +
        ' ' +
        date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      );
    } catch {
      return 'Unknown';
    }
  };

  return (
    <div className="top-masters-overlay" onClick={onClose}>
      <div className="top-masters-modal game-history-modal" onClick={(e) => e.stopPropagation()}>
        <div className="top-masters-header">
          <h2>Game History</h2>
          <button className="close-btn" onClick={onClose}>
            &times;
          </button>
        </div>

        {loading && <p className="loading-text">Loading game history...</p>}

        {error && <p className="error-text">{error}</p>}

        {!loading && !error && (
          <div className="history-list">
            {history.map((game) => (
              <div key={game.id} className="history-item">
                <div className="history-item-info">
                  <span className="history-date">{formatDate(game.createdAt)}</span>
                  <span className="history-size">
                    {game.gameMap.size}x{game.gameMap.size}
                  </span>
                  <span className={`history-result ${getResultClass(game)}`}>
                    {getResultText(game)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {!loading && !error && history.length === 0 && (
          <p className="empty-text">No games played yet</p>
        )}
      </div>
    </div>
  );
};
