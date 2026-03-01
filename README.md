# ScaleCart — Production-Grade Backend

**ScaleCart** is a single, runnable, production-style e-commerce backend built with Spring Boot. It demonstrates REST APIs, JPA persistence, JWT security, Redis caching, async processing, and Kafka messaging in one deployable codebase.

**Tech stack:** Java 21, Spring Boot 3.2.x, Spring Data JPA, Spring Security (JWT), Spring Cache (Redis with in-memory fallback), Spring Kafka, Actuator (health, metrics), SpringDoc OpenAPI, H2 (dev) / PostgreSQL (prod), Docker.

---

## Prerequisites

- **JDK 21**
- **Maven** (or use the project’s Maven wrapper if present)
- **Docker** and **Docker Compose** (optional, for full stack)

---

## Build and run locally

From the `capstone-production-app` directory:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or with Maven on your path:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The **dev** profile uses H2 (in-memory), in-memory cache, and no Kafka — so the app starts without any external services. The API is available at **http://localhost:8080**.

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **Actuator health:** http://localhost:8080/actuator/health  

---

## Run with Docker Compose

To run the full stack (app + PostgreSQL + Redis + Kafka) from the capstone directory:

```bash
docker-compose up --build
```

The app runs with the **prod** profile and connects to Postgres, Redis, and Kafka. Health checks use `/actuator/health`. Override secrets via environment variables (e.g. `APP_JWT_SECRET`, `SPRING_DATASOURCE_PASSWORD`).

---

## API overview

| Area        | Endpoints | Auth |
|------------|-----------|------|
| **Auth**    | `POST /api/auth/login` — body `{"username","password"}` → returns `{"token":"..."}` | Public |
| **Products**| `GET /v1/products`, `GET /v1/products/{id}` | Public |
|             | `POST /v1/products`, `PUT /v1/products/{id}`, `DELETE /v1/products/{id}` | Admin (JWT) |
| **Categories** | `GET /v1/categories`, `GET /v1/categories/{id}` | Public |
|             | `POST /v1/categories`, `PUT /v1/categories/{id}` | Admin (JWT) |
| **Orders**  | `GET /v1/orders?customerId=`, `GET /v1/orders/{id}`, `POST /v1/orders` | Authenticated (JWT) |

**Authentication:** After login, send the token in the header:

```http
Authorization: Bearer <token>
```

**Default users (dev data):** `user` / `password` (USER), `admin` / `admin` (USER + ADMIN).

---

## Deployment

- **Docker:** Build the image from the Dockerfile (multi-stage build, JRE 21, non-root user). Build context is the capstone directory.
- **Compose:** Use the included `docker-compose.yml` for app + Postgres + Redis + Kafka; configure env vars for production secrets.
- **Kubernetes:** Use health endpoints `/actuator/health/liveness` and `/actuator/health/readiness`

---

## Documentation

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | High-level architecture and capstone application layout |
| [PRODUCTION.md](PRODUCTION.md) | Production readiness checklist; capstone app is the reference implementation |

