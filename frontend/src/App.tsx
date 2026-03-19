import { useEffect, useState } from 'react';
import './App.css';
import { useGame } from './hooks/useGame';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { LoginForm } from './components/LoginForm';
import { RegisterForm } from './components/RegisterForm';

function Game() {
  const { gameData, loading, makingMove, error, startNewGame, makeMove } = useGame();
  const { logout } = useAuth();

  useEffect(() => {
    startNewGame();
  }, [startNewGame]);

  if (loading && !gameData) {
    return (
      <div className="welcome-container">
        <h1>Tic-Tac-Toe</h1>
        <p>Connecting to server...</p>
      </div>
    );
  }

  if (!gameData) {
    return (
      <div className="welcome-container">
        <h1>Connection Error</h1>
        <p>{error || 'Make sure Java backend is running'}</p>
        <button onClick={startNewGame} className="btn start-btn">
          Try Again
        </button>
      </div>
    );
  }

  const boardSize = gameData.gameMap.size;
  const { user } = useAuth();
  
  const statusText =
    {
      WAITING_FOR_PLAYERS: 'Waiting for players...',
      PLAYER_TURN: 'Your turn',
      VICTORY: gameData.winner ? (gameData.winner === user?.userId ? 'You won! 🎉' : 'AI won!') : 'Victory!',
      DRAW: 'Draw!',
    }[gameData.status] || gameData.status;

  return (
    <div className="game-container">
      <div className="game-header">
        <h1 className="game-title">Tic-Tac-Toe</h1>
        <button onClick={logout} className="btn logout-btn">
          Logout
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div
        className={`board ${makingMove ? 'disabled' : ''}`}
        style={{
          gridTemplateColumns: `repeat(${boardSize}, var(--cell-size))`,
        }}
      >
        {gameData.gameMap.map.map((row: number[], i: number) =>
          row.map((cell: number, j: number) => (
            <div
              key={`${i}-${j}`}
              className={`cell ${cell === 1 ? 'x-player' : cell === 2 ? 'o-player' : ''}`}
              onClick={() => {
                // Проверяем, может ли пользователь сделать ход
                const canMakeMove =
                  gameData.status === 'PLAYER_TURN' &&
                  !makingMove &&
                  cell === 0;
                
                if (canMakeMove) {
                  makeMove(i, j);
                }
              }}
              style={{
                opacity: makingMove ? 0.6 : 1,
                cursor:
                  gameData.status === 'PLAYER_TURN' &&
                  !makingMove &&
                  cell === 0
                    ? 'pointer'
                    : 'not-allowed',
              }}
            >
              {cell === 1 ? 'X' : cell === 2 ? 'O' : ''}
            </div>
          ))
        )}
      </div>

      <div className="status-panel">
        <div className="status-badge">{statusText}</div>
        <button onClick={startNewGame} className="btn" disabled={loading}>
          {loading ? 'Loading...' : 'New Game'}
        </button>
      </div>
    </div>
  );
}

function App() {
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    if (authMode === 'login') {
      return <LoginForm onSwitchToRegister={() => setAuthMode('register')} />;
    } else {
      return <RegisterForm onSwitchToLogin={() => setAuthMode('login')} />;
    }
  }

  return <Game />;
}

function AppWrapper() {
  return (
    <AuthProvider>
      <App />
    </AuthProvider>
  );
}

export default AppWrapper;
