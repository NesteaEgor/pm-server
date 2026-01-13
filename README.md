# PM Server

Backend-сервер для системы управления проектами и задачами.

Сервер реализован на Java с использованием Spring Boot и предоставляет REST API и WebSocket для мобильного клиента.

## Технологии

- Java 17
- Spring Boot
- Spring Web (REST API)
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- WebSocket (STOMP)
- Gradle
- Docker / Docker Compose

## Основной функционал

- Регистрация и авторизация пользователей (JWT)
- Управление проектами и участниками
- Управление задачами (CRUD, статусы, дедлайны)
- Комментарии к задачам
- Чат проекта в реальном времени (WebSocket)
- Реакции, read-receipts, typing события
- Генерация PDF-отчетов по задачам проекта


