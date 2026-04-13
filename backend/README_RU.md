# Крестики-нолики — Backend

REST API серверная часть на Spring Boot 3.3.4 для игры «Крестики-нолики» со слоистой архитектурой, базовой аутентификацией и хранением данных в PostgreSQL.

## 🚀 Возможности

-   **Spring Boot 3.3.4** — современный Java-фреймворк с встроенными лучшими практиками
-   **Spring Security + Basic Auth** — базовая аутентификация и авторизация
-   **PostgreSQL** — надёжное хранение данных пользователей и игровых сессий
-   **Spring Data JPA** — удобная абстракция для работы с данными через Hibernate
-   **Swagger UI** — интерактивная документация API по адресу `/swagger-ui.html`
-   **Слоистая архитектура** — чёткое разделение (Web → Domain → Datasource)
-   **Dependency Injection** — полное управление зависимостями через Spring DI
-   **Игровые сессии** — поддержка нескольких одновременных игр для каждого пользователя
-   **ИИ-противник** — встроенная логика ИИ для ходов ноликами (O)
-   **Управление пользователями** — регистрация, вход и управление профилем

## 📋 Начало работы

### Предварительные требования

-   Java 18+
-   PostgreSQL 15+ (или используйте Docker Compose)
-   Gradle (обёртка включена в проект)

### Установка

1.  Клонируйте репозиторий (если вы ещё этого не сделали):

    ```bash
    git clone <repository-url>
    cd backend
    ```

2.  Настройте переменные окружения:

    Для настройки backend вам необходимо задать следующие переменные окружения. Рекомендуется создать файл `.env` в директории `backend`.

    *   Скопируйте пример файла: `cp .env.example .env`
    *   Заполните значения в `.env`.

    Требуются следующие переменные:

    | Переменная                     | Описание                       | Пример                                                                |
    | ---------------------------- | --------------------------------- | ---------------------------------------------------------------------- |
    | `SPRING_DATASOURCE_URL`      | URL подключения к PostgreSQL         | `jdbc:postgresql://localhost:5433/game_sessions_storage`             |
    | `SPRING_DATASOURCE_USERNAME` | Имя пользователя БД                 | `postgres`                                                             |
    | `SPRING_DATASOURCE_PASSWORD` | Пароль БД                 | `postgres`                                                             |

    **Важно:** Никогда не указывайте эти значения непосредственно в файле `application.properties`. Используйте переменные окружения для обеспечения безопасности ваших учётных данных. Рекомендуется использовать `.env` файл или Docker Secrets для управления этими переменными в production environment.

3.  Запустите PostgreSQL (если вы не используете Docker Compose):

    ```bash
    docker run -d --name postgres-db \
      -e POSTGRES_DB=game_sessions_storage \
      -e POSTGRES_USER=postgres \
      -e POSTGRES_PASSWORD=postgres \
      -p 5433:5432 \
      postgres:15-alpine
    ```

4.  Запустите приложение:

    ```bash
    ./gradlew bootRun
    ```

    API будет доступен по адресу `http://localhost:8081`

    В Windows:
    ```bash
    gradlew.bat bootRun
    ```

### Переменные окружения

Для работы backend требуются следующие environment variables:

| Переменная                     | Описание                       | Пример                                                                |
| ---------------------------- | --------------------------------- | ---------------------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`      | URL подключения к PostgreSQL         | `jdbc:postgresql://localhost:5433/game_sessions_storage`             |
| `SPRING_DATASOURCE_USERNAME` | Имя пользователя БД                 | `postgres`                                                             |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД                 | `postgres`                                                             |

## 📦 Доступные команды

```bash
# Запуск приложения
./gradlew bootRun

# Сборка проекта
./gradlew build

# Запуск тестов
./gradlew test

# Очистка артефактов сборки
./gradlew clean

# Подробный лог сборки
./gradlew build --stacktrace
```

## 🏗️ Структура проекта

```
src/
├── main/java/org/example/
│   ├── Main.java               # Точка входа приложения
│   ├── web/                    # Слой REST-контроллеров
│   │   ├── controller/         # Эндпоинты @RestController
│   │   │   ├── AuthController  # /auth/signup, /auth/login
│   │   │   ├── GameController  # Эндпоинты /game
│   │   │   └── GlobalExceptionHandler
│   │   ├── filter/             # AuthFilter (валидация Basic Auth)
│   │   ├── mapper/             # Мапперы DTO ↔ Entity
│   │   └── model/              # DTO запросов/ответов
│   ├── domain/                 # Слой бизнес-логики
│   │   ├── model/              # Доменные сущности (Game, GameMap, User)
│   │   ├── repository/         # Интерфейсы репозиториев
│   │   ├── service/            # Бизнес-логика
│   │   │   ├── AuthService     # Управление базовой аутентификацией
│   │   │   ├── GameService     # Правила игры и логика ИИ
│   │   │   └── UserService     # Управление пользователями
│   │   └── exception/          # Пользовательские исключения
│   ├── datasource/             # Слой доступа к данным
│   │   ├── mapper/             # Пользовательские мапперы сущностей
│   │   ├── model/              # JPA-сущности (UserEntity, GameSessionEntity и др.)
│   │   └── repository/         # Реализации Spring Data JPA
│   └── di/config/              # Классы @Configuration Spring
│       ├── GameConfig          # Конфигурация игровых бинов
│       └── SecurityConfig      # Настройка Spring Security + Basic Auth
├── resources/
│   └── application.properties  # Конфигурация сервера и БД
└── test/java/org/example/      # Модульные и интеграционные тесты
```

