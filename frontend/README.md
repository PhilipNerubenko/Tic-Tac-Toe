# Tic-Tac-Toe Frontend

A modern React + TypeScript + Vite frontend for the Tic-Tac-Toe game with JWT-based authentication, user profiles, game history, leaderboards, and an AI opponent.

## 🚀 Features

-   **JWT Authentication** — Secure login/registration with Bearer tokens and automatic refresh
-   **User Profiles** — Player statistics and account information
-   **Dynamic Board Size** — Supports game boards of any size (not limited to 3x3)
-   **Real-time Game Updates** — Instant feedback on moves with polling and loading states
-   **Error Handling** — Robust error handling with automatic retry logic (3 retries, 1s delay)
-   **Responsive Design** — Beautiful dark-themed UI with smooth animations
-   **Type Safety** — Full TypeScript support for zero runtime errors
-   **Custom Hooks** — Game logic encapsulated in `useGame` hook for reusability
-   **PvP Multiplayer** — Join active games and play against other users
-   **Leaderboard & History** — View top players and your game history

## 📋 Getting Started

### Prerequisites

-   Node.js 20+ and npm
-   Java backend running on `http://localhost:8081` (or via Docker Compose)

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Vite dev server runs on `http://localhost:5173` and proxies `/auth` and `/game` requests to the Java backend at `http://localhost:8081`.

## 📦 Available Scripts

```bash
# Start development server with hot reload
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
├── App.tsx             # Main app component with routing and game logic
├── App.css             # App styling and theme variables
├── main.tsx            # Entry point (React + StrictMode)
├── index.css           # Global styles
├── components/         # Reusable UI components
│   ├── LoginForm.tsx           # User login form (JWT signin)
│   ├── RegisterForm.tsx        # User registration form (JWT signup)
│   ├── GameModeSelection.tsx   # Game mode selection screen (vs AI / PvP)
│   └── UserProfile.tsx         # User profile and statistics display
├── contexts/           # React Context providers
│   └── AuthContext.tsx # Authentication state management (JWT tokens, login/logout)
├── hooks/              # Custom React hooks
│   └── useGame.ts      # Game logic hook with polling, retry, validation, auto-check opponent
├── interfaces/         # TypeScript interfaces
│   └── game.ts         # Game data types and status values
├── utils/              # API utilities
│   └── api.ts          # authorizedFetch with automatic JWT token refresh on 401
└── constants.ts        # Storage keys (ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, USER_DATA_KEY)
```

## 🎮 How It Works

### Authentication Flow (JWT)

1.  **Register**: New users create an account via `POST /auth/signup`. The backend returns `accessToken` and `refreshToken`, which are stored in `localStorage`. The frontend then fetches user profile via `/auth/me`.
2.  **Login**: Existing users authenticate via `POST /auth/signin` with username/password. Same token handling as registration.
3.  **Token Storage**: Tokens stored in `localStorage` under keys `ACCESS_TOKEN_KEY`, `REFRESH_TOKEN_KEY`, `USER_DATA_KEY`. Access token sent in `Authorization: Bearer <token>` header on every request via `authorizedFetch`.
4.  **Automatic Refresh**: When an API call returns 401, `authorizedFetch` automatically calls `POST /auth/refresh/access` with the refresh token to obtain new tokens and retries the original request.
5.  **Profile Access**: Authenticated users can view their profile (userId, login) from `/auth/me`.

### Game Flow

1.  **Initialize**: After login, `useGame` hook connects to the backend and starts a new game (`POST /game?size=3&vsAi=true`).
2.  **Player Move**: Click on an empty cell to make your move (X). The board is disabled during move processing.
3.  **AI Response**: Backend processes the move and returns the AI's move (O) automatically in the same response.
4.  **Win/Draw**: Game ends when there's a winner or the board is full. Status becomes `CROSS_WIN`, `ZERO_WIN`, or `DRAW`.
5.  **Polling**: The game state polls every 2.5 seconds to detect opponent moves in PvP games or game end.
6.  **Opponent Check**: During opponent's turn in PvP, automatic check for opponent abandonment (`/game/{id}/check-opponent-left`) every poll.

