# Tic-Tac-Toe Backend

A Spring Boot 3.3.4 REST API backend for the Tic-Tac-Toe game with JWT Bearer token authentication, layered architecture, PostgreSQL persistence, and comprehensive game management features.

## 🚀 Features

-   **Spring Boot 3.3.4** — Modern Java framework with built-in best practices
-   **JWT Bearer Token Authentication** — Stateless authentication with access and refresh tokens (HS256)
-   **Spring Security** — Role-based access control (`USER` role) and method-level security
-   **PostgreSQL** — Reliable data persistence for users and game sessions
-   **Spring Data JPA** — Clean data access abstraction with Hibernate
-   **Swagger UI** — Interactive API documentation at `/swagger-ui.html` with Bearer auth support
-   **Layered Architecture** — Clean separation (Web → Domain → Datasource layers)
-   **Dependency Injection** — Full Spring DI container management
-   **Game Features** — AI opponent (Minimax algorithm), PvP multiplayer, game history, leaderboard
-   **Stateless Design** — No HTTP sessions; all state managed via JWT tokens

## 📋 Getting Started

### Prerequisites

-   Java 18+
-   PostgreSQL 15+ (or use Docker Compose)
-   Gradle (wrapper included)

### Installation

1.  **Clone the repository** (if you haven't already):

    ```bash
    git clone <repository-url>
    cd backend
    ```

2.  **Configure environment variables**

    The backend requires the following environment variables. It is recommended to create a `.env` file in the `backend` directory:

    ```bash
    cp .env.example .env
    ```

    Then edit `.env` with your values:

    | Variable                     | Description                       | Example |
    | ---------------------------- | --------------------------------- | ------- |
    | `SPRING_DATASOURCE_URL`      | PostgreSQL connection URL         | `jdbc:postgresql://localhost:5433/game_sessions_storage` |
    | `SPRING_DATASOURCE_USERNAME` | Database username                 | `postgres` |
    | `SPRING_DATASOURCE_PASSWORD` | Database password                 | `secure_password` |
    | `JWT_SECRET`                 | JWT signing key (min 256-bit HS256) | `base64-secret-key` |

    **Security:** Never hardcode these values in `application.properties`. Use environment variables or Docker secrets. The `JWT_SECRET` must be at least 32 random bytes for HS256. Generate with: `openssl rand -base64 32`.

3.  **Start PostgreSQL** (if not using Docker Compose):

    ```bash
    docker run -d --name postgres-db \
      -e POSTGRES_DB=game_sessions_storage \
      -e POSTGRES_USER=postgres \
      -e POSTGRES_PASSWORD=postgres \
      -p 5433:5432 \
      postgres:15-alpine
    ```

4.  **Run the application**

    ```bash
    ./gradlew bootRun
    ```

    The API will be available at `http://localhost:8081`

    On Windows:
    ```bash
    gradlew.bat bootRun
    ```

### Environment Variables Summary

| Variable                     | Required | Description | Default (if configurable) |
| ---------------------------- | -------- | ----------- | ------------------------- |
| `SPRING_DATASOURCE_URL`      | Yes      | JDBC PostgreSQL URL | — |
| `SPRING_DATASOURCE_USERNAME` | Yes      | DB username | — |
| `SPRING_DATASOURCE_PASSWORD` | Yes      | DB password | — |
| `JWT_SECRET`                 | Yes      | HS256 secret (min 256-bit) | — |
| `jwt.access-token-validity`  | No       | Access token TTL in seconds | `3600` (1 hour) |
| `jwt.refresh-token-validity` | No       | Refresh token TTL in seconds | `604800` (7 days) |

## 📦 Available Scripts

```bash
# Start the application
./gradlew bootRun

# Build the project
./gradlew build

# Run tests (H2 in-memory database)
./gradlew test

# Clean build artifacts
./gradlew clean

# View detailed build logs
./gradlew build --stacktrace

# Generate test coverage report
./gradlew jacocoTestReport  # HTML report in build/reports/jacoco/test/html
```

## 🏗️ Project Structure

```
src/
├── main/java/org/example/
│   ├── Main.java               # Application entry point (SpringBootApplication)
│   ├── web/                    # REST Controller layer
│   │   ├── controller/         # @RestController endpoints
│   │   │   ├── AuthController  # /auth/* endpoints (signup, signin, refresh, me)
│   │   │   ├── GameController  # /game endpoints (create, move, join, history, leaderboard)
│   │   │   └── GlobalExceptionHandler  # Centralized error handling
│   │   ├── filter/             # AuthFilter (JWT Bearer token validation)
│   │   ├── mapper/             # DTO ↔ Entity converters
│   │   └── model/              # Request/Response DTOs (JwtRequest, JwtResponse, GameSessionDTO, etc.)
│   ├── domain/                 # Business logic layer
│   │   ├── model/              # Domain entities (Game, GameMap, User, JwtAuthentication)
│   │   ├── repository/         # Repository interfaces (GameRepository, UserRepository)
│   │   ├── service/            # Business logic
│   │   │   ├── AuthService     # JWT token generation, validation, refresh
│   │   │   ├── GameService     # Game rules, AI moves (Minimax), history, leaderboard
│   │   │   └── UserService     # User CRUD and profile management
│   │   └── exception/          # Custom exceptions (DuplicateUserException, etc.)
│   ├── datasource/             # Data access layer
│   │   ├── mapper/             # Custom entity mappers
│   │   ├── model/              # JPA entities (UserEntity, GameSessionEntity, GameMapEntity, etc.)
│   │   └── repository/         # Spring Data JPA implementations (JpaGameRepository, etc.)
│   └── di/config/              # Spring @Configuration classes
│       ├── GameConfig          # Game beans configuration (AI strategy, mappers)
│       └── SecurityConfig      # Spring Security + JWT Bearer token setup
├── resources/
│   └── application.properties  # Server, database, JWT configuration
└── test/java/org/example/      # Unit & integration tests (H2 database)
```

## 🎮 How It Works

### Authentication Flow (JWT Bearer Tokens)

#### 1. Registration (`POST /auth/signup`)

**Request:**
```json
{
  "login": "player1",
  "password": "secret123"
}
```

**Response (201 Created):**
```json
{
  "type": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Password is hashed with BCrypt; user created; JWT tokens generated.

#### 2. Login (`POST /auth/signin`)

**Request:**
```json
{
  "login": "player1",
  "password": "secret123"
}
```

**Response (200 OK):**
```json
{
  "type": "Bearer",
  "accessToken": "...",
  "refreshToken": "..."
}
```

Credentials validated; tokens returned.

#### 3. Access Protected Endpoints

Include the access token in the `Authorization` header:

```
Authorization: Bearer <access-token>
```

All `/game/**` endpoints and `/auth/me` require authentication.

#### 4. Token Refresh

**Refresh access token:**
```bash
curl -X POST "http://localhost:8081/auth/refresh/access" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "current-refresh-token"}'
```

**Refresh refresh token (rotate):**
```bash
curl -X POST "http://localhost:8081/auth/refresh/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "current-refresh-token"}'
```

Both return:
```json
{
  "type": "Bearer",
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token"
}
```

Refresh token rotation: each call invalidates the previous refresh token.

### Game Flow

#### Create Game (`POST /game?size=3&vsAi=true`)

Creates a new game session. By default (`vsAi=true`), you play against AI (O). Set `vsAi=false` for PvP.

**Response (201):**
```json
{
  "id": "uuid-here",
  "gameMap": {
    "map": [[0,0,0],[0,0,0],[0,0,0]],
    "size": 3
  },
  "status": "PLAYER_TURN",
  "playerX": "your-uuid",
  "playerO": null,
  "currentPlayer": "your-uuid",
  "winner": null,
  "lastActiveAt": "2025-01-15T10:30:00Z",
  "createdAt": "2025-01-15T10:25:00Z"
}
```

Cell values: `0 = EMPTY`, `1 = CROSS (X)`, `2 = ZERO (O)`.

#### Make a Move (`POST /game/{id}/move`)

Submit your move; backend automatically responds with AI move (if `vsAi=true`).

**Request:**
```json
{
  "gameMap": {
    "map": [[1,0,0],[0,0,0],[0,0,0]],
    "size": 3
  }
}
```

**Response (200):** Updated game state with AI's move included.

#### Join Game (PvP) (`POST /game/{id}/join?guestId=<uuid>`)

Second player joins an existing waiting game.

#### Check Opponent Left (`POST /game/{id}/check-opponent-left?timeoutSeconds=30`)

Manually check if the opponent abandoned the game (useful for polling).

#### Get Game Status (`GET /game/{id}`)

Returns current board, status, players, and timestamps.

#### Get Active Games (`GET /game/active`)

List all games waiting for a second player (status = `WAITING_FOR_PLAYERS`).

#### Get Game History (`GET /game/history`)

Returns all completed games (status `VICTORY` or `DRAW`) for the current user.

#### Get Leaderboard (`GET /game/leaderboard?n=10`)

Returns top N players sorted by win rate.

**Response:**
```json
[
  {
    "userId": "uuid",
    "login": "player1",
    "winRate": 0.85
  }
]
```

### API Endpoints Summary

#### Authentication endpoints (public unless noted)

| Method | Path | Description | Auth |
| ------ | ---- | ----------- | ---- |
| `POST` | `/auth/signup` | Register new user | No |
| `POST` | `/auth/signin` | Login with credentials | No |
| `POST` | `/auth/refresh/access` | Refresh access token using refresh token | No |
| `POST` | `/auth/refresh/refresh` | Rotate refresh token | No |
| `GET`  | `/auth/me` | Get current user info (from JWT) | Yes |
| `GET`  | `/auth/{id}` | Get user by ID (self or ADMIN) | Yes |

#### Game endpoints (all require Bearer auth)

| Method | Path | Description |
| ------ | ---- | ----------- |
| `POST` | `/game` | Create new game (`?size=3&vsAi=true`) |
| `POST` | `/game/{id}/move` | Submit a move |
| `GET`  | `/game/{id}` | Get game state |
| `GET`  | `/game/active` | List available (waiting) games |
| `POST` | `/game/{id}/join` | Join as second player (PvP) |
| `POST` | `/game/{id}/check-opponent-left` | Check for abandoned opponent |
| `GET`  | `/game/history` | Get user's completed games |
| `GET`  | `/game/leaderboard?n=10` | Top players by win rate |

#### Public endpoints

- `GET /` → Redirects to Swagger UI
- `GET /swagger-ui.html` → Swagger UI interface
- `GET /v3/api-docs` → OpenAPI JSON specification

## 🔧 Configuration

### Application Properties

Location: `src/main/resources/application.properties`

```properties
server.error.include-message=always
spring.application.name=tic-tac-toe-backend

# Database (via environment variables)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# JWT (via environment variables)
jwt.secret=${JWT_SECRET}
jwt.access-token-validity=3600
jwt.refresh-token-validity=604800
```

### Changing Server Port

In `application.properties`:
```properties
server.port=8082
```

Or via command line:
```bash
./gradlew bootRun --args='--server.port=8082'
```

## 📚 API Documentation

### Swagger UI

Interactive API documentation and testing:

```
http://localhost:8081/swagger-ui.html
```

**Authenticating in Swagger:**
1. Call `POST /auth/signin` via Swagger to get tokens
2. Click "Authorize" (top-right)
3. Enter `Bearer <your-access-token>`
4. Protected endpoints become testable

### OpenAPI Specification

Machine-readable API spec in JSON:

```
http://localhost:8081/v3/api-docs
```

Can be imported into Postman or other API clients.

## 🐳 Docker

### Build Image

```bash
docker build -t tic-tac-toe-backend:latest .
```

### Run Container

```bash
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/game_sessions_storage \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=your_jwt_secret_here \
  tic-tac-toe-backend:latest
```

> **Note on Docker networking:** When using Docker Desktop on macOS/Windows, use `host.docker.internal` to reach host PostgreSQL. With Docker Compose, use the service name `db` (as defined in `docker-compose.yml`).

### Using Docker Compose (Recommended)

From project root:

```bash
docker compose up backend-api
```

The frontend will automatically connect to the API on `http://localhost:8081` via Docker network (`backend-api` hostname within compose network).

## Production Deployment

For production deployments:

1. **JWT Secret:** Generate a strong random secret (32+ bytes) and store securely (Docker secrets, Kubernetes secrets, or environment manager). Never use the development secret.
2. **Database:** Use managed PostgreSQL with persistent volumes, regular backups, and connection pooling (PgBouncer recommended).
3. **Reverse Proxy:** Configure Nginx/Traefik for SSL termination, HTTP/2, and static asset serving.
4. **CORS:** Whitelist frontend origins in `SecurityConfig.java` (`.cors(cors -> cors.configurationSource(...))`).
5. **Build optimizations:**
   ```bash
   ./gradlew build -x test    # Skip tests for faster builds (if desired)
   ```
   Artifact: `backend/build/libs/tic-tac-toe-backend-1.0-SNAPSHOT.jar`.
6. **Monitoring:** Enable Spring Boot Actuator (`spring-boot-starter-actuator`) for health checks, metrics, and logging.
7. **Logging:** Configure structured logging (JSON) and centralized aggregation (ELK, Loki).

### Example production `application-prod.properties`

```properties
server.port=8080
spring.jpa.hibernate.ddl-auto=validate  # Or 'none' in production; use migrations
spring.jpa.show-sql=false
logging.level.org.example=INFO
```

Activate with: `-Dspring.profiles.active=prod`.

## 🧪 Testing

### Unit & Integration Tests

```bash
# Run all tests (H2 in-memory database)
./gradlew test

# Run specific test class
./gradlew test --tests org.example.domain.service.GameServiceTest

# Run with verbose output
./gradlew test --info

# Generate code coverage report (HTML)
./gradlew jacocoTestReport
# Open: build/reports/jacoco/test/html/index.html
```

### Test Structure

- `src/test/java/org/example/domain/service/` — Service unit tests (game logic, auth)
- `src/test/java/org/example/web/controller/` — Controller integration tests (MockMvc)
- `src/test/java/org/example/datasource/` — Repository integration tests
- H2 database configured for isolation; no external dependencies

## 🔍 Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build

# Show detailed errors
./gradlew build --stacktrace --info
```

### Port 8081 Already in Use

Change port in `application.properties` or pass as argument:
```bash
./gradlew bootRun --args='--server.port=8082'
```

### Database Connection Error

- Ensure PostgreSQL is running: `docker ps` or `pg_isready -p 5433`
- Check environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- Verify URL format: `jdbc:postgresql://host:port/dbname`
- For Docker containers, ensure network connectivity between `backend-api` and `db` services

### JWT Secret Not Set

Application fails to start with error:
```
IllegalArgumentException: JWT secret is required
```

Set `JWT_SECRET` environment variable (see .env.example).

### API Returns 401 Unauthorized

- Ensure `Authorization: Bearer <token>` header is included
- Check token expiration (access token valid for 1 hour)
- Refresh token via `/auth/refresh/access` if access token expired
- Verify token structure in request (no extra spaces, correct "Bearer " prefix)

### Swagger UI Not Loading

Ensure app is running: `curl http://localhost:8081/swagger-ui.html`

Check startup logs for errors: `./gradlew bootRun`

### Frontend Can't Connect to API

For local dev: ensure Vite dev server is running and proxy is configured to `http://localhost:8081`. Check browser console for CORS or network errors.

Verify backend is accessible:
```bash
curl -X POST http://localhost:8081/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"login":"test","password":"test"}'
```

## 🛠️ Development

### Layered Architecture Pattern

-   **web**: HTTP requests/responses, DTOs, validation, JWT authentication filter
-   **domain**: Pure business logic, game rules, Minimax AI strategy, auth service, user service
-   **datasource**: Data persistence via JPA/Hibernate + PostgreSQL
-   **di**: Spring bean configuration, JWT provider, Security configuration

### Adding New Features

1. Create domain model in `domain.model`
2. Define repository interface in `domain.repository`
3. Implement business logic in `domain.service`
4. Add HTTP endpoint in `web.controller`
5. Create JPA entity in `datasource.model`
6. Implement repository in `datasource.repository`
7. Write unit tests in `src/test/`
8. Update OpenAPI annotations for Swagger documentation

### Code Quality Standards

-   Follow Spring Boot conventions and dependency injection
-   Keep single responsibility principle per class
-   Use DTOs for API boundaries; never expose entities directly
-   Add JavaDoc for public methods and complex logic
-   Write unit tests for services (business logic) and integration tests for controllers
-   Use `@Validated` and `@Valid` for request validation where applicable
-   Handle exceptions via `@ControllerAdvice` (GlobalExceptionHandler)

### JWT Security Notes

- Tokens are signed with HS256 using `JWT_SECRET`
- Access token claims: `sub` (user UUID), `roles` (array), `iat`, `exp`, `iss`
- Tokens stateless; no persistence or blacklist
- Refresh tokens rotate on each use; old tokens become invalid after rotation
- PasswordEncoder: BCrypt with default strength (10 rounds)

## 📖 Key Implementation Details

### Token Validation Flow (`AuthFilter.java`)

1. Extract `Authorization` header → ensure it starts with `Bearer `
2. Strip prefix, extract token
3. Call `JwtProvider.validateAccessToken(token)` — checks signature, expiration, issuer
4. Parse claims: `sub` → user UUID, `roles` → authorities
5. Create `JwtAuthentication` object (implements `Authentication`)
6. Set in `SecurityContextHolder.getContext().setAuthentication(auth)`
7. Continue filter chain; if invalid → 401 response immediately

### Game AI (Minimax)

The AI (`MinimaxAiStrategy`) uses the Minimax algorithm with alpha-beta pruning to choose optimal moves. It's deterministic and always plays optimally on 3x3; for larger boards a depth-limited heuristic is used.

### Leaderboard Calculation

Leaderboard (`GameService.getLeaderboard(limit)`) queries all users' game statistics and sorts by win rate (wins / total games). Only users with ≥1 completed game are included.

### Game History

Game history stores only **completed** games (status `VICTORY` or `DRAW`). Active/in-progress games are not included. History is retrieved via `GameService.getGameHistory(userId)`.

## 📄 License

Educational project.
