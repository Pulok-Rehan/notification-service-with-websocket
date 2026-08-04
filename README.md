# Notification Service

Centralized, event-driven Notification Service built with Java 21 + Spring Boot 3.x. Handles push notifications (Firebase), real-time WebSocket delivery, notification history, read/unread + unread-count, online presence, topic/role/broadcast/multicast notifications, and generic database-change events so frontends update live without polling.

`mobileNumber` is the single identity key used everywhere: authentication, Firebase targeting, WebSocket sessions/subscriptions, unread counts, and presence.

## Stack

Java 21, Spring Boot 3.3, Spring WebSocket/STOMP, Spring Data MongoDB, Spring Data Redis, Spring Kafka, Firebase Admin SDK, Spring Security (JWT), MapStruct, Lombok, springdoc-openapi.

## Project layout

```
controller/   REST endpoints (notification, tokens, topics, presence, admin)
service/      interfaces; service/impl  business logic + channel strategies
repository/   Spring Data MongoDB repositories
entity/       MongoDB documents
dto/          request/response payloads
mapper/       MapStruct entity<->DTO mappers
security/     JWT validation, Spring Security config
websocket/    STOMP config, auth interceptor, presence listener, WebSocketNotifier
firebase/     Firebase Admin SDK init + push sender
redis/        presence + unread-count Redis services
event/listener/publisher/  Kafka-based domain event consumption (the "database change events" feature)
scheduler/    scheduled-notification dispatch + TTL expiry (runs every minute/hour)
config/       Mongo indexes, Redis, OpenAPI, Kafka topic bootstrap
exception/    global exception handler + custom exceptions
response/     ApiResponse<T> wrapper used by all controllers
```

## How delivery works

`NotificationServiceImpl` is the single orchestrator for every send endpoint. For each request it:

1. Persists a `Notification` document (status `CREATED`) — this is the permanent history record.
2. Fans the entity out to every Spring bean implementing `NotificationSender` (`PushNotificationSender`, `WebSocketNotificationSender`, `InAppNotificationSender`). Adding Email/SMS/WhatsApp/APNS/Web Push later is just a new `@Component` implementing that interface — no existing code changes, per your "Future Integrations" requirement.
3. Marks the notification `SENT`, increments the receiver's Redis unread counter, and pushes the new count over `/user/{mobile}/updates`.

If the receiver is offline, `WebSocketNotificationSender` simply skips the live push (the record is already saved); pending notifications are redelivered through `NotificationServiceImpl#deliverPendingOnReconnect`, wired to the WebSocket connect event.

## Database Change Events (the core "no polling" feature)

Any upstream microservice (Attendance, Deposit, Withdrawal, IPO, KYC, Profile, ...) publishes a JSON message shaped like `DatabaseChangeEvent` to the Kafka topic `notification.events`:

```json
{
  "eventType": "APPROVED",
  "module": "attendance",
  "entityId": "ATT001",
  "mobileNumber": "8801711000000",
  "payload": { "status": "APPROVED" },
  "timestamp": "2026-06-30T10:00:00Z"
}
```

`DatabaseChangeEventListener` consumes it and fans out over STOMP to `/topic/{module}` (e.g. `/topic/attendance`, for screens/dashboards watching that module) and, if `mobileNumber` is set, directly to `/user/{mobile}/updates` for that specific user. No per-module code is needed in this service — routing is purely by the `module` field. This is the **outbox/domain-event pattern**, not direct DB polling, per the architectural recommendation in your spec.

## WebSocket

Connect with STOMP+SockJS to `ws://host:8085/ws`, sending `Authorization: Bearer <jwt>` as a STOMP CONNECT header. `WebSocketAuthChannelInterceptor` extracts `mobileNumber` from the JWT and sets it as the STOMP principal, which is what makes `/user/{mobile}/notifications` and `/user/{mobile}/updates` work. Connect/disconnect events automatically update Redis presence.