## 🎮 Как это работает

### Процесс аутентификации

1.  **Регистрация**: `POST /auth/signup` создаёт новый аккаунт пользователя
2.  **Вход**: `POST /auth/login` выполняет аутентификацию пользователя
3.  **Авторизация**: Добавьте заголовок `Authorization: Basic <base64-encoded-credentials>` к последующим запросам

### Игровой процесс

1.  **Создание игры**: `POST /game?size=3` создаёт новую игровую сессию (требуется авторизация)
2.  **Ход игрока**: `POST /game/{id}` с состоянием игры, содержащим ваш ход (X)
3.  **Ответ ИИ**: Backend обрабатывает ход и рассчитывает ход ИИ (O)
4.  **Статус игры**: Ответ содержит обновлённое поле и состояние игры (PLAYING/WIN/DRAW)

### API эндпоинты

#### Аутентификация

| Метод | Эндпоинт | Описание | Требуется авторизация |
| ------ | -------------- | ------------------- | ------------- |
| `POST` | `/auth/signup` | Регистрация нового пользователя   | Нет            |
| `POST` | `/auth/login`  | Вход и получение сессии | Нет            |

#### Пользователь

| Метод | Эндпоинт | Описание | Требуется авторизация |
| ------ | ------------- | ---------------------- | ------------- |
| `GET`  | `/user/profile` | Получить профиль текущего пользователя | Да           |

#### Игра

| Метод | Эндпоинт    | Описание                                    | Требуется авторизация |
| ------ | ----------- | ---------------------------------------------- | ------------- |
| `POST` | `/game?size=3` | Создать новую игру (размер: 3 и более) | Да           |
| `POST` | `/game/{id}`  | Сделать ход и получить ответ ИИ              | Да           |
| `GET`  | `/game/{id}`   | Получить статус игры                                | Да           |
| `GET`  | `/game`      | Список всех игр пользователя                          | Да           |

### Формат ответа

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

Значения карты: `0 = пусто`, `1 = X (игрок)`, `2 = O (ИИ)`

## 🔧 Конфигурация

### Application Properties

Расположен в `src/main/resources/application.properties`

Основные настройки:
```properties
# Порт сервера
server.port=8081

# Подключение к БД (через переменные окружения)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# Настройки Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Изменение порта

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

### Спецификация OpenAPI

Машиночитаемая спецификация API в формате JSON:
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
  tic-tac-toe-backend:latest
```

### Использование Docker Compose

Из корня проекта:
```bash
docker compose up backend-api
```

Frontend автоматически подключится к API по адресу `http://localhost:8081`

## Production Деплой (Развертывание)

Для продакшн-среды настоятельно рекомендуется использовать стратегию контейнеризации с помощью Docker Compose. Также следует настроить Reverse Proxy (например, Nginx) для управления SSL-сертификатами и раздачи статических файлов. Переменные окружения должны передаваться в контейнер через защищенные механизмы, такие как Docker Secrets или файл `.env`, который не должен попадать в репозиторий.

Чтобы собрать проект для продакшена, выполните команду:
```bash
./gradlew build -x test
```
После выполнения в директории `build/libs` будет создан готовый к работе JAR-файл.

## 🧪 Тестирование

```bash
# Запуск всех тестов
./gradlew test

# Запуск конкретного тестового класса
./gradlew test --tests org.example.domain.service.GameServiceTest

# Запуск с подробным выводом
./gradlew test --info
```

Тесты используют встроенную базу данных H2 для изоляции.

## 🔍 Устранение неполадок

### Ошибка сборки

```bash
# Очистка и пересборка
./gradlew clean build

# Показать подробные ошибки
./gradlew build --stacktrace
```

### Порт 8080 уже занят

Измените порт в `application.properties` или используйте параметр командной строки, указанный выше.

### Ошибка подключения к базе данных

- Убедитесь, что PostgreSQL запущен
- Проверьте правильность установки переменных окружения
- Проверьте формат URL подключения: `jdbc:postgresql://host:port/dbname`

### Документация API не загружается

Убедитесь, что приложение запущено:

```bash
curl http://localhost:8081/swagger-ui.html
```

Проверьте логи на наличие ошибок при запуске:

```bash
./gradlew bootRun
```

### Frontend не может подключиться к API

Проверьте, что backend запущен на `http://localhost:8081`:

```bash
curl -X POST http://localhost:8081/auth/signup \
     -H "Content-Type: application/json" \
     -d '{"username":"test","password":"test"}'
```

Проверьте настройки CORS и сети.

## 🛠️ Разработка

### Паттерн слоистой архитектуры

-   **web**: HTTP-запросы/ответы, DTOs, validation, Basic Auth filter
-   **domain**: Чистая бизнес-логика, правила игры, стратегия ИИ, сервис аутентификации
-   **datasource**: Хранение данных через JPA/Hibernate + PostgreSQL
-   **di**: Конфигурация бинов Spring, конфигурация безопасности

### Добавление новых функций

1.  Создайте модель данных в `domain.model`
2.  Определите интерфейс репозитория в `domain.repository`
3.  Реализуйте логику в `domain.service`
4.  Добавьте HTTP-эндпоинт в `web.controller`
5.  Создайте JPA-сущность в `datasource.model`
6.  Реализуйте репозиторий в `datasource.repository`
7.  Напишите тесты в `src/test/`

### Качество кода

-   Следуйте соглашениям Spring Boot
-   Используйте внедрение зависимостей
-   Соблюдайте принцип единственной ответственности
-   Добавляйте JavaDoc для публичных методов
-   Пишите модульные тесты для сервисов и контроллеров
