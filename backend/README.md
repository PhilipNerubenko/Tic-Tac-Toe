# Tic-Tac-Toe Backend

A Spring Boot 3.3.4 REST API backend for the Tic-Tac-Toe game with layered architecture, basic authentication, and PostgreSQL persistence.

## 🚀 Features

-   **Spring Boot 3.3.4** — Modern Java framework with built-in best practices
-   **Spring Security + Basic Auth** — Basic authentication and authorization
-   **PostgreSQL** — Reliable data persistence for users and game sessions
-   **Spring Data JPA** — Clean data access abstraction with Hibernate
-   **Swagger UI** — Interactive API documentation at `/swagger-ui.html`
-   **Layered Architecture** — Clean separation (Web → Domain → Datasource layers)
-   **Dependency Injection** — Full Spring DI container management
-   **Game Sessions** — Support for multiple concurrent games per user
-   **AI Opponent** — Built-in AI logic for O player moves
-   **User Management** — Registration, login, and profile management

## 📋 Getting Started

### Prerequisites

-   Java 18+
-   PostgreSQL 15+ (or use Docker Compose)
-   Gradle (wrapper included)

### Installation

1.  Clone the repository (if you haven't already):

    ```bash
    git clone <repository-url>
    cd backend
    ```

2.  Configure environment variables:

    To configure the backend, you need to set the following environment variables. It is recommended to create a `.env` file in the `backend` directory.

    *   Copy the example file: `cp .env.example .env`
    *   Fill in the values in `.env`.

    The following variables are required:

    | Variable                     | Description                       | Example                                                                |
    | ---------------------------- | --------------------------------- | ---------------------------------------------------------------------- |
    | `SPRING_DATASOURCE_URL`      | PostgreSQL connection URL         | `jdbc:postgresql://localhost:5433/game_sessions_storage`             |
    | `SPRING_DATASOURCE_USERNAME` | Database username                 | `postgres`                                                             |
    | `SPRING_DATASOURCE_PASSWORD` | Database password                 | `postgres`                                                             |

    **Important:** Never hardcode these values directly in your `application.properties` file. Use environment variables to keep your credentials secure.  It is recommended to use a `.env` file or Docker Secrets to manage these variables in a production environment.

3.  Start PostgreSQL (if you're not using Docker Compose):

    ```bash
    docker run -d --name postgres-db \
      -e POSTGRES_DB=game_sessions_storage \
      -e POSTGRES_USER=postgres \
      -e POSTGRES_PASSWORD=postgres \
      -p 5433:5432 \
      postgres:15-alpine
    ```

4.  Run the application:

    ```bash
    ./gradlew bootRun
    ```

    The API will be available at `http://localhost:8081`

    On Windows:
    ```bash
    gradlew.bat bootRun
    ```

### Environment Variables

The backend requires the following environment variables:

| Variable                     | Description                       | Example                                                                |
| ---------------------------- | --------------------------------- | ---------------------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL connection URL         | `jdbc:postgresql://localhost:5433/game_sessions_storage`             |
| `SPRING_DATASOURCE_USERNAME` | Database username                 | `postgres`                                                             |
| `SPRING_DATASOURCE_PASSWORD` | Database password                 | `postgres`                                                             |

## 📦 Available Scripts

```bash
# Start the application
./gradlew bootRun

# Build the project
./gradlew build

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean

# View detailed build logs
./gradlew build --stacktrace
```

## 🏗️ Project Structure

```
src/
├── main/java/org/example/
│   ├── Main.java               # Application entry point
│   ├── web/                    # REST Controller layer
│   │   ├── controller/         # @RestController endpoints
│   │   │   ├── AuthController  # /auth/signup, /auth/login
│   │   │   ├── GameController  # /game endpoints
│   │   │   └── GlobalExceptionHandler
│   │   ├── filter/             # AuthFilter (Basic Auth validation)
│   │   ├── mapper/             # DTO ↔ Entity mappers
│   │   └── model/              # Request/Response DTOs
│   ├── domain/                 # Business logic layer
│   │   ├── model/              # Domain entities (Game, GameMap, User)
│   │   ├── repository/         # Repository interfaces
│   │   ├── service/            # Business logic
│   │   │   ├── AuthService     # Basic auth management
│   │   │   ├── GameService     # Game rules and AI logic
│   │   │   └── UserService     # User management
│   │   └── exception/          # Custom exceptions
│   ├── datasource/             # Data access layer
│   │   ├── mapper/             # Custom entity mappers
│   │   ├── model/              # JPA entities (UserEntity, GameSessionEntity, etc.)
│   │   └── repository/         # Spring Data JPA implementations
│   └── di/config/              # Spring @Configuration classes
│       ├── GameConfig          # Game beans configuration
│       └── SecurityConfig      # Spring Security + Basic Auth setup
├── resources/
│   └── application.properties  # Server and database config
└── test/java/org/example/      # Unit & integration tests
```

## 🎮 How It Works

### Authentication Flow

1.  **Register**: `POST /auth/signup` creates a new user account
2.  **Login**: `POST /auth/login` authenticates the user
3.  **Authenticate**: Include `Authorization: Basic <base64-encoded-credentials>` header in subsequent requests

### Game Flow

1.  **Create Game**: `POST /game?size=3` creates a new game session (requires auth)
2.  **Player Move**: `POST /game/{id}` with game state containing your move (X)
3.  **AI Response**: Backend processes the move and calculates AI move (O)
4.  **Game Status**: Response includes updated board and game state (PLAYING/WIN/DRAW)

### API Endpoints

#### Authentication

| Method | Endpoint       | Description         | Auth Required |
| ------ | -------------- | ------------------- | ------------- |
| `POST` | `/auth/signup` | Register new user   | No            |
| `POST` | `/auth/login`  | Login and get session | No            |

#### User

| Method | Endpoint      | Description            | Auth Required |
| ------ | ------------- | ---------------------- | ------------- |
| `GET`  | `/user/profile` | Get current user profile | Yes           |

#### Game

| Method | Endpoint    | Description                                    | Auth Required |
| ------ | ----------- | ---------------------------------------------- | ------------- |
| `POST` | `/game?size=3` | Create new game (size parameter: 3 or higher) | Yes           |
| `POST` | `/game/{id}`  | Submit move and get AI response              | Yes           |
| `GET`  | `/game/{id}`   | Get game status                                | Yes           |
| `GET`  | `/game`      | List all user's games                          | Yes           |

### Response Format

```json
{
  "id": "uuid",
  "gameMap": {
    "map": [[1, 2, 1], [2, 0, 0], [0, 0, 0]],
    "size": 3
  },
  "status": "PLAYING"
}
```

Map values: `0 = empty`, `1 = X (player)`, `2 = O (AI)`

## 🔧 Configuration

### Application Properties

Located at `src/main/resources/application.properties`

Key settings:
```properties
# Server port
server.port=8081

# Database connection (via environment variables)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# Hibernate settings
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Changing Port

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
  tic-tac-toe-backend:latest
```

### Using Docker Compose

From project root:
```bash
docker compose up backend-api
```

The frontend will automatically connect to the API on `http://localhost:8081`

## Production Deployment

For production deployments, it is highly recommended to use a Docker containerization strategy with a tool like Docker Compose. You should also configure a reverse proxy (e.g., Nginx) to handle SSL termination and serve static assets. Environment variables should be passed to the container using a secure mechanism like Docker Secrets or a `.env` file that is not committed to the repository.

To build the project for production, run:
```bash
./gradlew build -x test
```
This will create a production-ready JAR file in the `build/libs` directory.

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests org.example.domain.service.GameServiceTest

# Run with detailed output
./gradlew test --info
```

Tests use H2 in-memory database for isolation.

## 🔍 Troubleshooting

### Build fails

```bash
# Clean and rebuild
./gradlew clean build

# Show detailed errors
./gradlew build --stacktrace
```

### Port 8080 already in use

Change the port in `application.properties` or use the command line option shown above.

### Database connection error

- Ensure PostgreSQL is running
- Check environment variables are set correctly
- Verify connection URL format: `jdbc:postgresql://host:port/dbname`

### API Documentation not loading

Ensure the application is running:
```bash
curl http://localhost:8081/swagger-ui.html
```

Check logs for startup errors:
```bash
./gradlew bootRun
```

### Frontend can't connect to API

Verify backend is running at `http://localhost:8081`:
```bash
curl -X POST http://localhost:8081/auth/signup \
     -H "Content-Type: application/json" \
     -d '{"username":"test","password":"test"}'
```

Check CORS and network configurations.

## 🛠️ Development

### Layered Architecture Pattern

-   **web**: HTTP requests/responses, DTOs, validation, Basic Auth filter
-   **domain**: Pure business logic, game rules, AI strategy, auth service
-   **datasource**: Data persistence via JPA/Hibernate + PostgreSQL
-   **di**: Spring bean configuration, Security configuration

### Adding Features

1.  Create data model in `domain.model`
2.  Define repository interface in `domain.repository`
3.  Implement logic in `domain.service`
4.  Add HTTP endpoint in `web.controller`
5.  Create JPA entity in `datasource.model`
6.  Implement repository in `datasource.repository`
7.  Write tests in `src/test/`

### Code Quality

-   Follow Spring Boot conventions
-   Use dependency injection
-   Keep single responsibility principle
-   Add JavaDoc for public methods
-   Write unit tests for services and controllers
