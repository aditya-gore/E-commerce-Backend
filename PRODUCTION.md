# Capstone — Production Readiness Checklist

Master checklist for a production-ready ScaleCart-style backend. Each area can be deepened in the referenced module. **The capstone-production-app in this directory is the reference implementation** for these checks.

## Logging

- [ ] **Structured logging:** JSON or key=value; include trace ID and span ID for correlation (see [07-microservices](../07-microservices)).
- [ ] **Levels:** INFO in production; DEBUG only when needed; no sensitive data (passwords, tokens, PII) in logs.
- [ ] **Volume:** Avoid logging full request/response bodies; log IDs and status. Use sampling for high-throughput endpoints if necessary.
- [ ] **Rotation and retention:** Log files or log shipping (e.g. to central store) with retention and access control.

**Ref:** [07-microservices/CONCEPTS.md](../07-microservices/CONCEPTS.md) (Observability), [07-microservices/PRODUCTION.md](../07-microservices/PRODUCTION.md).

---

## Metrics

- [ ] **Application metrics:** Latency (e.g. `http.server.requests`), error count, business counters (e.g. orders created). Use Micrometer; tag with bounded dimensions (endpoint, status), not unbounded (e.g. order ID).
- [ ] **Infrastructure:** CPU, memory, thread pool, connection pool (DB, Redis). Expose via Actuator or custom metrics.
- [ ] **Circuit breaker and resilience:** Expose Resilience4j metrics (state, call count, failure rate) for alerting.
- [ ] **Export:** Prometheus scrape or push; dashboards and alerts configured.

**Ref:** [05-performance-and-caching](../05-performance-and-caching), [07-microservices](../07-microservices).

---

## Distributed tracing

- [ ] **Tracing enabled:** OpenTelemetry or Sleuth; trace ID propagated across services (e.g. RestClient, Kafka).
- [ ] **Sampling:** Configure sampling rate in production to control cost and overhead.
- [ ] **No PII in spans:** Span names and attributes must not contain user identifiers or secrets.
- [ ] **Backend:** Traces exported to Jaeger, Zipkin, or OTLP collector; retention and access defined.

**Ref:** [07-microservices/CONCEPTS.md](../07-microservices/CONCEPTS.md) (Trace propagation).

---

## Security

- [ ] **Authentication:** JWT or OAuth2 RS; short-lived access tokens; refresh or re-login strategy. Keys and secrets in env or secret manager, not in code.
- [ ] **Authorization:** Role/permission checks on APIs and methods (@PreAuthorize); principle of least privilege.
- [ ] **Passwords:** BCrypt or Argon2; never logged or stored in plain text.
- [ ] **HTTPS only** in production; CORS and security headers (CSP, X-Content-Type-Options) configured.
- [ ] **Secrets:** No default credentials; rotation and revocation process documented.

**Ref:** [04-security](../04-security).

---

## Configuration

- [ ] **Externalised:** All environment-specific and secret config via env, Config Server, or secret manager. No hardcoded URLs or credentials.
- [ ] **Profiles:** Use `spring.profiles.active` (e.g. prod, staging); separate config per profile.
- [ ] **Refresh:** If using Config Server, @RefreshScope and refresh strategy documented; no restart for non-sensitive changes where appropriate.

**Ref:** [07-microservices](../07-microservices) (Config Server).

---

## Resilience

- [ ] **Timeouts:** Connect and read timeouts set on all outbound HTTP (RestClient/WebClient). Timeouts on DB and cache where supported.
- [ ] **Circuit breaker:** On calls to other services; fallback or fast-fail when open. Tuned for failure threshold and wait duration.
- [ ] **Retries:** Bounded retries with backoff for idempotent or safely retriable operations; avoid unbounded retries.
- [ ] **Health and readiness:** Liveness and readiness probes (K8s or Docker); readiness fails when dependencies (DB, cache) are down.

**Ref:** [05-performance-and-caching](../05-performance-and-caching), [06-messaging](../06-messaging), [07-microservices](../07-microservices), [08-deployment](../08-deployment).

---

## Database

- [ ] **Connection pool:** Sized appropriately (e.g. HikariCP); not exceeding DB max connections. Pool metrics monitored.
- [ ] **Transactions:** Correct boundaries (service layer); no long-running TX with I/O inside. N+1 avoided (fetch join, entity graph, or batch).
- [ ] **Locking:** Optimistic or pessimistic locking where concurrent updates matter; no lost updates.
- [ ] **Migrations:** Schema managed with Flyway/Liquibase; no manual DDL in production.
- [ ] **Backup and recovery:** Backups and restore tested; RTO/RPO documented.

**Ref:** [03-database-and-transactions](../03-database-and-transactions).

---

## Cache

- [ ] **Keys and eviction:** Explicit cache keys; eviction on update/delete. TTL set where appropriate.
- [ ] **Serialization:** Cached values serializable; no lazy entity references in distributed cache.
- [ ] **Failure mode:** Behaviour when Redis (or provider) is down is defined and tested (e.g. cache miss, optional fallback).
- [ ] **Metrics:** Hit rate and eviction monitored; alert on abnormal drop or errors.

**Ref:** [05-performance-and-caching](../05-performance-and-caching).

---

## Messaging (Kafka)

- [ ] **Idempotency:** Consumers check “already processed” (e.g. by business key) before applying side effects; store processed keys.
- [ ] **Offset commit:** Commit after processing (or in same transaction as state) to avoid double process on replay.
- [ ] **Retries and DLT:** Retry with backoff; dead-letter topic for poison messages; no infinite retry blocking partition.
- [ ] **Ordering and keys:** Message key used for partitioning and idempotency; same key → same partition.
- [ ] **Monitoring:** Lag, consumer group, and send failures monitored and alerted.

**Ref:** [06-messaging](../06-messaging).

---

## Deployment and CI/CD

- [ ] **Containers:** Multi-stage Docker build; minimal base image; no secrets in image; non-root user where possible.
- [ ] **Orchestration:** K8s (or equivalent) with resource limits, probes, ConfigMap/Secret. Rollback procedure documented.
- [ ] **CI/CD:** Build, test, image build, push to registry, and deploy automated. No secrets in logs or code; image scanning considered.
- [ ] **Rolling updates:** Zero-downtime deploy strategy; health checks and readiness used correctly.

**Ref:** [08-deployment](../08-deployment).

---

## Summary

Production readiness spans **observability** (logging, metrics, tracing), **security** (auth, secrets, HTTPS), **resilience** (timeouts, circuit breaker, retries, health), **data** (DB and cache correctness and performance), **messaging** (idempotency, DLT, monitoring), and **deployment** (containers, K8s, CI/CD). Use this checklist and the linked modules for implementation and review.