### Features in Detail

-   **Retry Logic**: Failed requests automatically retry up to 3 times with 1-second delays.
-   **Data Validation**: All responses from the server are validated before use (`isValidGameData` type guard).
-   **Loading States**: Board is disabled when a move is being processed to prevent duplicate submissions.
-   **Error Display**: Connection errors and 403 (not your turn) shown to user with helpful messages.
-   **Auth Context**: Centralized authentication state management across all components with token persistence.
-   **PvP Support**: Create game with `vsAi=false`, share session ID with friend, they join via `POST /game/{id}/join?guestId=<uuid>`.
-   **Statistics**: View your game history (`/game/history`) and global leaderboard (`/game/leaderboard?n=10`).

## 🔧 Configuration

### Vite Proxy

The `vite.config.ts` proxies API requests to the Java backend. This avoids CORS issues during development:

```typescript
server: {
  proxy: {
    '/game': {
      target: 'http://localhost:8081',
      changeOrigin: true,
    },
    '/auth': {
      target: 'http://localhost:8081',
      changeOrigin: true,
    },
  },
}
```

**Important:** The proxy target must match your backend port. Default is `8081`. Adjust if your backend runs on a different port.

### Backend API Endpoints

#### Authentication

-   `POST /auth/signup` — Register a new user (returns JWT tokens)
-   `POST /auth/signin` — Login with credentials (returns JWT tokens)
-   `POST /auth/refresh/access` — Refresh access token using refresh token
-   `POST /auth/refresh/refresh` — Rotate refresh token
-   `GET /auth/me` — Get current user profile (requires Bearer token)
-   `GET /auth/{id}` — Get user by ID (self or admin only)

#### User

-   `GET /auth/me` — Get current user profile (replaces old `/user/profile`)

#### Game

-   `POST /game?size=3&vsAi=true` — Start a new game (vs AI or PvP)
-   `POST /game/:id/move` — Submit a move for game with given ID (body: `{ "gameMap": { "map": [...], "size": 3 } }`)
-   `GET /game/:id` — Get game status
-   `GET /game/active` — List all available (waiting) games for PvP joining
-   `POST /game/:id/join?guestId=<uuid>` — Join existing game as second player
-   `POST /game/:id/check-opponent-left?timeoutSeconds=30` — Check if opponent abandoned the game
-   `GET /game/history` — Get all finished games for current user
-   `GET /game/leaderboard?n=10` — Get top N players by win rate

**Expected game response format:**

```json
{
  "id": "uuid",
  "gameMap": {
    "map": [[0, 1, 2], [0, 0, 0], [0, 0, 0]],
    "size": 3
  },
  "status": "PLAYING|CROSS_WIN|ZERO_WIN|DRAW|WAITING_FOR_PLAYERS|OPPONENT_LEFT",
  "playerX": "uuid",
  "playerO": "uuid|null",
  "currentPlayer": "uuid",
  "winner": "uuid|null",
  "lastActiveAt": "2025-01-15T10:30:00Z",
  "createdAt": "2025-01-15T10:25:00Z"
}
```

**Cell values:**
-   `0` = EMPTY
-   `1` = CROSS (player X)
-   `2` = ZERO (player O or AI)

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

The frontend automatically supports any board size returned by the backend through `gameMap.size`. The board grid is generated dynamically.

## 🐛 Troubleshooting

### "Connection Error" message

-   Ensure Java backend is running on `http://localhost:8081` (not 8080!)
-   Check that `/game` and `/auth` endpoints are accessible via browser or curl
-   Verify Vite dev server proxy configuration in `vite.config.ts` matches backend URL
-   See browser DevTools Console for detailed error messages and network tab for failed requests

### Authentication issues

-   Verify credentials are correct; check for duplicate registration attempts (409 conflict)
-   Ensure tokens are stored in `localStorage` with correct keys (`accessToken`, `refreshToken`)
-   Check that `authorizedFetch` includes `Authorization: Bearer <token>` header (inspect network tab)
-   If 401 persists after refresh, you may be logged out — login again
-   Clear `localStorage` and re-authenticate if token format seems corrupted

### Board not loading

