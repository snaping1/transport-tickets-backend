# Transport Tickets — Backend

Серверная часть приложения для покупки транспортных билетов. Написана полностью на **Kotlin** с использованием фреймворка **Ktor**.

## Технологии

| Категория | Инструмент |
|---|---|
| Язык | Kotlin |
| Framework | Ktor 2.3.7 + Netty |
| БД | PostgreSQL (Neon.tech) + HikariCP |
| Миграции | Flyway (V1–V11) |
| Аутентификация | JWT (собственная, без Firebase) |
| Сериализация | kotlinx.serialization |

## API

### Аутентификация пользователей

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/auth/register` | Регистрация по email и паролю |
| `POST` | `/auth/login` | Вход, возвращает JWT-токен |

### Защищённые эндпоинты (требуют `Authorization: Bearer <token>`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/routes` | Поиск маршрутов (`origin`, `destination`, `date`, `transportType`) |
| `GET` | `/routes/{id}/seats` | Занятые места на маршруте |
| `POST` | `/tickets/buy` | Покупка билета |
| `GET` | `/tickets/my` | Мои билеты |
| `GET` | `/tickets/{id}` | Детали билета |
| `DELETE` | `/tickets/{id}` | Отмена билета |

### Администрирование (отдельный JWT)

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/admin/login` | Вход администратора |
| `GET` | `/admin/routes` | Список маршрутов |
| `POST` | `/admin/routes` | Добавить маршрут |
| `DELETE` | `/admin/routes/{id}` | Удалить маршрут |

## База данных

Собственная база данных PostgreSQL на облачном сервисе **Neon.tech**. Схема управляется через Flyway-миграции (V1–V11): маршруты, места, пользователи, пассажиры, билеты, оплаты, администраторы.

## Запуск

```bash
./gradlew run
```

Сервер запускается на `http://localhost:8080`.

Для переопределения базы данных:

```bash
export DATABASE_URL=jdbc:postgresql://host:5432/dbname?user=user&password=pass
```

## Тесты

```bash
./gradlew test
```
