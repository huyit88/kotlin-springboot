# Topic 23: Resilience (Resilience4j)

### Dependencies

Add (per-module) only what’s new for this topic:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
}
```

---

### Problem A — Circuit Breaker with Fallback

#### Requirement

Create a flaky service and protect it with a **Circuit Breaker**:

* Service `FlakyWeatherService.get(city: String): String`

  * 60% chance to throw `RuntimeException("remote failure")`.
* Controller `GET /api/weather/{city}` → returns:

  * `200 OK` with `"Sunny in <city>"` when success,
  * or **fallback** string `"Weather unavailable for <city>"` when breaker **OPEN** or call fails.
* Configure a **tiny window** so you can see it open quickly:

  * sliding window **COUNT_BASED**, size **4**
  * failure-rate-threshold **50%**
  * open wait duration **5s**
  * half-open permits **2**.

#### Acceptance criteria

* Within ~4–6 requests (due to randomness), the breaker **opens**; subsequent calls return the **fallback immediately** (fast).
* After ~5s, breaker **half-opens** and allows a couple trial calls; on success it **closes**.

#### Suggested Import Path

```kotlin
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.Service
```

#### Command to verify/run

```bash
./gradlew 23-resilience:bootRun
# Hit several times; observe fallback after threshold
for i in {1..12}; do curl -s http://localhost:8080/api/weather/berlin; echo; done
# Wait 6s, then try again to see half-open -> close
sleep 6
for i in {1..4}; do curl -s http://localhost:8080/api/weather/berlin; echo; done
```

---

### Problem B — Retry with Exponential Backoff (idempotent read)

#### Requirement

Protect an idempotent method with **Retry**:

* Service `CatalogService.fetchSku(sku: String): String`

  * Throws `java.util.concurrent.TimeoutException("slow backend")` for the first N calls (simulate), then succeeds.
* Controller `GET /api/sku/{id}` returns `200 OK` with `"SKU:<id>"` on success.
* Annotate with `@Retry(name = "catalog", fallbackMethod = "fallbackSku")`.
* Config:

  * `max-attempts: 5` (initial + 4 retries)
  * `wait-duration: 200ms`
  * `exponential-backoff-multiplier: 2.0` (200ms, 400ms, 800ms, 1600ms)
  * Retry only on `TimeoutException`.

#### Acceptance criteria

* When the service fails N≤4 times → endpoint eventually returns **200** after retries.
* When it fails >4 times → fallback is returned with **200** body `"SKU not available, try later"` (we keep it a read-only endpoint; choose status 200 for simplicity here).

#### Suggested Import Path

```kotlin
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeoutException
```

#### Command to verify/run

```bash
# First call triggers retries internally; total latency reflects backoff
time curl -s http://localhost:8080/api/sku/ABC-123
```

---

### Problem C — TimeLimiter + CircuitBreaker (async) with Fallback

#### Requirement

Expose an async job protected by **TimeLimiter** + **CircuitBreaker**:

* Service method returns `CompletableFuture<String>` and sleeps **2s** (simulate).
* Time limiter timeout **1s**; on timeout call fallback returning `"fallback: timed out"`.
* Controller `GET /api/report/async` returns **200** with the resolved string.

> Use annotations: `@TimeLimiter(name="report", fallbackMethod="reportFallback")` and `@CircuitBreaker(name="report")`.

#### Acceptance criteria

* The request **completes quickly** with `"fallback: timed out"` (since job exceeds 1s).
* If you reduce sleep to <1s and call again, it returns `"report ready"`.

#### Suggested Import Path

```kotlin
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import java.util.concurrent.CompletableFuture
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -s http://localhost:8080/api/report/async
for i in {1..12}; do  curl -s http://localhost:8080/api/report/async ; echo; done;

```

---

### Problem D — Bulkhead (Semaphore) to Limit Concurrency

#### Requirement

Guard a blocking endpoint with a **Semaphore Bulkhead**:

* Service `ThumbnailService.render(id: String): String` sleeps **500ms** and returns `"ok:<id>"`.
* Annotate with `@Bulkhead(name = "thumb", type = Bulkhead.Type.SEMAPHORE)`.
* Config: `max-concurrent-calls: 2`, `max-wait-duration: 0` (fail fast when saturated).
* Controller `GET /api/thumb/{id}` returns `200 OK` on success; on bulkhead rejection return **429 Too Many Requests** with body `"busy"` (map the exception).

#### Acceptance criteria

* With concurrency 4, only ~2 in-flight calls succeed; others are **rejected immediately** and mapped to **429**.
* Single-threaded calls all succeed.

#### Suggested Import Path

```kotlin
import io.github.resilience4j.bulkhead.annotation.Bulkhead
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
```

#### Command to verify/run

```bash
# Install ApacheBench or use xargs to simulate concurrency
ab -n 10 -c 4 http://localhost:8080/api/thumb/1
# or:
seq 10 | xargs -n1 -P4 -I{} curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/thumb/{}
```

---

## Minimal config (put in `src/main/resources/application.yml`)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      weather:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 4
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 2
        automatic-transition-from-open-to-half-open-enabled: true

  retry:
    instances:
      catalog:
        max-attempts: 5
        wait-duration: 200ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2.0
        retry-exceptions:
          - java.util.concurrent.TimeoutException

  timelimiter:
    instances:
      report:
        timeout-duration: 1s

  bulkhead:
    instances:
      thumb:
        max-concurrent-calls: 2
        max-wait-duration: 0
```

---

## Suggested Import Path (one-time, top of files)

```kotlin
import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
```

---

## Notes / Hints

* **Fallback signatures must match**:

  * Sync method → `fun fallback(arg1: A1, ex: Throwable): T` or just `(ex: Throwable)`.
  * Async method → must return `CompletableFuture<T>`.
* For **Problem D (429)**, add an `@ExceptionHandler(BulkheadFullException::class)` in the controller (or `@ControllerAdvice`) to map to 429.
* Keep everything **in-memory**; no DBs or external APIs needed. Random failures simulate dependencies.
