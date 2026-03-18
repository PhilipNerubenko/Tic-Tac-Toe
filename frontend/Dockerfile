# Этап 1: Сборка
FROM node:20-alpine AS build
WORKDIR /app

# Кэшируем зависимости (отдельный слой)
COPY package*.json ./
RUN npm install

# Копируем исходники и собираем
COPY . .
RUN npm run build

# Этап 2: Раздача статики
FROM nginx:alpine

# 1. Обязательно очищаем папку, чтобы не было конфликтов со стандартным "Welcome to Nginx"
RUN rm -rf /usr/share/nginx/html/*

# 2. Копируем билд.
COPY --from=build /app/dist /usr/share/nginx/html

# 3. Копируем конфиг
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]