# Tic-Tac-Toe Frontend

A modern React + TypeScript + Vite frontend for the Tic-Tac-Toe game with AI opponent.

## 🚀 Features

- **Dynamic Board Size**: Supports game boards of any size (not limited to 3x3)
- **Real-time Game Updates**: Instant feedback on moves with loading states
- **Error Handling**: Robust error handling with automatic retry logic
- **Responsive Design**: Beautiful dark-themed UI with smooth animations
- **Type Safety**: Full TypeScript support for zero runtime errors
- **Custom Hooks**: Game logic encapsulated in `useGame` hook for reusability

## 📋 Getting Started

### Prerequisites

- Node.js 18+
- Java backend running on `http://localhost:8080`

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Vite will proxy all `/game` requests to your Java backend automatically.

## 📦 Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Run ESLint to check code quality
npm run lint

# Format code with Prettier
npm run format

# Check formatting without modifying files
npm run format:check

# Preview production build locally
npm run preview
```

## 🏗️ Project Structure

```
src/
├── hooks/              # Custom React hooks
│   └── useGame.ts      # Game logic hook with retry and validation
├── interfaces/         # TypeScript interfaces
│   └── game.ts         # Game data types and status
├── App.tsx             # Main app component
├── App.css             # App styling
├── main.tsx            # Entry point
└── index.css           # Global styles
```

## 🎮 How It Works

### Game Flow

1. **Initialize**: On app mount, `useGame` hook connects to the backend and starts a new game
2. **Player Move**: Click on an empty cell to make your move (X)
3. **AI Response**: Backend processes the move and returns the AI's move (O)
4. **Win/Draw**: Game ends when there's a winner or the board is full

### Features in Detail

- **Retry Logic**: Failed requests automatically retry up to 3 times with 1-second delays
- **Data Validation**: All responses from the server are validated before use
- **Loading States**: Board is disabled when a move is being processed to prevent duplicate submissions
- **Error Display**: Connection errors are shown to the user with helpful messages

## 🔧 Configuration

### Vite Proxy

The `vite.config.ts` proxies `/game` requests to the Java backend:

```typescript
server: {
  proxy: {
    '/game': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

### Backend API Endpoints

- `POST /game` - Start a new game
- `POST /game/:id` - Submit a move for game with given ID

Expected response format:

```json
{
  "id": "uuid",
  "gameMap": {
    "map": [[0, 1, 2], ...],
    "size": 3
  },
  "status": "PLAYING|CROSS_WIN|ZERO_WIN|DRAW"
}
```

## 🎨 Customization

### Colors

Edit the CSS variables in `src/App.css`:

```css
:root {
  --bg-color: #1a1a1a;
  --panel-color: #2a2a2a;
  --x-color: #ff4d4d; /* Player (X) color */
  --o-color: #4da6ff; /* AI (O) color */
  --accent-color: #646cff; /* Button color */
}
```

### Board Size

The frontend automatically supports any board size returned by the backend through `gameMap.size`.

## 🐛 Troubleshooting

### "Connection Error" message

- Ensure Java backend is running on `http://localhost:8080`
- Check that `/game` endpoints are accessible
- See browser DevTools Console for detailed error messages

### Board not loading

- Check if backend is returning valid game data structure
- Verify the proxy settings in `vite.config.ts`

## 📝 Code Quality

This project uses:

- **ESLint**: Code quality
- **TypeScript**: Type safety
- **Prettier**: Code formatting

```bash
# Check quality
npm run lint

# Auto-fix issues
npm run format
```
