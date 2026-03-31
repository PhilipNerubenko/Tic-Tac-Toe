# Tic-Tac-Toe

[![Java 18](https://img.shields.io/badge/Java-18-orange?style=flat-square)](https://www.java.com/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![React 19.2.0](https://img.shields.io/badge/React-19.2.0-61dafb?style=flat-square&logo=react)](https://react.dev/)
[![TypeScript 5.9.3](https://img.shields.io/badge/TypeScript-5.9.3-3178c6?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Vite 7.2.4](https://img.shields.io/badge/Vite-7.2.4-646CFF?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ed?style=flat-square&logo=docker)](https://www.docker.com/)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)

## Description

A full-featured web application for the classic Tic-Tac-Toe game with user authentication, player profiles, and modern multi-layer architecture. This project demonstrates best practices in development using Spring Boot for the backend and React for the frontend.

<p align="center">
  <img src="./assets/game_preview.png" width="600" alt="Tic-Tac-Toe Preview">
</p>

## Features

- ✅ **Spring Boot 3.3.4 Backend** — REST API with full OpenAPI/Swagger documentation
- ✅ **React 19.2.0 Frontend** — Modern reactive user interface
- ✅ **TypeScript 5.9.3** — Strict typing for reliable code
- ✅ **PostgreSQL 15** — Reliable storage for game and user data
- ✅ **Spring Security** — Basic authentication and registration
- ✅ **Docker Compose** — Simple one-step deployment (3 containers: DB, Backend, Frontend)
- ✅ **Layered Architecture** — Clean separation (Web, Domain, Datasource layers)
- ✅ **Dependency Injection** — Spring DI container management
- ✅ **API Documentation** — Interactive Swagger UI

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
│   │   ├── domain/            # Business logic (Auth, Game, User)
│   │   ├── datasource/        # Data access (JPA, PostgreSQL)
│   │   └── di/                # DI and Security configuration
│   ├── build.gradle.kts       # Gradle configuration
│   └── Dockerfile             # Docker image for backend
│
├── frontend/                   # React application
│   ├── src/
│   │   ├── App.tsx           # Main component
│   │   ├── components/       # UI components (Login, Register, Game, Profile)
│   │   ├── contexts/         # React Context (AuthContext)
│   │   ├── hooks/            # Custom React hooks (useGame)
│   │   └── interfaces/       # TypeScript interfaces
│   ├── package.json          # npm dependencies
│   ├── vite.config.ts        # Vite configuration
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

- **URL:** [http://localhost:3000](http://localhost:3000) (Docker Compose)
- **URL:** [http://localhost:5173](http://localhost:5173) (local development)

### ⚙️ Developer Tools (Backend)

The backend runs as a **Headless API** — the root path is not intended for direct access. Use the following resources:

| Resource | Link | Description |
| --- | --- | --- |
| **Swagger UI** | [🔗 Open Documentation](http://localhost:8080/swagger-ui.html) | Interactive API documentation. |
| **OpenAPI (JSON)** | [📄 Specification](http://localhost:8080/v3/api-docs) | JSON specification for client generation or Postman import. |

### 📡 API Request Examples

```bash
# Register a new user
curl -X POST "http://localhost:8080/auth/signup" \
     -H "Content-Type: application/json" \
     -d '{"username": "player1", "password": "secret123"}'

# Login
curl -X POST "http://localhost:8080/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "player1", "password": "secret123"}'

# Create a new game (POST /game?size=3)
curl -s -X POST "http://localhost:8080/game?size=3" \
     -u "player1:secret123"

# Get game status
curl "http://localhost:8080/game/{id}" \
     -u "player1:secret123"
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
DB_NAME=game_sessions_storage
DB_USER=postgres
DB_PASSWORD=postgres
DB_INTERNAL_PORT=5432
DB_EXTERNAL_PORT=5432
BACKEND_INTERNAL_PORT=8080
BACKEND_EXTERNAL_PORT=8080
FRONTEND_EXTERNAL_PORT=3000
```

- **Start the application**

```bash
docker-compose up -d
```

- **Open the application**

- Frontend: [http://localhost:3000](http://localhost:3000)
- Backend API: [http://localhost:8080](http://localhost:8080)
- API Documentation: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

- **Stop the application**

```bash
docker-compose down
```

### Option 2 — Local Development

#### PostgreSQL

```bash
docker run -d --name postgres-db \
  -e POSTGRES_DB=game_sessions_storage \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

#### Backend (Spring Boot)

- **Navigate to backend directory**

```bash
cd backend
```

- **Set environment variables**

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/game_sessions_storage
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
```

- **Build the jar file**

```bash
./gradlew build
```

- **Run the application**

```bash
./gradlew bootRun
```

Backend will be available at [http://localhost:8080](http://localhost:8080)

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

Frontend will be available at [http://localhost:5173](http://localhost:5173) (Vite dev server)

## Available Commands

### Backend (Gradle)

| Command | Description |
| --- | --- |
| `./gradlew build` | Build the project |
| `./gradlew bootRun` | Run the application |
| `./gradlew test` | Run tests |
| `./gradlew clean` | Clean build artifacts |

### Frontend (npm)

| Command | Description |
| --- | --- |
| `npm run dev` | Development with hot reload |
| `npm run build` | Production build |
| `npm run lint` | Code check with ESLint |
| `npm run format` | Code formatting with Prettier |
| `npm run preview` | Preview production build |

## API Documentation

### About the Backend

The backend is a **Headless REST API** built on Spring Boot 3. It provides full game logic and authentication functionality through HTTP endpoints.

### Main Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/auth/signup` | Register a new user |
| `POST` | `/auth/login` | Login (returns session cookie) |
| `GET` | `/user/profile` | Current user profile |
| `POST` | `/game?size=3` | Create a new game |
| `POST` | `/game/{id}` | Make a move |
| `GET` | `/game/{id}` | Get game status |
| `GET` | `/game` | List all user's games |

### Available Resources

After starting the backend, full API documentation is available in Swagger UI:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

You can test all API endpoints directly from the Swagger interface!

## Architecture

### Backend Architecture (Layers)

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

- **Web Layer**: REST controllers, DTO models, mappers, authentication filters
- **Domain Layer**: Business logic, entity models, repository interfaces, services
- **Datasource Layer**: JPA repositories, entity mappers, PostgreSQL
- **DI Configuration**: Spring bean configuration, Security configuration

### Frontend Architecture

- **React Components**: Functional components (LoginForm, RegisterForm, GameModeSelection, UserProfile)
- **Context API**: AuthContext for authentication state management
- **Custom Hooks**: Reusable logic (useGame)
- **TypeScript Interfaces**: Strict typing
- **Vite**: Fast development and optimized builds

## Dependencies

### Backend

- **Java 18**
- **Spring Boot 3.3.4**
- **SpringDoc OpenAPI 2.6.0**
- **Spring Security** — Authentication and basic auth
- **Spring Data JPA** — Database access
- **PostgreSQL** — Primary database
- **H2** — Test database

### Frontend

- **React 19.2.0**
- **TypeScript 5.9.3**
- **Vite 7.2.4**
- **ESLint** — Code quality
- **Prettier** — Code formatting
