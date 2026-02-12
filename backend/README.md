# Tic-Tac-Toe Backend

A Spring Boot 3.2.2 REST API backend for the Tic-Tac-Toe game with layered architecture and interactive API documentation.

## 🚀 Features

- **Spring Boot 3.2.2** - Modern Java framework with built-in best practices
- **REST API** - Clean endpoints for game operations
- **Swagger UI** - Interactive API documentation at `/swagger-ui.html`
- **Layered Architecture** - Clean separation (Web → Domain → Datasource layers)
- **Dependency Injection** - Full Spring DI container management
- **Game Sessions** - Support for multiple concurrent games
- **AI Opponent** - Built-in AI logic for O player moves

## 📋 Getting Started

### Prerequisites

- Java 18+
- Gradle (wrapper included)
- Frontend running on `http://localhost:5173` (for development)

### Installation

```bash
cd backend
```

### Development

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

On Windows:
```bash
gradlew.bat bootRun
```

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
│   │   ├── mapper/             # DTO ↔ Entity mappers
│   │   └── model/              # Request/Response DTOs
│   ├── domain/                 # Business logic layer
│   │   ├── model/              # Domain entities (Game, GameMap)
│   │   ├── repository/         # Repository interfaces
│   │   └── service/            # Business logic (GameService)
│   ├── datasource/             # Data access layer
│   │   ├── mapper/             # Custom entity mappers
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Spring Data implementations
│   │   └── storage/            # In-memory storage
│   └── di/config/              # Spring @Configuration classes
├── resources/
│   └── application.properties  # Server and logging config
└── test/java/org/example/      # Unit & integration tests
```

## 🎮 How It Works

### Game Flow

1. **Create Game**: `POST /game?size=3` creates a new game session
2. **Player Move**: `POST /game/{id}` with game state containing your move (X)
3. **AI Response**: Backend processes the move and calculates AI move (O)
4. **Game Status**: Response includes updated board and game state (PLAYING/WIN/DRAW)

### API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/game?size=3` | Create new game (size parameter: 3 or higher) |
| `POST` | `/game/{id}` | Submit move and get AI response |

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
server.port=8080

# Context path
server.servlet.context-path=/

# Logging level
logging.level.org.example=DEBUG
logging.level.org.springframework=INFO
```

### Changing Port

In `application.properties`:
```properties
server.port=8081
```

Or via command line:
```bash
./gradlew bootRun --args='--server.port=8081'
```

## 📚 API Documentation

### Swagger UI

Interactive API documentation and testing:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification

Machine-readable API spec in JSON:

```
http://localhost:8080/v3/api-docs
```

Can be imported into Postman or other API clients.

## 🐳 Docker

### Build Image

```bash
docker build -t tic-tac-toe-backend:latest .
```

### Run Container

```bash
docker run -p 8080:8080 tic-tac-toe-backend:latest
```

### Using Docker Compose

From project root:

```bash
docker-compose up backend
```

The frontend will automatically connect to the API on `http://localhost:8080`

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests org.example.domain.service.GameServiceTest

# Run with detailed output
./gradlew test --info
```

Tests follow the same package structure as main code in `src/test/java/`

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

### API Documentation not loading

Ensure the application is running:

```bash
curl http://localhost:8080/swagger-ui.html
```

Check logs for startup errors:

```bash
./gradlew bootRun
```

### Frontend can't connect to API

Verify backend is running at `http://localhost:8080`:

```bash
curl -X POST http://localhost:8080/game?size=3
```

Check CORS and network configurations.

## 🛠️ Development

### Layered Architecture Pattern

- **web**: HTTP requests/responses, DTOs, validation
- **domain**: Pure business logic, game rules, AI strategy
- **datasource**: Data persistence abstraction
- **di**: Spring bean configuration

### Adding Features

1. Create data model in `domain.model`
2. Define service interface in `domain.repository`
3. Implement logic in `domain.service`
4. Add HTTP endpoint in `web.controller`
5. Write tests in `src/test/`

### Code Quality

- Follow Spring Boot conventions
- Use dependency injection
- Keep single responsibility principle
- Add JavaDoc for public methods
