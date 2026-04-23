# Tic-Tac-Toe

[![Java 18](https://img.shields.io/badge/Java-18-orange?style=flat-square)](https://www.java.com/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![React 19.2.0](https://img.shields.io/badge/React-19.2.0-61dafb?style=flat-square&logo=react)](https://react.dev/)
[![TypeScript 5.9.3](https://img.shields.io/badge/TypeScript-5.9.3-3178c6?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Vite 7.2.4](https://img.shields.io/badge/Vite-7.2.4-646CFF?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ed?style=flat-square&logo=docker)](https://www.docker.com/)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)

## Description

A full-featured web application for the classic Tic-Tac-Toe game with JWT-based authentication, player profiles, leaderboards, game history, and modern multi-layer architecture. This project demonstrates best practices in development using Spring Boot for the backend and React for the frontend.

<p align="center">
  <img src="./assets/game_preview.png" width="600" alt="Tic-Tac-Toe Preview">
</p>

## Features

- ✅ **Spring Boot 3.3.4 Backend** — REST API with full OpenAPI/Swagger documentation
- ✅ **JWT Bearer Token Authentication** — Stateless, secure authentication with refresh token rotation
- ✅ **React 19.2.0 Frontend** — Modern reactive user interface with automatic token refresh
- ✅ **TypeScript 5.9.3** — Strict typing for reliable code
- ✅ **PostgreSQL 15** — Reliable storage for game and user data
- ✅ **Docker Compose** — Simple one-step deployment (3 containers: DB, Backend, Frontend)
- ✅ **Layered Architecture** — Clean separation (Web, Domain, Datasource layers)
- ✅ **Dependency Injection** — Spring DI container management
- ✅ **Game Features** — AI opponent, multiplayer (PvP), leaderboard, game history
- ✅ **API Documentation** — Interactive Swagger UI with Bearer auth support

## Screenshots

### Login Form

<p align="center">
  <img src="./assets/form_for_login.png" width="600" alt="Login Form">
</p>

### Registration Form

<p align="center">
  <img src="./assets/form_for_register.png" width="600" alt="Registration Form">
</p>

### Game Mode Menu

<p align="center">
  <img src="./assets/game_mode_menu.png" width="600" alt="Game Mode Menu">
</p>

## Project Structure

```text
Tic-Tac-Toe/
├── backend/                    # Spring Boot application
│   ├── src/main/java/org/example/
│   │   ├── Main.java          # Application entry point
│   │   ├── web/               # REST controllers (Auth, Game)
│   │   │   ├── controller/    # @RestController endpoints
│   │   │   ├── filter/        # JWT authentication filter
│   │   │   ├── mapper/        # DTO ↔ Entity converters
│   │   │   └── model/         # Request/Response DTOs
│   │   ├── domain/            # Business logic (Auth, Game, User)
│   │   │   ├── model/         # Domain entities
│   │   │   ├── repository/    # Repository interfaces
│   │   │   ├── service/       # Business logic services
│   │   │   └── exception/     # Custom exceptions
│   │   ├── datasource/        # Data access (JPA, PostgreSQL)
│   │   │   ├── model/         # JPA entities
│   │   │   └── repository/    # JPA implementations
│   │   └── di/config/         # DI and Security configuration
│   │       ├── GameConfig     # Game beans configuration
│   │       └── SecurityConfig # JWT + Spring Security setup
│   ├── build.gradle.kts       # Gradle configuration
│   └── Dockerfile             # Docker image for backend
│
├── frontend/                   # React application
│   ├── src/
│   │   ├── App.tsx           # Main component
│   │   ├── components/       # UI components (Login, Register, Game, Profile)
│   │   ├── contexts/         # React Context (AuthContext)
│   │   ├── hooks/            # Custom React hooks (useGame)
│   │   ├── interfaces/       # TypeScript interfaces
│   │   ├── utils/            # API utilities with token refresh
│   │   └── constants.ts      # Storage keys and constants
│   ├── package.json          # npm dependencies
│   ├── vite.config.ts        # Vite configuration with proxy
│   └── Dockerfile            # Docker image for frontend
│
└── docker-compose.yml        # Container orchestration (DB + Backend + Frontend)
```

## Prerequisites

### For Local Development

- **Java 18+** ([download](https://www.oracle.com/java/technologies/downloads/))
- **Node.js 20+** and npm ([download](https://nodejs.org/))
- **PostgreSQL 15+** (or use Docker Compose)
- **Gradle** (included via Gradle Wrapper)

### For Docker Deployment

- **Docker** 20.10+
- **Docker Compose** 2.0+

## 🚀 How to Use the Application

After starting the containers or local servers, the project is available at:

### 🖥 User Interface (Frontend)

The main game panel where all the magic happens.

- **URL:** [http://localhost:3001](http://localhost:3001) (Docker Compose)
- **URL:** [http://localhost:5173](http://localhost:5173) (local development)

### ⚙️ Developer Tools (Backend)

The backend runs as a **Headless REST API**. Use the following resources:

| Resource | Link | Description |
| --- | --- | --- |
| **Swagger UI** | [🔗 Open Documentation](http://localhost:8081/swagger-ui.html) | Interactive API documentation (authenticate with Bearer token) |
| **OpenAPI (JSON)** | [📄 Specification](http://localhost:8081/v3/api-docs) | Machine-readable API spec for client generation |

## 🔐 Authentication

The application uses **JWT (JSON Web Token) Bearer authentication**. After registering or logging in, you receive an access token (valid 1 hour) and a refresh token (valid 7 days). Include the access token in the `Authorization` header of all authenticated requests:

```
Authorization: Bearer <your-access-token>
```

### Token Flow

1. **Register** → `POST /auth/signup` with `{ "login": "user", "password": "pass" }` → Returns `accessToken` + `refreshToken`
2. **Login** → `POST /auth/signin` with credentials → Returns tokens
3. **Access protected endpoints** → Include `Authorization: Bearer <token>` header
4. **Refresh tokens** → `POST /auth/refresh/access` or `/auth/refresh/refresh` when access token expires

### Frontend Token Management

The frontend automatically handles token storage (localStorage) and refresh logic. When an API call returns 401, it attempts to refresh the token and retry the request transparently.

## 📡 API Request Examples

```bash
# Register a new user
curl -X POST "http://localhost:8081/auth/signup" \
     -H "Content-Type: application/json" \
     -d '{"login": "player1", "password": "secret123"}'

# Login (JWT signin)
curl -X POST "http://localhost:8081/auth/signin" \
     -H "Content-Type: application/json" \
     -d '{"login": "player1", "password": "secret123"}'

# Response: { "type": "Bearer", "accessToken": "...", "refreshToken": "..." }

# Create a new game (vs AI, default 3x3)
curl -X POST "http://localhost:8081/game?size=3&vsAi=true" \
     -H "Authorization: Bearer <your-access-token>"

# Get game status
curl "http://localhost:8081/game/{id}" \
     -H "Authorization: Bearer <your-access-token>"

# Make a move
curl -X POST "http://localhost:8081/game/{id}/move" \
     -H "Authorization: Bearer <your-access-token>" \
     -H "Content-Type: application/json" \
     -d '{"gameMap": {"map": [[1,0,0],[0,0,0],[0,0,0]], "size": 3}}'

# Get user profile
curl "http://localhost:8081/auth/me" \
     -H "Authorization: Bearer <your-access-token>"

# Get game history
curl "http://localhost:8081/game/history" \
     -H "Authorization: Bearer <your-access-token>"

# Get leaderboard (top 10)
curl "http://localhost:8081/game/leaderboard?n=10" \
     -H "Authorization: Bearer <your-access-token>"

# Join an active game (PvP)
curl -X POST "http://localhost:8081/game/{sessionId}/join?guestId=<your-uuid>" \
     -H "Authorization: Bearer <your-access-token>"

# Check if opponent left
curl -X POST "http://localhost:8081/game/{id}/check-opponent-left?timeoutSeconds=30" \
     -H "Authorization: Bearer <your-access-token>"

# Refresh access token
curl -X POST "http://localhost:8081/auth/refresh/access" \
     -H "Content-Type: application/json" \
     -d '{"refreshToken": "<your-refresh-token>"}'
```

## 🔧 Installation and Setup

### Option 1 — Docker Compose Deployment (Recommended)

- **Clone the repository**

```bash
git clone <repository-url>
cd Tic-Tac-Toe
```

- **Create .env file** (if not present)

```env
# Database
DB_NAME=game_sessions_storage
DB_USER=postgres
DB_PASSWORD=your_secure_password_here
DB_INTERNAL_PORT=5432
DB_EXTERNAL_PORT=5433

# Backend
BACKEND_INTERNAL_PORT=8080
BACKEND_EXTERNAL_PORT=8081

# Frontend
FRONTEND_EXTERNAL_PORT=3001

# JWT Secret (generate with: openssl rand -base64 32)
JWT_SECRET=your_jwt_secret_key_min_32_bytes_base64_encoded
```

> **Security Note:** Generate a strong JWT secret for production. Use `openssl rand -base64 32` or a password manager. The secret must be at least 256 bits (32 bytes) for HS256.

- **Start the application**

```bash
docker compose up --build -d
```

- **Open the application**

- Frontend: [http://localhost:3001](http://localhost:3001)
- Backend API: [http://localhost:8081](http://localhost:8081)
- API Documentation: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

- **Stop the application**

```bash
docker compose down
```

### Option 2 — Local Development

#### PostgreSQL

```bash
docker run -d --name postgres-db \
  -e POSTGRES_DB=game_sessions_storage \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  postgres:15-alpine
```

#### Backend (Spring Boot)

- **Navigate to backend directory**

```bash
cd backend
```

- **Set environment variables**

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/game_sessions_storage
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=$(openssl rand -base64 32)
```

- **Build and run**

```bash
./gradlew build
./gradlew bootRun
```

Backend will be available at [http://localhost:8081](http://localhost:8081)

On Windows:
```bash
gradlew.bat bootRun
```

#### Frontend (React)

- **Navigate to frontend directory**

```bash
cd frontend
```

- **Install dependencies**

```bash
npm install
```

- **Start dev server**

```bash
npm run dev
```

Frontend will be available at [http://localhost:5173](http://localhost:5173) (Vite dev server). The dev server proxies `/auth` and `/game` requests to `http://localhost:8081`.

## Available Commands

### Backend (Gradle)

| Command | Description |
| --- | --- |
| `./gradlew build` | Build the project |
| `./gradlew bootRun` | Run the application |
| `./gradlew test` | Run tests (H2 in-memory database) |
| `./gradlew clean` | Clean build artifacts |

### Frontend (npm)

| Command | Description |
| --- | --- |
| `npm run dev` | Development with hot reload and proxy |
| `npm run build` | Production build |
| `npm run lint` | Code check with ESLint |
| `npm run format` | Code formatting with Prettier |
| `npm run preview` | Preview production build |

## 📚 API Documentation

### About the Backend

The backend is a **headless REST API** built on Spring Boot 3 with JWT authentication. It provides complete game logic, user management, and statistics through HTTP endpoints.

### Main Endpoints

#### Authentication

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| `POST` | `/auth/signup` | Register new user | No |
| `POST` | `/auth/signin` | Login (returns JWT tokens) | No |
| `POST` | `/auth/refresh/access` | Refresh access token | No (requires refresh token) |
| `POST` | `/auth/refresh/refresh` | Refresh refresh token (rotation) | No (requires refresh token) |
| `GET` | `/auth/me` | Get current user profile | Yes (Bearer) |

#### User

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| `GET` | `/auth/{id}` | Get user by ID (self or admin) | Yes (Bearer) |

#### Game

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| `POST` | `/game?size=3&vsAi=true` | Create new game session (vs AI or PvP) | Yes (Bearer) |
| `POST` | `/game/{id}/move` | Submit a move (X), AI responds automatically (O) | Yes (Bearer) |
| `GET` | `/game/{id}` | Get game status | Yes (Bearer) |
| `GET` | `/game/active` | List all available (waiting) games | Yes (Bearer) |
| `POST` | `/game/{id}/join` | Second player joins existing game (PvP) | Yes (Bearer) |
| `POST` | `/game/{id}/check-opponent-left` | Check if opponent abandoned the game | Yes (Bearer) |
| `GET` | `/game/history` | Get all finished games for current user | Yes (Bearer) |
| `GET` | `/game/leaderboard?n=10` | Top N players by win rate | Yes (Bearer) |

### Interactive Documentation

After starting the backend, full API documentation is available in Swagger UI:

- **Swagger UI:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

> **Note:** Swagger UI supports Bearer token authentication. Click the "Authorize" button and enter `Bearer <your-access-token>` to test protected endpoints.

## Architecture

### Backend Architecture (Layered)

```text
┌─────────────────────────────────────────┐
│       Web Layer (Controller)            │
│     HTTP Request Processing             │
│     AuthController, GameController      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Domain Layer (Service)             │
│     Application Business Logic          │
│     AuthService, GameService, UserService
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│    Datasource Layer (Repository)        │
│   PostgreSQL Integration (JPA/Hibernate)│
└─────────────────────────────────────────┘
```

### Components

- **Web Layer**: REST controllers, DTO models, mappers, JWT authentication filter
- **Domain Layer**: Business logic, entity models, repository interfaces, services, game AI (Minimax)
- **Datasource Layer**: JPA repositories, entity mappers, PostgreSQL
- **DI Configuration**: Spring bean configuration, JWT provider, Security configuration (stateless, Bearer token)

### Frontend Architecture

- **React Components**: Functional components (LoginForm, RegisterForm, GameModeSelection, UserProfile)
- **Context API**: AuthContext for authentication state management with token persistence
- **Custom Hooks**: Reusable game logic (useGame) with polling, retry, and validation
- **TypeScript Interfaces**: Strict typing for API contracts
- **Vite**: Fast development with proxy configuration for backend API
- **API Utility**: Centralized `authorizedFetch` with automatic token refresh on 401

### Security Architecture

- **Stateless JWT**: No server-side sessions; tokens contain user ID and roles
- **Password Hashing**: BCrypt with automatic salt generation
- **Token Expiration**: Access tokens (1h), refresh tokens (7d)
- **Token Rotation**: Refresh endpoints issue new refresh tokens, invalidating old ones
- **Role-Based Access**: `USER` role default; future `ADMIN` support with `@PreAuthorize`

## Dependencies

### Backend

- **Java 18**
- **Spring Boot 3.3.4**
- **SpringDoc OpenAPI 2.6.0** — Swagger UI integration
- **Spring Security** — JWT Bearer token authentication
- **Spring Data JPA** — Database abstraction
- **jjwt 0.13.0** — JWT token generation and validation
- **PostgreSQL** — Primary database
- **H2** — Test database (in-memory)

### Frontend

- **React 19.2.0**
- **TypeScript 5.9.3**
- **Vite 7.2.4**
- **ESLint** — Code quality
- **Prettier** — Code formatting

## Configuration

### Backend Environment Variables

Required variables (set in `.env`, Docker Compose, or CI/CD):

| Variable | Description | Example |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5433/game_sessions_storage` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secure_password` |
| `JWT_SECRET` | JWT signing secret (HS256, min 256-bit) | `base64-encoded-secret` |

Optional (with defaults):

| Variable | Default | Description |
| --- | --- | --- |
| `jwt.access-token-validity` | `3600` | Access token validity in seconds (1 hour) |
| `jwt.refresh-token-validity` | `604800` | Refresh token validity in seconds (7 days) |

Application properties: `backend/src/main/resources/application.properties`

### Frontend Proxy Configuration

Vite proxy (`frontend/vite.config.ts`) forwards API requests:

```typescript
server: {
  proxy: {
    '/game': { target: 'http://localhost:8081', changeOrigin: true },
    '/auth': { target: 'http://localhost:8081', changeOrigin: true },
  }
}
```

## Production Deployment

For production deployments:

1. Use Docker Compose with externalized configuration
2. Generate a strong `JWT_SECRET` (minimum 32 random bytes, stored securely)
3. Use a managed PostgreSQL service or persistent volumes
4. Configure a reverse proxy (Nginx) for SSL termination
5. Set appropriate CORS origins in `SecurityConfig.java`
6. Build backend: `./gradlew build -x test` (produces JAR in `backend/build/libs/`)
7. Build frontend: `npm run build` (static assets in `frontend/dist/`)
8. Serve frontend via Nginx or CDN

Example production `docker-compose.yml` volumes and secrets are recommended for sensitive data.

## Testing

### Backend

```bash
cd backend
./gradlew test                    # Run all tests with H2 database
./gradlew test --tests org.example.domain.service.GameServiceTest  # Specific test
./gradlew test --info             # Verbose output
./gradlew jacocoTestReport        # Generate coverage report (HTML in build/reports)
```

### Frontend

```bash
cd frontend
npm run lint                      # ESLint code quality check
npm run format:check              # Verify Prettier formatting
```

## Troubleshooting

### JWT Token Expired

The frontend automatically attempts token refresh. If refresh fails (401 on refresh endpoint), you will be redirected to login.

### Port Conflicts

- Backend default: 8081 (change in `application.properties` or Docker port mapping)
- Frontend dev: 5173, prod: 80 (Docker)
- PostgreSQL: 5433 external (mapped from container 5432)

### Database Connection Issues

- Verify PostgreSQL is running: `docker ps` or `pg_isready`
- Check environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- Docker network: ensure `backend-api` container can reach `db` container (compose network)

### API Calls Fail with 401

- Ensure you have a valid access token (not expired)
- Check `Authorization: Bearer <token>` header is present
- Refresh token if needed via `/auth/refresh/access`
- Verify frontend proxy is running for dev: Vite must be running on port 5173

### Swagger UI Authorization

1. Call `/auth/signin` to get tokens (use Swagger "Try it out")
2. Copy the `accessToken` value
3. Click "Authorize" button (top-right) → enter `Bearer <accessToken>` → Authorize
4. Now you can test protected endpoints

## Contributing

Follow standard Spring Boot and React best practices:
- Layered architecture: controllers → services → repositories
- Dependency injection for all beans
- Single responsibility per class
- DTOs for API contracts; no entity exposure
- Comprehensive error handling with `@ControllerAdvice`
- Unit tests for services, integration tests for controllers

## License

This project is created for educational purposes.
