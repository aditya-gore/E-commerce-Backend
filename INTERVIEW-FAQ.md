# Capstone-Specific Interview FAQ (SDE-2)

Short answers and talking points for questions an interviewer might ask about the ScaleCart capstone project. Use these to structure a 2–3 minute answer; see [INTERVIEW-SDE2.md](INTERVIEW-SDE2.md) for broader themes.

---

## Project overview

**“How would you describe ScaleCart in 2 minutes?”**

- Production-style e-commerce backend: REST API (versioned `/v1/`), JPA (Product, Category, Order, User), JWT auth, Redis caching with in-memory fallback, async order confirmation, Kafka for order events with idempotent consumer.
- Single deployable artifact; runs locally with dev profile (H2, no Kafka) or full stack with Docker Compose (Postgres, Redis, Kafka).
- Resume line: “ScaleCart – production-style e-commerce backend (Spring Boot, JPA, JWT, Redis, Kafka, Docker).”

**“What’s the request flow from API to database?”**

- Request hits controller → JWT filter validates token and sets principal → controller calls service → service may hit cache (`@Cacheable` on product/category by ID); on miss, repository loads from DB. For order create: one transaction (reserve stock, create order and items), then async confirmation and Kafka event (when enabled).

**“Why monolithic first instead of microservices?”**

- Single codebase demonstrates all concerns (REST, JPA, security, cache, Kafka) without operational overhead; easier to run and demo. Splitting into services (e.g. catalog vs orders) is a later step once scaling or team boundaries justify it.

---

## REST and API design

**“Why version the API with `/v1/`?”**

- Allows future breaking changes under `/v2/` while keeping clients on `/v1/` until they migrate. In ScaleCart we use `/v1/products`, `/v1/categories`, `/v1/orders`.

**“How do you handle validation errors and what’s your error response shape?”**

- Bean Validation on DTOs; `GlobalExceptionHandler` catches `MethodArgumentNotValidException` and returns a single `ErrorDto` with message and optional `fieldErrors` list. Consistent shape for clients.

**“When would you use HATEOAS in this API?”**

- When we want clients to discover next actions from links in the response (e.g. order has `link: confirm`, `link: cancel`). ScaleCart currently returns plain DTOs; HATEOAS would add `_links` for hypermedia-driven flows.

---

## Data and transactions

**“How do you avoid N+1 when loading an order with items?”**

- We use a repository method that fetches order and items in one query (e.g. `findByIdWithItems` with `@EntityGraph` or JOIN FETCH on `Order.items`). Avoids lazy-loading each item in a loop.

**“Why optimistic locking on product inventory? When would you use pessimistic?”**

- ScaleCart uses `@Version` on Product so concurrent updates (e.g. two orders reducing stock) cause one to fail with optimistic lock; we can retry or return a clear error. Pessimistic locking (`LOCK`) is better when contention is very high and we want to block the second request until the first commits.

**“How do you handle a failed order creation after inventory is decremented?”**

- Order creation runs in one transaction: we decrement stock and create order/items in the same `@Transactional` method. If anything fails, the whole transaction rolls back so inventory is not left decremented without an order.

---

## Security

**“How does JWT flow work in your app? Where do you validate the token?”**

- Client posts credentials to `POST /api/auth/login`; we return a signed JWT. Subsequent requests send `Authorization: Bearer <token>`. `JwtAuthenticationFilter` runs before the controller, parses the token, validates signature and expiry, and sets the security context.

**“Why stateless JWT vs server-side sessions?”**

- Stateless JWT avoids server-side session store; easier to scale horizontally and run behind load balancers without sticky sessions. Trade-off: we can’t revoke a token before expiry without a blacklist or short TTL.

**“How would you add refresh tokens or revoke a token?”**

- Refresh token: issue long-lived refresh token (stored in DB or secure cookie); access token short-lived; new access token issued when refresh is presented. Revocation: maintain a blacklist (e.g. Redis) of revoked token IDs and check it in the filter, or use short TTL and require re-login to “revoke.”