Topics: `/topic/global`, `/topic/{module}` (attendance, deposit, withdraw, ipo, dashboard, system, ...), `/user/{mobile}/notifications`, `/user/{mobile}/updates`.

## Running locally

### Option A — Docker Compose (recommended, brings up Mongo/Redis/Kafka too)

```bash
docker compose up --build
```

Service starts on `http://localhost:8085`. Swagger UI: `http://localhost:8085/swagger-ui.html`.

### Option B — run the JAR directly against your own Mongo/Redis/Kafka

```bash
mvn clean package -DskipTests
java -jar target/notification-service.jar \
  --MONGODB_URI=mongodb://localhost:27017/notification_service \
  --REDIS_HOST=localhost --REDIS_PORT=6379 \
  --KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  --JWT_SECRET=your-secret \
  --spring.profiles.active=dev
```

### Firebase setup

Download a service-account JSON from Firebase Console → Project Settings → Service Accounts, save it as `firebase-service-account.json` in the project root (or set `FIREBASE_CREDENTIALS_PATH`). Set `FIREBASE_ENABLED=false` to run without Firebase (push sends will log and no-op via `FailedNotificationRepository`).

## Environment variables

| Variable | Purpose |
|---|---|
| `MONGODB_URI` | Mongo connection string |
| `REDIS_HOST`, `REDIS_PORT` | Redis connection |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers |
| `JWT_SECRET` | HMAC secret used to validate incoming JWTs (issued by your Auth/User service) |
| `FIREBASE_ENABLED`, `FIREBASE_CREDENTIALS_PATH` | Firebase Admin SDK |

Profiles: `dev`, `test`, `prod` (`--spring.profiles.active=prod`).

## Key APIs

See `docs/postman_collection.json` for ready-to-import requests, and Swagger UI for the full contract. Highlights:

- `POST /notifications/unicast|multicast|broadcast|topic|role|schedule`
- `GET /notifications/history?mobile=...&page=&size=&read=&search=`
- `PUT /notifications/read/{id}`, `/read-all`, `/unread/{id}`, `/archive/{id}`
- `GET /users/{mobile}/unread-count`
- `POST /tokens/register`, `PUT /tokens/update`, `DELETE /tokens`, `GET /tokens/{mobile}`
- `POST /topics/subscribe|unsubscribe`, `GET /topics/{mobile}`
- `GET /presence/{mobile}`, `/presence/online`, `/presence/count`
- `POST /admin/broadcast`, `/admin/system-alert`, `GET /admin/statistics`

## Mongo indexes

Created automatically on boot (`MongoIndexConfig`). Reference script for manual/Atlas use: `docs/mongo-indexes.js`.

## What's scaffolded vs. what you should still wire up before production

- **Role directory**: `RoleDirectoryServiceImpl` is an in-memory cache today (`cacheRoleMapping`). Point it at your real User Service REST API or sync it via a `role.updated` Kafka event.
- **Email/SMS/WhatsApp/APNS/Web Push**: not implemented, but the `NotificationSender` interface is the exact extension point — add a `@Component` per channel.
- **Rate limiting, audit logging, dead-letter queue**: noted in the spec as security/future items; hook in Spring's `RateLimiter`/Resilience4j and a DLQ Kafka topic for `FailedNotification` retries.
- **`/admin/statistics`**: placeholder; wire to Actuator metrics (`/actuator/metrics`, `/actuator/prometheus`) for delivery success rate, queue size, WS connection count.
- The unicast/multicast/broadcast/topic senders currently fan out to **all** registered senders rather than filtering strictly by the request's `channel` field — adjust `NotificationServiceImpl#dispatch` if you need per-request channel selection instead of "send everywhere it makes sense."

## Tests

`mvn test` runs the included JUnit5/Mockito unit test for the core dispatch flow. Add integration tests with embedded Mongo (`de.flapdoodle.embed.mongo`, already a test dependency) and `spring-kafka-test` for the event listener.
