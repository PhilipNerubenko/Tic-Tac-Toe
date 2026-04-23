# Крестики-нолики — Backend

REST API бэкенд на Spring Boot 3.3.4 для игры «Крестики-нолики» с JWT Bearer-аутентификацией, слоистой архитектурой, PostgreSQL и расширенными игровыми функциями.

## 🚀 Возможности

-   **Spring Boot 3.3.4** — современный Java-фреймворк с лучшими практиками
-   **JWT Bearer Token Authentication** — Stateless-аутентификация с access и refresh токенами (HS256)
-   **Spring Security** — Ролевой контроль доступа (роль `USER`) и method-level security
-   **PostgreSQL** — Надёжное хранение данных пользователей и игровых сессий
-   **Spring Data JPA** — Удобная абстракция для работы с данными через Hibernate
-   **Swagger UI** — Интерактивная документация API по `/swagger-ui.html` с Bearer-аутентификацией
-   **Слоистая архитектура** — Чёткое разделение (Web → Domain → Datasource)
-   **Dependency Injection** — Полное управление зависимостями через Spring DI
-   **Игровые функции** — ИИ-противник (алгоритм Минимакса), PvP мультиплеер, история игр, таблица лидеров
-   **Stateless дизайн** — Без HTTP сессий; всё состояние в JWT токенах

## 📋 Начало работы

### Предварительные требования

-   Java 18+
-   PostgreSQL 15+ (или используйте Docker Compose)
-   Gradle (обёртка включена в проект)

### Установка

1.  **Клонируйте репозиторий** (если вы ещё этого не делали):

    ```bash
    git clone <repository-url>
    cd backend
    ```

2.  **Настройте переменные окружения**

    Backend требует следующие переменные окружения. Рекомендуется создать файл `.env` в директории `backend`:

    ```bash
    cp .env.example .env
    ```

    Затем отредактируйте `.env` со своими значениями:

    | Переменная                     | Описание                       | Пример |
    | ---------------------------- | --------------------------------- | ------- |
    | `SPRING_DATASOURCE_URL`      | URL подключения к PostgreSQL         | `jdbc:postgresql://localhost:5433/game_sessions_storage` |
    | `SPRING_DATASOURCE_USERNAME` | Имя пользователя БД                 | `postgres` |
    | `SPRING_DATASOURCE_PASSWORD` | Пароль БД                 | `secure_password` |
    | `JWT_SECRET`                 | Секрет подписи JWT (мин. 256-bit HS256) | `base64-secret-key` |

    **Безопасность:** Никогда не указывайте эти значения прямо в `application.properties`. Используйте переменные окружения или Docker secrets. `JWT_SECRET` должен быть минимум 32 случайных байта для HS256. Сгенерировать: `openssl rand -base64 32`.

3.  **Запустите PostgreSQL** (если не используете Docker Compose):

    ```bash
    docker run -d --name postgres-db \
      -e POSTGRES_DB=game_sessions_storage \
      -e POSTGRES_USER=postgres \
      -e POSTGRES_PASSWORD=postgres \
      -p 5433:5432 \
      postgres:15-alpine
    ```

4.  **Запустите приложение**

    ```bash
    ./gradlew bootRun
    ```

    API будет доступен по адресу `http://localhost:8081`

    В Windows:
    ```bash
    gradlew.bat bootRun
    ```

### Сводка переменных окружения

| Переменная                     | Обязательная | Описание | Значение по умолчанию |
| ---------------------------- | ------------ | ----------- | -------------------- |
| `SPRING_DATASOURCE_URL`      | Да           | JDBC URL PostgreSQL | — |
| `SPRING_DATASOURCE_USERNAME` | Да           | Имя пользователя БД | — |
| `SPRING_DATASOURCE_PASSWORD` | Да           | Пароль БД | — |
| `JWT_SECRET`                 | Да           | Секрет подписи HS256 (мин. 256 бит) | — |
| `jwt.access-token-validity`  | Нет          | TTL access-токена в секундах | `3600` (1 час) |
| `jwt.refresh-token-validity` | Нет          | TTL refresh-токена в секундах | `604800` (7 дней) |

## 📦 Доступные команды

