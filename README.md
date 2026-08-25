# 🍔 Food Delivery Microservices System
Пет-проект для практической отработки микросервисной архитектуры на Java / Spring Boot, а также для адаптации и перехода на JVM-стек после опыта разработки на **.NET Core**.

## 🛠 Технологический стек
* **Язык & Фреймворк:** Java 17, Spring Boot 4.1.0, Spring Cloud 
* **Централизованная безопасность:** `common-security` модуль (`JwtAuthenticationFilter`, `UserContextFilter` с пробросом `X-User-Id` / `X-User-Role`)
* **Межсервисный REST:** OpenFeign + Resilience4j (Circuit Breakers & Fallback Factories)
* **Брокер сообщений:** RabbitMQ
* **Надежность событий:** Transactional Outbox Pattern 
* **Базы данных:** PostgreSQL (Spring Data JPA)
* **Утилиты:** Lombok, Jackson
---

## 🏛 Архитектура и взаимодействие

Система построена на событийно-ориентированной архитектуре (Event-Driven) с использованием RabbitMQ:

```text
[ Client / Postman ]
                            │
                            ▼ (HTTP)
                  ┌──────────────────┐
                  │ gateway-service  │
                  └─────────┬────────┘
                            │ (Header Injection: X-User-Id)
             ┌──────────────┴──────────────┐
             │ (HTTP)                      │ (HTTP)
             ▼                             ▼
    ┌────────────────┐   Feign (Fallback) ┌────────────────┐
    │  user-service  │ ◄───────────────── │ order-service  │
    └───────┬────────┘                    └───────┬────────┘
            │                                     │ (Outbox DB Tx)
            ▼                                     ▼
     [( User DB )]                           [( Order DB )]
                                                  │
                                                  │ RabbitMQ Exchange
                                                  │ (order.created.event)
                                                  ▼
                                      ┌──────────────────────┐
                                      │ notification-service │
                                      └──────────┬───────────┘
                                                 │
                                                 ▼
                                       [( Notification DB )]
```
## **Реализованные механизмы надежности в RabbitMQ:**
**Идемпотентность обработчиков:** Защита от дубликатов сообщений (проверка обработанных eventId на стороне потребителя).

**Retry Policy:** Автоматические повторные попытки обработки сообщений при сбоях.

**Dead Letter Queue (DLQ):** Перенаправление сбойных сообщений в отдельную "мертвую" очередь.

**DLQ Logging:** Логирование проблемных сообщений из DLQ в БД для последующего анализа.

## 🚀 Текущий функционал
**Централизованный Gateway:** Единая точка входа для клиентов.

**Пользователи:** Валидация пользователей при оформлении заказов.

**Заказы:** Защищенное создание заказов без возможности подмены userId, вынос сетевых I/O вызовов за пределы БД-транзакций, фоновая отправка Outbox-событий.

**Уведомления:** Идемпотентная обработка событий создания заказа и сохранение истории уведомлений.

## 🚀 Как запустить проект локально
Требования:

Java 17+;

Запущенный локально RabbitMQ (стандартный порт 5672, менеджмент-панель 15672)

Запущенная локально СУБД Postgres (настройки следует обновить в .properties-файлах)

**Порядок запуска:**

1. Клонировать репозиторий:

```Bash
git clone [https://github.com/valdemare/microservice-pet-app.git](https://github.com/valdemare/microservice-pet-app.git)
cd microservice-pet-app
```

2. Собрать общий модуль безопасности:
```Bash
cd common-security
mvn clean install
cd ..
 ```
3. Убедиться, что PostgreSQL и RabbitMQ запущены.

4. Запустить сервисы по очереди:

 * discovery-service / gateway-service

 * user-service

 * order-service

 * notification-service

## 📌 План дальнейшего развития (Roadmap)
- [x] **Гарантии доставки (Transactional Outbox Pattern):** Исключение потери событий при падении брокера или базы в момент сохранения заказа.
- [x] **API Gateway & Context:** Маршрутизация внешних запросов и централизованный проброс контекста пользователя.
- [x] **Resilience & Circuit Breaker:** Изоляция вызовов между сервисами при помощи Resilience4j и FallbackFactory.
- [x] **Distributed Tracing:** Подключение и визуализация сквозных трасс через Micrometer Tracing + Jaeger (OTLP).
- [x] **Frontend:** Простой веб-интерфейс (`index.html`) для авторизации и проведения заказов.
- [x] **Интеграционное тестирование:** Написание тестов на контроллеры и Feign-клиенты с использованием WireMock / Testcontainers.
- [ ] **Контейнеризация:** Написание Dockerfile для каждого сервиса и docker-compose.yml для развертывания всей инфраструктуры.