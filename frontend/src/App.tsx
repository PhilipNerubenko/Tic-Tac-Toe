import { useState, useEffect } from 'react';
import './App.css';
import { useGame } from './hooks/useGame';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { LoginForm } from './components/LoginForm';
import { RegisterForm } from './components/RegisterForm';
import { GameModeSelection } from './components/GameModeSelection';
import { UserProfile } from './components/UserProfile';

function Game({
  gameApi,
  onBackToMenu,
  onPlayAgain,
  onShowProfile,
  onRetry,
}: {
  gameApi: ReturnType<typeof useGame>;
  onBackToMenu: () => void;
  onPlayAgain: () => void;
  onShowProfile: () => void;
  onRetry: () => void;
}) {
  const { gameData, loading, makingMove, error, isNotYourTurn, makeMove, checkOpponentLeft } =
    gameApi;
  const { logout, user } = useAuth();

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
        <button onClick={() => onRetry()} className="btn start-btn">
          Try Again
        </button>
      </div>
    );
  }

  const boardSize = gameData.gameMap.size;

  // Проверяем, является ли текущий пользователь тем, кто должен ходить
  const isMyTurn = gameData.currentPlayer === user?.userId;

  const statusText =
    {
      WAITING_FOR_PLAYERS: 'Waiting for players...',
      PLAYER_TURN: isMyTurn ? 'Your turn' : "Opponent's turn",
      VICTORY: gameData.winner
        ? gameData.winner === user?.userId
          ? 'You won! 🎉'
          : 'Opponent won!'
        : 'Victory!',
      DRAW: 'Draw!',
      OPPONENT_LEFT: 'Opponent left the game! You win! 🎉',
    }[gameData.status] || gameData.status;

  return (
    <div className="game-container">
      <div className="game-header">
        <h1 className="game-title">Tic-Tac-Toe</h1>
        <div className="header-buttons">
          <button onClick={onShowProfile} className="btn profile-btn">
            Profile
          </button>
          <button onClick={logout} className="btn logout-btn">
            Logout
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}
      {isNotYourTurn && !error && (
        <div className="error-message" style={{ backgroundColor: '#ffcc00', color: '#333' }}>
          Сейчас не ваш ход!
        </div>
      )}

      <div
        className={`board ${makingMove ? 'disabled' : ''}`}
        style={{
          gridTemplateColumns: `repeat(${boardSize}, var(--cell-size))`,
        }}
      >
        {gameData.gameMap.map.map((row: number[], i: number) =>
          row.map((cell: number, j: number) => (
            <button
              key={`${i}-${j}`}
              className={`cell ${cell === 1 ? 'x-player' : cell === 2 ? 'o-player' : ''}`}
              onClick={() => {
                if (gameData.status === 'PLAYER_TURN' && isMyTurn && !makingMove && cell === 0) {
                  makeMove(i, j);
                }
              }}
              disabled={
                !(gameData.status === 'PLAYER_TURN' && isMyTurn && !makingMove && cell === 0) ||
                makingMove
              }
              aria-disabled={
                !(gameData.status === 'PLAYER_TURN' && isMyTurn && !makingMove && cell === 0) ||
                makingMove
              }
              aria-label={`Cell ${i}, ${j}. ${cell === 1 ? 'X' : cell === 2 ? 'O' : 'Empty'}`}
              style={{
                opacity: makingMove ? 0.6 : 1,
                cursor:
                  gameData.status === 'PLAYER_TURN' && isMyTurn && !makingMove && cell === 0
                    ? 'pointer'
                    : 'not-allowed',
              }}
            >
              {cell === 1 ? 'X' : cell === 2 ? 'O' : ''}
            </button>
          ))
        )}
      </div>

      <div className="status-panel">
        <div className="status-badge">{statusText}</div>
        {(gameData.status === 'VICTORY' ||
          gameData.status === 'DRAW' ||
          gameData.status === 'OPPONENT_LEFT') && (
          <button onClick={onPlayAgain} className="btn play-again-btn">
            Play Again
          </button>
        )}
        {gameData.status === 'PLAYER_TURN' && !isMyTurn && (
          <button onClick={checkOpponentLeft} className="btn check-opponent-btn">
            Check if opponent left
          </button>
        )}
        <button onClick={onBackToMenu} className="btn back-btn">
          Back to Menu
        </button>
      </div>
    </div>
  );
}

function App() {
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  const { isAuthenticated, logout } = useAuth();
  const [gameStarted, setGameStarted] = useState(false);
  const [vsAi, setVsAi] = useState<boolean>(true);
  const [showProfile, setShowProfile] = useState(false);

  const game = useGame();
  const { resetGame } = game;

  // Сбрасываем игру при выходе из системы
  useEffect(() => {
    if (!isAuthenticated) {
      resetGame();
      setTimeout(() => setShowProfile(false), 0);
    }
  }, [isAuthenticated, resetGame]);

  if (!isAuthenticated) {
    if (authMode === 'login') {
      return <LoginForm onSwitchToRegister={() => setAuthMode('register')} />;
    } else {
      return <RegisterForm onSwitchToLogin={() => setAuthMode('login')} />;
    }
  }

  if (!gameStarted) {
    return (
      <div className="game-mode-selection-screen">
        <div className="game-header">
          <h1 className="game-title">Tic-Tac-Toe</h1>
          <button onClick={logout} className="btn logout-btn">
            Logout
          </button>
        </div>
        <GameModeSelection
          onStartGame={(vsAiMode) => {
            setVsAi(vsAiMode);
            game.startNewGame(vsAiMode);
            setGameStarted(true);
          }}
          onJoinGame={(sessionId) => {
            // Join the existing game (always human vs human)
            setVsAi(false);
            game.joinGame(sessionId);
            setGameStarted(true);
          }}
        />
      </div>
    );
  }

  return (
    <>
      <Game
        gameApi={game}
        onBackToMenu={() => {
          game.resetGame();
          setGameStarted(false);
          setShowProfile(false);
        }}
        onPlayAgain={() => {
          game.startNewGame(vsAi);
        }}
        onShowProfile={() => setShowProfile(true)}
        onRetry={() => game.startNewGame(vsAi)}
      />
      {showProfile && <UserProfile onClose={() => setShowProfile(false)} />}
    </>
  );
}

function AppWrapper() {
  return (
    <AuthProvider>
      <App />
    </AuthProvider>
  );
}

export default AppWrapper;