```bash
# Запуск приложения
./gradlew bootRun

# Сборка проекта
./gradlew build

# Запуск тестов (H2 in-memory БД)
./gradlew test

# Очистка артефактов сборки
./gradlew clean

# Подробный лог сборки
./gradlew build --stacktrace

# Генерация отчёта о покрытии тестами (HTML)
./gradlew jacocoTestReport  # Открыть: build/reports/jacoco/test/html/index.html
```

## 🏗️ Структура проекта

```
src/
├── main/java/org/example/
│   ├── Main.java               # Точка входа приложения (SpringBootApplication)
│   ├── web/                    # Слой REST контроллеров
│   │   ├── controller/         # @RestController эндпоинты
│   │   │   ├── AuthController  # /auth/* (signup, signin, refresh, me)
│   │   │   ├── GameController  # /game endpoints (create, move, join, history, leaderboard)
│   │   │   └── GlobalExceptionHandler  # Централизованная обработка ошибок
│   │   ├── filter/             # AuthFilter (валидация JWT Bearer токенов)
│   │   ├── mapper/             # DTO ↔ Entity конвертеры
│   │   └── model/              # Request/Response DTOs (JwtRequest, JwtResponse, GameSessionDTO и др.)
│   ├── domain/                 # Слой бизнес-логики
│   │   ├── model/              # Доменные сущности (Game, GameMap, User, JwtAuthentication)
│   │   ├── repository/         # Интерфейсы репозиториев (GameRepository, UserRepository)
│   │   ├── service/            # Бизнес-логика
│   │   │   ├── AuthService     # Генерация, валидация JWT; обновление токенов
│   │   │   ├── GameService     # Правила игры, ходы ИИ (Minimax), история, таблица лидеров
│   │   │   └── UserService     # CRUD пользователей и профили
│   │   └── exception/          # Пользовательские исключения (DuplicateUserException и др.)
│   ├── datasource/             # Слой доступа к данным
│   │   ├── mapper/             # Кастомные мапперы сущностей
│   │   ├── model/              # JPA-сущности (UserEntity, GameSessionEntity, GameMapEntity и др.)
│   │   └── repository/         # Реализации Spring Data JPA (JpaGameRepository и др.)
│   └── di/config/              # Spring @Configuration классы
│       ├── GameConfig          # Конфигурация игровых бинов (AI стратегия, мапперы)
│       └── SecurityConfig      # Настройка Spring Security + JWT Bearer
├── resources/
│   └── application.properties  # Конфигурация сервера, БД, JWT
└── test/java/org/example/      # Unit и интеграционные тесты (H2 БД)
```

## 🎮 Как это работает

### Процесс аутентификации (JWT Bearer Tokens)

#### 1. Регистрация (`POST /auth/signup`)

**Запрос:**
```json
{
  "login": "player1",
  "password": "secret123"
}
```

