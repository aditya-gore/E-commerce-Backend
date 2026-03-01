# Interview & SDE-2 Discussion Points

Consolidated themes across the Spring Boot Mastery modules for interview preparation and SDE-2 level discussion. Use with each module’s CONCEPTS.md and COMMON_MISTAKES.md.

---

## 1. Consistency and transactions

- **When to use transactions:** Service layer for a single use case; keep TX short and avoid I/O (HTTP, Kafka) inside the same TX unless designed (e.g. outbox).
- **Propagation:** REQUIRED (join or create) vs REQUIRES_NEW (suspend, new TX, commit independently). Use REQUIRES_NEW for audit or outbox that must persist even if main TX rolls back.
- **N+1 and lazy loading:** Only managed entities can load lazy associations; detached entity + lazy collection outside TX → LazyInitializationException. Fix: fetch join, entity graph, or DTO with data loaded in TX.
- **Optimistic vs pessimistic locking:** Optimistic (version field) for read-heavy; pessimistic (SELECT FOR UPDATE) for high contention. Lost updates without any locking; handle OptimisticLockException with retry or 409.
- **Distributed consistency:** Cross-service operations are not in one TX. Use saga, outbox, or event-driven compensation; discuss exactly-once vs at-least-once + idempotent consumer.

**Modules:** [03-database-and-transactions](../03-database-and-transactions), [06-messaging](../06-messaging).

---

## 2. Performance

- **Caching:** When to cache (read-heavy, stable data); key design and eviction; cache stampede and mitigation (single-flight, probabilistic refresh). Don’t cache without eviction on update.
- **Async:** Use for I/O-bound, non-critical path (notifications, logging). Don’t block the request thread on async result without timeout. Thread pool sizing and rejection policy.
- **DB:** Connection pool size vs DB max connections; indexes on filter/join columns; pagination and batch size; avoid N+1 and unbounded result sets.
- **REST:** DTOs and pagination; no lazy serialization; consider ETag/conditional requests for large reads.
- **Kafka:** Batch size, commit frequency, idempotency store performance.

**Modules:** [02-building-rest-api](../02-building-rest-api), [03-database-and-transactions](../03-database-and-transactions), [05-performance-and-caching](../05-performance-and-caching), [06-messaging](../06-messaging).

---

## 3. Security

- **Auth:** Stateless JWT vs session; short-lived access token and refresh or re-login. Where to validate (gateway vs service); token storage (memory, cookie, etc.) and XSS/CSRF considerations.
- **Authorization:** URL vs method security; RBAC vs fine-grained permissions; when to use @PreAuthorize and custom evaluators.
- **Secrets:** Never in code or logs; use env or secret manager; rotation and revocation. Password hashing (BCrypt/Argon2).
- **API security:** HTTPS only; CORS and security headers; rate limiting and input validation; no information leakage in errors (e.g. “user exists” vs “bad credentials”).

**Modules:** [04-security](../04-security), [07-microservices](../07-microservices) (Gateway).

---

## 4. Observability

- **Logging:** Structured (JSON/key=value); trace ID and span ID for correlation; levels and volume; no PII or secrets.
- **Metrics:** What to measure (latency, errors, business counters); bounded vs unbounded dimensions; circuit breaker and pool metrics. Export (Prometheus, etc.) and alerting.
- **Tracing:** Why tracing (request path across services); propagation (traceparent); sampling; no PII in spans. Trade-off: overhead vs debuggability.
- **Health:** Liveness (process alive) vs readiness (ready for traffic); use readiness to stop traffic when dependencies are down.

**Modules:** [05-performance-and-caching](../05-performance-and-caching), [07-microservices](../07-microservices), [08-deployment](../08-deployment).

---

## 5. Resilience and design

- **Circuit breaker:** Purpose (avoid cascading failure); states (Closed, Open, HalfOpen); threshold and wait duration; fallback or fast-fail. When to use retry vs circuit breaker (retry first, then circuit counts failures).
- **Timeouts:** Every outbound call (HTTP, DB, cache) should have timeouts; otherwise thread pool exhaustion.
- **Idempotency:** Critical for messaging and retries; business key and “already processed” store; commit offset after side effects (or same TX for exactly-once style).
- **API design:** REST semantics (status codes, methods); versioning strategy; error contract and validation; pagination and rate limiting.

**Modules:** [02-building-rest-api](../02-building-rest-api), [06-messaging](../06-messaging), [07-microservices](../07-microservices).

---

## 6. Spring and JVM

- **IoC and DI:** Inversion of control vs dependency injection; why constructor injection; bean lifecycle (post-processors, @PostConstruct, proxy creation). Singleton scope and thread safety.
- **AOP:** Proxy-based; only external method calls go through proxy; “this” calls bypass advice. Use for cross-cutting (logging, transactions, security).
- **Boot:** Auto-configuration and conditions; starters; embedded server. How to customise and exclude.
- **JVM:** Heap vs stack; why stateless singletons; connection and thread pool sizing relative to load.

**Modules:** [01-core-concepts](../01-core-concepts).

---

## 7. Deployment and operations

- **Docker:** Multi-stage build; minimal image; no secrets in image; non-root user. Layer order for cache.
- **K8s:** Deployment, Service, ConfigMap, Secret; probes and resource limits; rolling update and rollback.
- **CI/CD:** Build → test → image → push → deploy; secrets handling; idempotent deploys and rollback procedure.
- **Production checklist:** Logging, metrics, tracing, security, config, resilience, DB, cache, messaging, deployment (see [PRODUCTION.md](PRODUCTION.md)).

**Modules:** [08-deployment](../08-deployment), [capstone-production-app](.).

---

## How to use this doc

- **Interview prep:** For each theme, be able to explain the concept, trade-offs, and one “common mistake” or pitfall. Use module CONCEPTS and COMMON_MISTAKES for depth.
- **System design:** Combine themes (e.g. “Design an order service that calls catalog, uses cache, and publishes events” → consistency, performance, resilience, observability).
- **Code review:** Use PRODUCTION.md and this doc as a checklist for production readiness and SDE-2 level discussion.