---

## Caching

**“What do you cache and when do you evict?”**

- We cache product and category by ID (`@Cacheable`). Evict on update and delete (`@CacheEvict`) so reads see fresh data after writes.

**“What happens when Redis is down?”**

- When `app.cache.redis.enabled` is false (e.g. dev), we use an in-memory `ConcurrentMapCacheManager`. In prod we rely on Redis; if Redis is down, cache calls could fail unless we add a fallback or circuit breaker (not in current scope).

**“How would you prevent cache stampede?”**

- Use a lock per key (e.g. in-memory or Redis lock) so only one thread loads from DB and others wait or read stale; or use probabilistic early expiration so not all keys expire at once.

---

## Kafka and messaging

**“Why emit an event when an order is created? Who consumes it?”**

- To decouple order creation from downstream actions (e.g. notifications, analytics, inventory sync). In ScaleCart we have an in-app consumer that processes order-events with idempotency; in a larger system other services could consume the same topic.

**“How do you ensure the same order event isn’t processed twice?”**

- Consumer keeps a store of processed order IDs (e.g. in-memory `ProcessedOrderEventStore`). Before handling a message we check `alreadyProcessed(orderId)`; if yes, skip; otherwise process and `markProcessed(orderId)`.

**“What if the consumer fails after processing but before committing offset?”**

- We’d process the same event again after restart. That’s why idempotency is required: the second run (e.g. “order already processed”) is a no-op so we don’t double-apply side effects.

---

## Performance and scaling

**“How would you scale this app under high load?”**

- Horizontal scaling: run multiple app instances behind a load balancer; JWT is stateless so any instance can serve any request. Scale DB (read replicas), Redis (cluster if needed), and Kafka (partitions, consumer groups).

**“Where are the bottlenecks (DB, cache, Kafka)?”**

- DB: product/order queries and transaction duration. Cache reduces DB load for product/category by ID. Kafka: throughput and consumer lag. We’d add metrics (Actuator, Micrometer) to measure latency and lag.

**“How would you add rate limiting or protect downstream services?”**

- Rate limiting: filter or annotation that checks per-client (or per-user) counters (e.g. in Redis) and returns 429 when over limit. For downstream (e.g. Kafka producer): circuit breaker and retries with backoff so we don’t overwhelm the broker.

---

## Deployment and ops

**“How do you run this in production? Docker? K8s?”**

- Docker: multi-stage Dockerfile (build JAR, run with JRE 21, non-root user). Docker Compose runs app + Postgres + Redis + Kafka. For K8s we’d use the same image and expose liveness/readiness via Actuator; reuse patterns from 08-deployment.

**“How do you handle secrets and config?”**

- Secrets via environment variables (e.g. `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`); no defaults in prod. Config in `application.yml` with profile-specific values; prod uses env placeholders.

**“What health checks do you expose and how would you use them?”**

- Actuator exposes `/actuator/health`; with probes enabled we have `/actuator/health/liveness` and `/actuator/health/readiness`. K8s liveness probe: if liveness fails, restart the pod. Readiness: if not ready, remove from service so traffic stops until dependencies (DB, Redis) are healthy.

---

## Trade-offs and design

**“What would you do differently if you rebuilt it?”**

- Consider Flyway from day one for prod schema; add rate limiting and request validation at the edge; optional distributed tracing (OpenTelemetry) for cross-service flow.

**“Why Redis for cache instead of Caffeine only?”**

- Redis allows shared cache across multiple app instances; Caffeine is local to one JVM. ScaleCart uses Redis when enabled (prod) and in-memory (dev) so we can run without Redis for local demos.

**“How would you split this into microservices and what would you break out first?”**

- Natural split: Catalog service (products, categories) and Orders service (orders, items). Catalog owns product/category CRUD and cache; Orders calls Catalog for product info (e.g. price) and owns order lifecycle. Event flow (order created) stays via Kafka so other systems can react without coupling.