**Ответ (201 Created):**
```json
{
  "type": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Пароль хешируется BCrypt; пользователь создаётся; генерируются JWT токены.

#### 2. Вход (`POST /auth/signin`)

**Запрос:**
```json
{
  "login": "player1",
  "password": "secret123"
}
```

**Ответ (200 OK):**
```json
{
  "type": "Bearer",
  "accessToken": "...",
  "refreshToken": "..."
}
```

Учётные данные проверяются; возвращаются токены.

#### 3. Доступ к защищённым ресурсам

Включайте access-токен в заголовок `Authorization`:

```
Authorization: Bearer <access-token>
```

Все эндпоинты `/game/**` и `/auth/me` требуют аутентификации.

#### 4. Обновление токенов

**Обновить access-токен:**
```bash
curl -X POST "http://localhost:8081/auth/refresh/access" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "current-refresh-token"}'
```

**Ротация refresh-токена:**
```bash
curl -X POST "http://localhost:8081/auth/refresh/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "current-refresh-token"}'
```

Оба возвращают:
```json
{
  "type": "Bearer",
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token"
}
```

Ротация: каждый вызов делает предыдущий refresh-токен невалидным.

### Игровой процесс

#### Создать игру (`POST /game?size=3&vsAi=true`)

Создаёт новую игровую сессию. По умолчанию (`vsAi=true`) игра против ИИ (O). Установите `vsAi=false` для PvP.

**Ответ (201):**
```json
{
  "id": "uuid",
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

Значения ячеек: `0 = EMPTY`, `1 = CROSS (X)`, `2 = ZERO (O)`.

#### Сделать ход (`POST /game/{id}/move`)

Отправьте свой ход; backend автоматически отвечает ходом ИИ (если `vsAi=true`).

**Запрос:**
```json
{
  "gameMap": {
    "map": [[1,0,0],[0,0,0],[0,0,0]],
    "size": 3
  }
}
```

**Ответ (200):** Обновлённое состояние игры включает ход ИИ.

#### Присоединиться к игре (PvP) (`POST /game/{id}/join?guestId=<uuid>`)

Второй игрок присоединяется к существующей ожидающей игре.

#### Проверить покинул ли соперник (`POST /game/{id}/check-opponent-left?timeoutSeconds=30`)

Ручная проверка, покинул ли соперник игру (для polling).

#### Получить статус игры (`GET /game/{id}`)

Возвращает текущее поле, статус, игроков и временные метки.

#### Активные игры (`GET /game/active`)

Список всех игр, ожидающих второго игрока (статус `WAITING_FOR_PLAYERS`).

#### История игр (`GET /game/history`)

Возвращает все завершённые игры (статус `VICTORY` или `DRAW`) текущего пользователя.

#### Таблица лидеров (`GET /game/leaderboard?n=10`)

Возвращает топ N игроков по проценту побед.

**Ответ:**
```json
[
  {
    "userId": "uuid",
    "login": "player1",
    "winRate": 0.85
  }
]
```

### Сводка API эндпоинтов

#### Аутентификация (публичные где отмечено)

| Метод | Путь | Описание | Требуется авторизация |
| ------ | ---- | ----------- | -------------------- |
| `POST` | `/auth/signup` | Регистрация нового пользователя | Нет |
| `POST` | `/auth/signin` | Вход с учётными данными | Нет |
| `POST` | `/auth/refresh/access` | Обновить access-токен | Нет (refresh-токен) |
| `POST` | `/auth/refresh/refresh` | Ротация refresh-токена | Нет (refresh-токен) |
| `GET`  | `/auth/me` | Получить информацию о текущем пользователе | Да |
| `GET`  | `/auth/{id}` | Получить пользователя по ID (self или ADMIN) | Да |

#### Игра (все требуют Bearer-аутентификацию)

| Метод | Путь | Описание |
| ------ | ---- | ----------- |
| `POST` | `/game` | Создать новую игру (`?size=3&vsAi=true`) |
| `POST` | `/game/{id}/move` | Сделать ход |
| `GET`  | `/game/{id}` | Получить состояние игры |
| `GET`  | `/game/active` | Список доступных (ожидающих) игр |
| `POST` | `/game/{id}/join` | Присоединиться как второй игрок (PvP) |
| `POST` | `/game/{id}/check-opponent-left` | Проверить покинул ли соперник игру |
| `GET`  | `/game/history` | Получить завершённые игры пользователя |
| `GET`  | `/game/leaderboard?n=10` | Топ игроков по проценту побед |

#### Публичные эндпоинты

- `GET /` → Редирект на Swagger UI
- `GET /swagger-ui.html` → Интерфейс Swagger UI
- `GET /v3/api-docs` → OpenAPI JSON спецификация

## 🔧 Конфигурация

### Application Properties

Расположение: `src/main/resources/application.properties`

```properties
server.error.include-message=always
spring.application.name=tic-tac-toe-backend

# База данных (через переменные окружения)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# JWT (через переменные окружения)
jwt.secret=${JWT_SECRET}
jwt.access-token-validity=3600
jwt.refresh-token-validity=604800
```

### Изменение порта сервера

В `application.properties`:
```properties
server.port=8082
```

Или через командную строку:
```bash
./gradlew bootRun --args='--server.port=8082'
```

## 📚 Документация API

### Swagger UI

Интерактивная документация API и тестирование:

```
http://localhost:8081/swagger-ui.html
```

**Аутентификация в Swagger:**
1. Вызовите `POST /auth/signin` через Swagger для получения токенов
2. Нажмите "Authorize" (вверху справа)
3. Введите `Bearer <your-access-token>`
4. Защищённые эндпоинты становятся доступными для тестирования

### Спецификация OpenAPI

Машиночитаемая спецификация API в JSON:

```
http://localhost:8081/v3/api-docs
```

Может быть импортирована в Postman или другие API-клиенты.

## 🐳 Docker

### Сборка образа

```bash
docker build -t tic-tac-toe-backend:latest .
```

### Запуск контейнера

```bash
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/game_sessions_storage \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=your_jwt_secret_here \
  tic-tac-toe-backend:latest
```

> **Примечание:** На macOS/Windows с Docker Desktop используйте `host.docker.internal` для доступа к PostgreSQL на хосте. С Docker Compose используйте имя сервиса `db` (как в `docker-compose.yml`).

### Использование Docker Compose (рекомендуется)

Из корня проекта:
```bash
docker compose up backend-api
```

Фронтенд автоматически подключится к API по адресу `http://localhost:8081` через Docker сеть (хост `backend-api` внутри compose сети).

## Production-развертывание

Для production-среды:

1. **JWT Secret:** Сгенерируйте надёжный случайный secret (32+ байта) и храните безопасно (Docker secrets, Kubernetes secrets, менеджер переменных окружения). Никогда не используйте development secret.
2. **База данных:** Используйте управляемый PostgreSQL с persistent volumes, регулярным backup и connection pooling (PgBouncer рекомендуется).
3. **Reverse Proxy:** Настройте Nginx/Traefik для SSL termination, HTTP/2 и раздачи статики.
4. **CORS:** Добавьте whitelist фронтенд origins в `SecurityConfig.java` (`.cors(cors -> cors.configurationSource(...))`).
5. **Оптимизация сборки:**
   ```bash
   ./gradlew build -x test    # Пропустить тесты для быстрой сборки
   ```
   Артефакт: `backend/build/libs/tic-tac-toe-backend-1.0-SNAPSHOT.jar`.
6. **Мониторинг:** Включите Spring Boot Actuator (`spring-boot-starter-actuator`) для health checks, метрик и логирования.
7. **Логирование:** Настройте structured logging (JSON) и централизованный сбор (ELK, Loki).

### Пример production `application-prod.properties`

```properties
server.port=8080
spring.jpa.hibernate.ddl-auto=validate  # Или 'none'; использовать миграции
spring.jpa.show-sql=false
logging.level.org.example=INFO
```

Активировать: `-Dspring.profiles.active=prod`.

## 🧪 Тестирование

### Unit и интеграционные тесты

```bash
# Запуск всех тестов (H2 in-memory БД)
./gradlew test

# Запуск конкретного тестового класса
./gradlew test --tests org.example.domain.service.GameServiceTest

# Подробный вывод
./gradlew test --info

# Генерация отчёта о покрытии (HTML)
./gradlew jacocoTestReport  # Открыть: build/reports/jacoco/test/html/index.html
```

### Структура тестов

- `src/test/java/org/example/domain/service/` — Service unit-тесты (логика игры, auth)
- `src/test/java/org/example/web/controller/` — Интеграционные тесты контроллеров (MockMvc)
- `src/test/java/org/example/datasource/` — Интеграционные тесты репозиториев
- H2 БД настроена для изоляции; внешних зависимостей нет

## 🔍 Устранение неполадок

### Ошибка сборки

```bash
# Очистка и пересборка
./gradlew clean build

# Подробные ошибки
./gradlew build --stacktrace --info
```

### Порт 8081 уже занят

Измените порт в `application.properties` или передайте аргумент:
```bash
./gradlew bootRun --args='--server.port=8082'
```

### Ошибка подключения к базе данных

- Убедитесь, что PostgreSQL запущен: `docker ps` или `pg_isready -p 5433`
- Проверьте переменные окружения: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- Проверьте формат URL: `jdbc:postgresql://host:port/dbname`
- Для Docker контейнеров убедитесь в сетевой связности между `backend-api` и `db` сервисами

### JWT Secret не задан

Приложение не запустится с ошибкой:
```
IllegalArgumentException: JWT secret is required
```

Установите переменную окружения `JWT_SECRET` (см. .env.example).

### API возвращает 401 Unauthorized

- Убедитесь, что заголовок `Authorization: Bearer <token>` присутствует
- Проверьте срок действия токена (access токен живёт 1 час)
- Обновите токен через `/auth/refresh/access` при истечении access-токена
- Проверьте структуру токена в запросе (лишние пробелы, правильный префикс "Bearer ")

### Swagger UI не загружается

Убедитесь, что приложение запущено: `curl http://localhost:8081/swagger-ui.html`

Проверьте логи запуска: `./gradlew bootRun`

### Фронтенд не может подключиться к API

Для локальной разработки убедитесь, что Vite dev-сервер запущен и proxy настроен на `http://localhost:8081`. Проверьте консоль браузера на CORS или сетевые ошибки.

Проверьте доступность backend:
```bash
curl -X POST http://localhost:8081/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"login":"test","password":"test"}'
```

## 🛠️ Разработка

### Паттерн слоистой архитектуры

-   **web**: HTTP запросы/ответы, DTOs, валидация, JWT фильтр аутентификации
-   **domain**: Чистая бизнес-логика, правила игры, алгоритм Minimax, сервисы auth и game
-   **datasource**: Хранение данных через JPA/Hibernate + PostgreSQL
-   **di**: Spring конфигурация бинов, JWT провайдер, конфигурация безопасности

### Добавление новых функций

1. Создайте доменную модель в `domain.model`
2. Определите интерфейс репозитория в `domain.repository`
3. Реализуйте бизнес-логику в `domain.service`
4. Добавьте HTTP эндпоинт в `web.controller`
5. Создайте JPA-сущность в `datasource.model`
6. Реализуйте репозиторий в `datasource.repository`
7. Напишите unit-тесты для сервисов и интеграционные тесты для контроллеров
8. Обновите OpenAPI аннотации для Swagger

### Стандарты качества кода

-   Следуйте соглашениям Spring Boot и используйте внедрение зависимостей
-   Соблюдайте принцип единственной ответственности (SRP) для каждого класса
-   Используйте DTOs для API границ; никогда не экспонируйте entity напрямую
-   Добавляйте JavaDoc для публичных методов и сложной логики
-   Пишите unit-тесты для сервисов (бизнес-логика) и интеграционные тесты для контроллеров
-   Используйте `@Validated` и `@Valid` для валидации входных данных
-   Обрабатывайте исключения через `@ControllerAdvice` (GlobalExceptionHandler)

### Примечания по безопасности JWT

-   Токены подписываются HS256 с использованием `JWT_SECRET`
-   Клеймы access-токена: `sub` (UUID пользователя), `roles` (массив), `iat`, `exp`, `iss`
-   Токены stateless; без блокировок или persistance
-   Refresh токены ротируются при каждом использовании; старые становятся невалидными
-   PasswordEncoder: BCrypt с дефолтной strength (10 раундов)

## 📖 Ключевые детали реализации

### Flow валидации токена (AuthFilter.java)

1. Извлечь заголовок `Authorization` → проверить префикс `Bearer `
2. Отделить префикс, извлечь токен
3. Вызвать `JwtProvider.validateAccessToken(token)` — проверка подписи, срока, issuer
4. Распарсить claims: `sub` → UUID пользователя, `roles` → authorities
5. Создать объект `JwtAuthentication` (реализует `Authentication`)
6. Установить в `SecurityContextHolder.getContext().setAuthentication(auth)`
7. Продолжить цепочку фильтров; при невалидности → 401 сразу

### ИИ для игры (Minimax)

ИИ (`MinimaxAiStrategy`) использует алгоритм Минимакса с альфа-бета отсечением для выбора оптимальных ходов. Детерминирован и всегда играет оптимально на поле 3x3; для больших полей используется эвристика с ограниченной глубиной.

### Расчёт таблицы лидеров

Таблица лидеров (`GameService.getLeaderboard(limit)`) запрашивает статистику всех пользователей и сортирует по проценту побед (wins / total games). Учитываются только пользователи с ≥1 завершённой игрой.

### Хранение истории игр

История игр хранит только **завершённые** игры (статус `VICTORY` или `DRAW`). Активные/незавершённые игры не включаются. История получается через `GameService.getGameHistory(userId)`.

## 📄 Лицензия

Обучающий проект.
