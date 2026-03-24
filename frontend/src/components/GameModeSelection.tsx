import React from 'react';

interface GameModeSelectionProps {
  onStartGame: (vsAi: boolean) => void;
}

export const GameModeSelection: React.FC<GameModeSelectionProps> = ({ onStartGame }) => {
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
    </div>
  );
};