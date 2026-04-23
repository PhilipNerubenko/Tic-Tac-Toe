import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authorizedFetch } from '../utils/api';

interface LeaderboardEntry {
  userId: string;
  login: string;
  winRate: number;
}

interface TopMastersProps {
  onClose: () => void;
}

export const TopMasters: React.FC<TopMastersProps> = ({ onClose }) => {
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { user } = useAuth();

  const fetchLeaderboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await authorizedFetch('/game/leaderboard?n=10');

      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }

      const data = await response.json();
      setLeaderboard(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch leaderboard');
      console.error('Leaderboard fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchLeaderboard();
  }, [fetchLeaderboard]);

  return (
    <div className="top-masters-overlay" onClick={onClose}>
      <div className="top-masters-modal" onClick={(e) => e.stopPropagation()}>
        <div className="top-masters-header">
          <h2>Top Masters</h2>
          <button className="close-btn" onClick={onClose}>
            &times;
          </button>
        </div>

        {loading && <p className="loading-text">Loading leaderboard...</p>}

        {error && <p className="error-text">{error}</p>}

        {!loading && !error && (
          <table className="leaderboard-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Player</th>
                <th>Win Rate</th>
              </tr>
            </thead>
            <tbody>
              {leaderboard.map((entry, index) => (
                <tr
                  key={entry.userId}
                  className={entry.userId === user?.userId ? 'current-user-row' : ''}
                >
                  <td>{index + 1}</td>
                  <td>
                    {entry.login}
                    {entry.userId === user?.userId && ' (You)'}
                  </td>
                  <td>{entry.winRate.toFixed(1)}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {!loading && !error && leaderboard.length === 0 && (
          <p className="empty-text">No data available</p>
        )}
      </div>
    </div>
  );
};
