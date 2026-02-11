import { useEffect } from 'react';
import './App.css';
import { useGame } from './hooks/useGame';

function App() {
  const { gameData, loading, makingMove, error, startNewGame, makeMove } = useGame();

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
  const statusText =
    {
      PLAYING: 'Your turn',
      CROSS_WIN: 'You won! 🎉',
      ZERO_WIN: 'AI won!',
      DRAW: 'Draw!',
    }[gameData.status] || gameData.status;

  return (
    <div className="game-container">
      <h1 className="game-title">Tic-Tac-Toe</h1>

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
              onClick={() => makeMove(i, j)}
              style={{
                opacity: makingMove ? 0.6 : 1,
                cursor: makingMove ? 'not-allowed' : 'pointer',
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

export default App;