-   Check if backend is returning valid game data structure with `id`, `gameMap`, `status` fields
-   Verify the proxy settings in `vite.config.ts` target the correct port (`8081`)
-   Ensure you are authenticated; unauthenticated requests to `/game/*` return 401
-   Check console for `Failed to fetch` or CORS errors (proxy should prevent CORS)

### 403 Forbidden (Not Your Turn)

This occurs when:
- You try to join a game that you didn't create or aren't authorized for
- You try to make a move when it's not your turn (status not `PLAYER_TURN` and `currentPlayer !== yourId`)
- You try to access another user's game (backend enforces ownership)

Wait for your turn or create/join the correct game.

### Port mismatches

Common issues:
- Backend runs on **8081** (external), **8080** (internal Docker)
- Frontend dev runs on **5173**
- If you see "ECONNREFUSED 8080", your proxy might target wrong port — update `vite.config.ts` to `8081`

## Production Deployment

To deploy the frontend to production:

1.  **Configure environment** — Ensure backend API URL is set appropriately. For production, update the Vite proxy or replace API calls with absolute URLs to your deployed backend.
2.  **Build the application:**

    ```bash
    npm run build
    ```

    Output in `dist/` folder (static files).

3.  **Serve with a web server** — Use Nginx, Apache, or a CDN:

    **Nginx example:**
    ```nginx
    server {
        listen 80;
        server_name your-domain.com;
        root /path/to/tic-tac-toe/frontend/dist;
        index index.html;

        location / {
            try_files $uri $uri/ /index.html;
        }

        location /api/ {
            proxy_pass http://localhost:8081;  # Backend
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
    ```

4.  **Set environment variables** for API base URL if needed. Consider replacing hardcoded proxy with environment-based API URL via Vite's `import.meta.env`.

5.  **Enable HTTPS** — Configure SSL certificates in your reverse proxy.

### Docker (Production Frontend)

The provided `frontend/Dockerfile` uses Nginx to serve static files. Build and run:

```bash
docker build -t tic-tac-toe-frontend:latest -f frontend/Dockerfile .
docker run -p 80:80 -e BACKEND_HOST=backend-api tic-tac-toe-frontend:latest
```

When using Docker Compose, the frontend container uses Nginx template (`nginx.conf.template`) to route `/api/*` to the backend.

## Code Quality

This project uses:

-   **ESLint** (`@eslint/js`, `eslint-plugin-react-hooks`, `eslint-plugin-react-refresh`) — Code quality and best practices enforcement
-   **TypeScript** (`~5.9.3`) — Static type checking
-   **Prettier** — Consistent code formatting

```bash
# Check code quality (ESLint)
npm run lint

# Auto-format all files (Prettier)
npm run format

# Verify formatting without changes
npm run format:check
```

ESLint and Prettier configurations are in `frontend/` package.json and config files.

## Architecture Notes

### State Management

-   **AuthContext** — Global auth state (user, token, login/logout functions)
-   **useGame hook** — Local game state (board, loading, errors) with side effects (polling, retry)
-   **localStorage** — Persistent token storage across page reloads

### Network Layer

-   `authorizedFetch` wrapper around `fetch` — adds Bearer token, handles 401 refresh automatically, queues concurrent requests during refresh
-   Retry logic in `useGame` — up to 3 attempts with exponential backoff (fixed 1s delay)
-   Polling interval — 2.5 seconds for game state updates

### Component Structure

-   **App.tsx** — Top-level routing and global layout
-   **LoginForm / RegisterForm** — Controlled forms with validation
-   **GameModeSelection** — Choose board size and AI/PvP
-   **UserProfile** — Displays user info; can navigate to history/leaderboard

## Contributing

Frontend guidelines:

-   Use TypeScript for all new code; define interfaces in `interfaces/`
-   Prefer functional components and hooks; avoid class components
-   Keep components small and focused (single responsibility)
-   Use `authorizedFetch` for all API calls — never use bare `fetch`
-   Handle errors gracefully; display user-friendly messages
-   Follow existing CSS pattern (CSS modules or styled-jsx if added); maintain dark theme consistency
-   Add storybook docs if component library grows (optional)

## License

Educational project.
