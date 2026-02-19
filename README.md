# Ktor Store Backend (E-Commerce API)

Backend-сервис для простого интернет‑магазина: пользователи могут регистрироваться/логиниться, смотреть товары и управлять заказами; администратор — управлять товарами и получать статистику.

## Что уже реализовано

**Функционал**
- Регистрация и логин (JWT)
- Просмотр каталога товаров и карточки товара
- Создание заказа, отмена заказа, история заказов пользователя
- Admin-доступ к управлению товарами (создание/обновление/удаление)
- Audit логирование действий
- Очередь событий заказов + consumer (логирование + email‑заглушка)

**Технологии**
- **Ktor 2.x**
- **PostgreSQL** + **Exposed**
- **Flyway** миграции
- **JWT** авторизация
- **Redis (Lettuce)** — кэширование товаров (TTL)
- **RabbitMQ** — producer/consumer событий заказов
- **Swagger UI / OpenAPI**
- Тесты: unit + integration (Testcontainers) + e2e (Ktor testApplication)
- **Dockerfile** и **docker-compose**

## Эндпоинты

### Auth
- `POST /auth/register`
- `POST /auth/login`

### Public
- `GET /products`
- `GET /products/{id}`

### User (JWT)
- `POST /orders`
- `GET /orders`
- `DELETE /orders/{id}`

### Admin (JWT + роль ADMIN)
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`
- `GET /admin/stats/orders`


## Быстрый старт (Docker)

1) Поднять инфраструктуру и сервис:

```bash
docker-compose up --build
```

2) Проверка здоровья:

```bash
curl http://localhost:8080/health
```

3) Swagger UI:
- `http://localhost:8080/swagger`

RabbitMQ UI:
- `http://localhost:15672` (login: `guest`, password: `guest`)

## Конфигурация

Основные параметры берутся из переменных окружения (см. `application.yaml` и docker-compose).

## База данных

Миграции Flyway лежат в:
- `src/main/resources/db/migration`

Создаются таблицы:
- `users`
- `products`
- `orders`
- `order_items`
- `audit_logs`

## Тесты

Запуск всех тестов:

```bash
./gradlew test
```
