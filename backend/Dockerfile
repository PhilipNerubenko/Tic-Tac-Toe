# Этап 1: Сборка
# Используем Amazon Corretto 18 как стабильный источник Java 18
FROM amazoncorretto:18 AS build
WORKDIR /app

# Копируем файлы для работы Gradle Wrapper
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN sed -i 's/\r$//' gradlew

# Даем права на выполнение скрипта сборки (важно для Linux)
RUN chmod +x ./gradlew

# Копируем исходный код
COPY src ./src

# Сборка проекта (bootJar — для Spring Boot, jar — для обычного Java)
RUN ./gradlew clean bootJar -x test

# Этап 2: Запуск
FROM amazoncorretto:18
WORKDIR /app

# У Gradle путь к результату: build/libs/
# Берем только основной jar (исключая -plain.jar, если он есть)
# Вместо старой строки COPY
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
