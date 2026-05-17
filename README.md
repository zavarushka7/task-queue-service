# Task Queue Service

Сервис очереди задач на обработку файлов. Принимает задачи, ставит их в очередь, обрабатывает асинхронно и сохраняет результат.

## Технологии

Kotlin, Spring Boot, Coroutines, H2/PostgreSQL, Spring Data JPA, Liquibase, Swagger, JUnit, MockK

## Запуск

```bash
./gradlew bootRun
```

Приложение доступно на `http://localhost:8080`.

Swagger: `http://localhost:8080/swagger-ui.html`

H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskqueue`, user: `sa`, пароль пустой)

## Тесты

```bash
./gradlew test
```
