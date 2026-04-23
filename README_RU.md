# Крестики-нолики (Tic-Tac-Toe)

[![Java 18](https://img.shields.io/badge/Java-18-orange?style=flat-square)](https://www.java.com/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![React 19.2.0](https://img.shields.io/badge/React-19.2.0-61dafb?style=flat-square&logo=react)](https://react.dev/)
[![TypeScript 5.9.3](https://img.shields.io/badge/TypeScript-5.9.3-3178c6?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Vite 7.2.4](https://img.shields.io/badge/Vite-7.2.4-646CFF?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ed?style=flat-square&logo=docker)](https://www.docker.com/)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)

## Описание

Полнофункциональное веб-приложение классической игры «Крестики-нолики» с JWT-аутентификацией, профилями игроков, таблицей лидеров, историей игр и современной многоуровневой архитектурой. Проект демонстрирует лучшие практики разработки с использованием Spring Boot для бэкенда и React для фронтенда.

<p align="center">
  <img src="./assets/game_preview.png" width="600" alt="Tic-Tac-Toe Preview">
</p>

## Особенности

- ✅ **Spring Boot 3.3.4 Backend** — REST API с полной документацией OpenAPI/Swagger
- ✅ **JWT Bearer Token Authentication** — Безопасная stateless-аутентификация с ротацией refresh-токенов
- ✅ **React 19.2.0 Frontend** — Современный реактивный интерфейс с автоматическим обновлением токенов
- ✅ **TypeScript 5.9.3** — Строгая типизация для надёжного кода
- ✅ **PostgreSQL 15** — Надёжное хранение данных игр и пользователей
- ✅ **Docker Compose** — Простое одноэтапное развертывание (3 контейнера: БД, Backend, Frontend)
- ✅ **Слоистая архитектура** — Чёткое разделение (Web, Domain, Datasource слои)
- ✅ **Dependency Injection** — Управление зависимостями через Spring DI
- ✅ **Игровые возможности** — ИИ-противник, мультиплеер (PvP), таблица лидеров, история игр
- ✅ **API документация** — Интерактивная Swagger UI с поддержкой Bearer-аутентификации

## Скриншоты

### Форма входа

<p align="center">
  <img src="./assets/form_for_login.png" width="600" alt="Форма входа">
</p>

### Форма регистрации

<p align="center">
  <img src="./assets/form_for_register.png" width="600" alt="Форма регистрации">
</p>

### Меню выбора режима игры

<p align="center">
  <img src="./assets/game_mode_menu.png" width="600" alt="Меню выбора режима игры">
</p>

## Структура проекта

```text
Tic-Tac-Toe/
├── backend/                    # Spring Boot приложение
│   ├── src/main/java/org/example/
│   │   ├── Main.java          # Точка входа приложения
│   │   ├── web/               # REST контроллеры (Auth, Game)
│   │   │   ├── controller/    # @RestController эндпоинты
│   │   │   ├── filter/        # JWT фильтр аутентификации
│   │   │   ├── mapper/        # DTO ↔ Entity конвертеры
│   │   │   └── model/         # Request/Response DTOs
│   │   ├── domain/            # Бизнес-логика (Auth, Game, User)
│   │   │   ├── model/         # Доменные сущности
│   │   │   ├── repository/    # Интерфейсы репозиториев
│   │   │   ├── service/       # Сервисы бизнес-логики
│   │   │   └── exception/     # Пользовательские исключения
│   │   ├── datasource/        # Слой доступа к данным (JPA, PostgreSQL)
│   │   │   ├── model/         # JPA сущности
│   │   │   └── repository/    # Реализации JPA
│   │   └── di/config/         # DI и конфигурация безопасности
│   │       ├── GameConfig     # Конфигурация игровых бинов
│   │       └── SecurityConfig # Настройка JWT + Spring Security
│   ├── build.gradle.kts       # Gradle конфигурация
│   └── Dockerfile             # Docker образ для backend
│
├── frontend/                   # React приложение
│   ├── src/
│   │   ├── App.tsx           # Главный компонент
│   │   ├── components/       # UI компоненты (Login, Register, Game, Profile)
│   │   ├── contexts/         # React Context (AuthContext)
│   │   ├── hooks/            # Пользовательские хуки (useGame)
│   │   ├── interfaces/       # TypeScript интерфейсы
│   │   ├── utils/            # Утилиты API с автообновлением токенов
│   │   └── constants.ts      # Ключи хранилища и константы
│   ├── package.json          # npm зависимости
│   ├── vite.config.ts        # Vite конфигурация с proxy
│   └── Dockerfile            # Docker образ для frontend
│
└── docker-compose.yml        # Оркестрация контейнеров (БД + Backend + Frontend)
```

## Предварительные требования

### Для локальной разработки

- **Java 18+** ([установка](https://www.oracle.com/java/technologies/downloads/))
- **Node.js 20+** и npm ([установка](https://nodejs.org/))
- **PostgreSQL 15+** (или используйте Docker Compose)
- **Gradle** (встроен через Gradle Wrapper)

### Для развертывания с Docker

- **Docker** 20.10+
- **Docker Compose** 2.0+

## 🚀 Как пользоваться приложением

После запуска контейнеров или локальных серверов проект доступен по следующим адресам:

### 🖥 Пользовательский интерфейс (Frontend)

Основная игровая панель, где происходит вся магия игры.

- **URL:** [http://localhost:3001](http://localhost:3001) (Docker Compose)
- **URL:** [http://localhost:5173](http://localhost:5173) (локальная разработка)

### ⚙️ Инструменты разработчика (Backend)

Backend работает как **headless REST API**. Используйте следующие ресурсы:

| Ресурс | Ссылка | Описание |
| --- | --- | --- |
| **Swagger UI** | [🔗 Открыть документацию](http://localhost:8081/swagger-ui.html) | Интерактивная документация API (аутентификация через Bearer token) |
| **OpenAPI (JSON)** | [📄 Спецификация](http://localhost:8081/v3/api-docs) | Машиночитаемая спецификация API для генерации клиентов |

## 🔐 Аутентификация

Приложение использует **JWT (JSON Web Token) Bearer-аутентификацию**. После регистрации или входа вы получаете access-токен (срок 1 час) и refresh-токен (срок 7 дней). Включайте access-токен в заголовке `Authorization` всех защищённых запросов:

```
Authorization: Bearer <your-access-token>
```

### Жизненный цикл токенов

1. **Регистрация** → `POST /auth/signup` с `{ "login": "user", "password": "pass" }` → Возвращаются `accessToken` + `refreshToken`
2. **Вход** → `POST /auth/signin` с учётными данными → Возвращаются токены
3. **Доступ к защищённым эндпоинтам** → Заголовок `Authorization: Bearer <token>`
4. **Обновление токенов** → `POST /auth/refresh/access` или `/auth/refresh/refresh` при истечении access-токена

### Управление токенами на фронтенде

Фронтенд автоматически хранит токены (localStorage) и обрабатывает их обновление. При получении 401 от API происходит прозрачная попытка обновить токен и повторить запрос.

## 📡 Примеры взаимодействия с API

```bash
# Регистрация нового пользователя
curl -X POST "http://localhost:8081/auth/signup" \
     -H "Content-Type: application/json" \
     -d '{"login": "player1", "password": "secret123"}'

# Вход (JWT signin)
curl -X POST "http://localhost:8081/auth/signin" \
     -H "Content-Type: application/json" \
     -d '{"login": "player1", "password": "secret123"}'

# Ответ: { "type": "Bearer", "accessToken": "...", "refreshToken": "..." }

# Создать новую игру (против ИИ, 3x3 по умолчанию)
curl -X POST "http://localhost:8081/game?size=3&vsAi=true" \
     -H "Authorization: Bearer <your-access-token>"

# Получить статус игры
curl "http://localhost:8081/game/{id}" \
     -H "Authorization: Bearer <your-access-token>"

# Сделать ход
curl -X POST "http://localhost:8081/game/{id}/move" \
     -H "Authorization: Bearer <your-access-token>" \
     -H "Content-Type: application/json" \
     -d '{"gameMap": {"map": [[1,0,0],[0,0,0],[0,0,0]], "size": 3}}'

# Получить профиль пользователя
curl "http://localhost:8081/auth/me" \
     -H "Authorization: Bearer <your-access-token>"

# Получить историю игр
curl "http://localhost:8081/game/history" \
     -H "Authorization: Bearer <your-access-token>"

# Получить таблицу лидеров (топ 10)
curl "http://localhost:8081/game/leaderboard?n=10" \
     -H "Authorization: Bearer <your-access-token>"

# Присоединиться к активной игре (PvP)
curl -X POST "http://localhost:8081/game/{sessionId}/join?guestId=<your-uuid>" \
     -H "Authorization: Bearer <your-access-token>"

# Проверить, покинул ли соперник игру
curl -X POST "http://localhost:8081/game/{id}/check-opponent-left?timeoutSeconds=30" \
     -H "Authorization: Bearer <your-access-token>"

# Обновить access-токен
curl -X POST "http://localhost:8081/auth/refresh/access" \
     -H "Content-Type: application/json" \
     -d '{"refreshToken": "<your-refresh-token>"}'
```

## 🔧 Установка и запуск

### Вариант 1 — Развертывание с Docker Compose (рекомендуется)

- **Клонируйте репозиторий**

```bash
git clone <repository-url>
cd Tic-Tac-Toe
```

- **Создайте файл .env** (если отсутствует)

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

# JWT Secret (сгенерировать: openssl rand -base64 32)
JWT_SECRET=your_jwt_secret_key_min_32_bytes_base64_encoded
```

> **Важно:** Для production сгенерируйте надёжный JWT secret. Используйте `openssl rand -base64 32` или менеджер паролей. Secret должен быть минимум 256 бит (32 байта) для HS256.

- **Запустите приложение**

```bash
docker compose up --build -d
```

- **Откройте приложение**

- Frontend: [http://localhost:3001](http://localhost:3001)
- Backend API: [http://localhost:8081](http://localhost:8081)
- API документация: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

- **Остановка приложения**

```bash
docker compose down
```

### Вариант 2 — Локальная разработка

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

- **Перейдите в директорию backend**

```bash
cd backend
```

- **Установите переменные окружения**

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/game_sessions_storage
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=$(openssl rand -base64 32)
```

- **Сборка и запуск**

```bash
./gradlew build
./gradlew bootRun
```

Backend будет доступен по адресу [http://localhost:8081](http://localhost:8081)

В Windows:
```bash
gradlew.bat bootRun
```

#### Frontend (React)

- **Перейдите в директорию frontend**

```bash
cd frontend
```

- **Установите зависимости**

```bash
npm install
```

- **Запустите dev-сервер**

```bash
npm run dev
```

Frontend будет доступен по адресу [http://localhost:5173](http://localhost:5173) (Vite dev-сервер). Dev-сервер проксирует запросы `/auth` и `/game` на `http://localhost:8081`.

## Доступные команды

### Backend (Gradle)

| Команда | Описание |
| --- | --- |
| `./gradlew build` | Сборка проекта |
| `./gradlew bootRun` | Запуск приложения |
| `./gradlew test` | Запуск тестов (H2 in-memory БД) |
| `./gradlew clean` | Очистка артефактов сборки |

### Frontend (npm)

| Команда | Описание |
| --- | --- |
| `npm run dev` | Разработка с горячей перезагрузкой и proxy |
| `npm run build` | Production сборка |
| `npm run lint` | Проверка кода с ESLint |
| `npm run format` | Форматирование кода через Prettier |
| `npm run preview` | Просмотр production сборки |

## 📚 Документация API

### О Backend

Backend — это **headless REST API** на Spring Boot 3 с JWT-аутентификацией. Он обеспечивает полную игровую логику, управление пользователями и статистику через HTTP endpoints.

### Основные эндпоинты

#### Аутентификация

| Метод | Эндпоинт | Описание | Требуется авторизация |
| ------ | -------------- | ------------------- | ------------- |
| `POST` | `/auth/signup` | Регистрация нового пользователя | Нет |
| `POST` | `/auth/signin` | Вход (возвращает JWT токены) | Нет |
| `POST` | `/auth/refresh/access` | Обновить access-токен | Нет (требуется refresh-токен) |
| `POST` | `/auth/refresh/refresh` | Обновить refresh-токен (ротация) | Нет (требуется refresh-токен) |
| `GET` | `/auth/me` | Получить профиль текущего пользователя | Да (Bearer) |

#### Пользователь

| Метод | Эндпоинт | Описание | Требуется авторизация |
| ------ | ------------- | ---------------------- | ------------- |
| `GET`  | `/auth/{id}` | Получить пользователя по ID (свой или admin) | Да (Bearer) |

#### Игра

| Метод | Эндпоинт | Описание | Требуется авторизация |
| ------ | ----------- | ---------------------------------------------- | ------------- |
| `POST` | `/game?size=3&vsAi=true` | Создать новую игровую сессию (против ИИ или PvP) | Да (Bearer) |
| `POST` | `/game/{id}/move` | Отправить ход (X), ИИ отвечает автоматически (O) | Да (Bearer) |
| `GET`  | `/game/{id}` | Получить статус игры | Да (Bearer) |
| `GET`  | `/game/active` | Список всех доступных (ожидающих) игр | Да (Bearer) |
| `POST` | `/game/{id}/join` | Второй игрок присоединяется к существующей игре | Да (Bearer) |
| `POST` | `/game/{id}/check-opponent-left` | Проверить, покинул ли соперник игру | Да (Bearer) |
| `GET`  | `/game/history` | Получить все завершённые игры текущего пользователя | Да (Bearer) |
| `GET`  | `/game/leaderboard?n=10` | Топ N игроков по проценту побед | Да (Bearer) |

### Интерактивная документация

После запуска backend полная документация API доступна в Swagger UI:

- **Swagger UI:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

> **Примечание:** Swagger UI поддерживает Bearer-аутентификацию. Нажмите кнопку "Authorize" (вверху справа) и введите `Bearer <your-access-token>` для тестирования защищённых endpoints.

## Архитектура

### Backend-архитектура (слои)

```text
┌─────────────────────────────────────────┐
│       Web Layer (Controller)            │
│     Обработка HTTP запросов             │
│     AuthController, GameController      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Domain Layer (Service)             │
│     Бизнес-логика приложения            │
│     AuthService, GameService, UserService
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│    Datasource Layer (Repository)        │
│   PostgreSQL Integration (JPA/Hibernate)│
└─────────────────────────────────────────┘
```

### Компоненты

- **Web Layer**: REST контроллеры, DTO модели, мапперы, JWT фильтр аутентификации
- **Domain Layer**: Бизнес-логика, entity модели, интерфейсы репозиториев, сервисы, ИИ (алгоритм Минимакса)
- **Datasource Layer**: JPA репозитории, мапперы сущностей, PostgreSQL
- **DI Configuration**: Spring конфигурация бинов, JWT провайдер, Security конфигурация (stateless, Bearer tokens)

### Frontend-архитектура

- **React Components**: Функциональные компоненты (LoginForm, RegisterForm, GameModeSelection, UserProfile)
- **Context API**: AuthContext для управления состоянием аутентификации с сохранением токенов
- **Custom Hooks**: Переиспользуемая игровая логика (useGame) с polling, повторными попытками и валидацией
- **TypeScript Interfaces**: Строгая типизация API контрактов
- **Vite**: Быстрая разработка с proxy для backend API
- **API утилита**: Централизованный `authorizedFetch` с автообновлением токенов при 401

### Архитектура безопасности

- **Stateless JWT**: Без серверных сессий; токены содержат ID пользователя и роли
- **Хеширование паролей**: BCrypt с автоматической генерацией соли
- **Срок действия токенов**: Access-токены (1 час), refresh-токены (7 дней)
- **Ротация токенов**: Refresh-эндпоинты выдают новый refresh-токен, старый становится невалидным
- **Ролевой доступ**: Роль `USER` по умолчанию; будущая поддержка `ADMIN` через `@PreAuthorize`

## Зависимости

### Backend

- **Java 18**
- **Spring Boot 3.3.4**
- **SpringDoc OpenAPI 2.6.0** — Интеграция Swagger UI
- **Spring Security** — JWT Bearer-аутентификация
- **Spring Data JPA** — Абстракция работы с БД
- **jjwt 0.13.0** — Генерация и валидация JWT токенов
- **PostgreSQL** — Основная база данных
- **H2** — Тестовая БД (in-memory)

### Frontend

- **React 19.2.0**
- **TypeScript 5.9.3**
- **Vite 7.2.4**
- **ESLint** — Проверка качества кода
- **Prettier** — Форматирование кода

## 🔧 Конфигурация

### Переменные окружения Backend

Обязательные переменные (устанавливаются в `.env`, Docker Compose или CI/CD):

| Переменная | Описание | Пример |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://localhost:5433/game_sessions_storage` |
| `SPRING_DATASOURCE_USERNAME` | Имя пользователя БД | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД | `secure_password` |
| `JWT_SECRET` | Секрет для подписи JWT (HS256, мин. 256 бит) | `base64-encoded-secret` |

Опциональные (со значениями по умолчанию):

| Переменная | Значение по умолчанию | Описание |
| --- | --- | --- |
| `jwt.access-token-validity` | `3600` | Срок действия access-токена в секундах (1 час) |
| `jwt.refresh-token-validity` | `604800` | Срок действия refresh-токена в секундах (7 дней) |

Файл конфигурации: `backend/src/main/resources/application.properties`

### Конфигурация прокси Frontend

Vite прокси (`frontend/vite.config.ts`) перенаправляет API запросы:

```typescript
server: {
  proxy: {
    '/game': { target: 'http://localhost:8081', changeOrigin: true },
    '/auth': { target: 'http://localhost:8081', changeOrigin: true },
  }
}
```

## Production-развертывание

Для production-окружения:

1. Используйте Docker Compose с внешней конфигурацией
2. Сгенерируйте надёжный `JWT_SECRET` (минимум 32 случайных байта, храните безопасно)
3. Используйте управляемый PostgreSQL сервис или persistent volumes
4. Настройте reverse proxy (Nginx) для SSL termination
5. Установите корректные CORS origins в `SecurityConfig.java`
6. Сборка backend: `./gradlew build -x test` (JAR в `backend/build/libs/`)
7. Сборка фронтенда: `npm run build` (статичные файлы в `frontend/dist/`)
8. Раздача фронтенда через Nginx или CDN

Рекомендуется использовать Docker volumes и secrets для конфиденциальных данных в production.

## Тестирование

### Backend

```bash
cd backend
./gradlew test                    # Запуск всех тестов с H2 БД
./gradlew test --tests org.example.domain.service.GameServiceTest  # Конкретный тест
./gradlew test --info             # Подробный вывод
./gradlew jacocoTestReport        # Генерация отчёта о покрытии (HTML в build/reports)
```

### Frontend

```bash
cd frontend
npm run lint                      # Проверка качества кода ESLint
npm run format:check              # Проверка форматирования Prettier
```

## Устранение неполадок

### JWT токен истёк

Фронтенд автоматически пытается обновить токен. Если обновление не удалось (401 на refresh-эндпоинте), вы будете перенаправлены на страницу входа.

### Конфликты портов

- Backend по умолчанию: 8081 (изменяется в `application.properties` или маппинге Docker)
- Frontend dev: 5173, prod: 80 (Docker)
- PostgreSQL: 5433 внешний (контейнер 5432)

### Ошибки подключения к БД

- Убедитесь, что PostgreSQL запущен: `docker ps` или `pg_isready`
- Проверьте переменные окружения: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- Docker сеть: убедитесь, что контейнер `backend-api` может reach контейнер `db` (compose сеть)

### API вызовы возвращают 401

- Убедитесь, что у вас валидный access-токен (не истёкший)
- Проверьте заголовок `Authorization: Bearer <token>`
- Обновите токен через `/auth/refresh/access` при необходимости
- Для разработки убедитесь, что запущен Vite (порт 5173) для proxy

### Авторизация в Swagger UI

1. Вызовите `/auth/signin` для получения токенов (используйте "Try it out" в Swagger)
2. Скопируйте значение `accessToken`
3. Нажмите кнопку "Authorize" (вверху справа) → введите `Bearer <accessToken>` → Authorize
4. Теперь можно тестировать защищённые endpoints

## Вклад в проект

Следуйте стандартным best practices Spring Boot и React:
- Слоистая архитектура: контроллеры → сервисы → репозитории
- Внедрение зависимостей для всех бинов
- Принцип единственной ответственности (SRP)
- DTO для API контрактов; никаких entity за пределами datasource слоя
- Комплексная обработка ошибок через `@ControllerAdvice`
- Unit-тесты для сервисов, интеграционные тесты для контроллеров

## Лицензия

Проект создан в образовательных целях.
