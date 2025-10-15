# Topic 18: Caching

### Dependencies

Add to your module’s `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}
```

---

### Problem A — Basic Caching with Caffeine

#### Requirement

Create an in-memory caching layer for a simple **CurrencyService** that converts an amount between two currencies.

* Endpoint: `GET /api/rates/{from}/{to}`
* When called, the service should **simulate an expensive computation** (e.g., `Thread.sleep(1000)`) before returning a conversion rate.
* Use `@Cacheable("rates")` to cache based on `{from,to}` pair.
* Include a simple Caffeine config bean with max size = 100 and TTL = 5 minutes.
* Return a JSON like:

  ```json
  { "from": "USD", "to": "EUR", "rate": 0.91 }
  ```

#### Acceptance criteria

* First call takes ~1s, subsequent calls with same pair return instantly (<100ms).
* Changing either path variable causes recomputation.
* Restarting the app clears cache.
* Expected status: `200 OK`.

#### Suggested Import Path

```kotlin
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.web.bind.annotation.*
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
```

#### Command to verify/run

```bash
./gradlew bootRun
# First call (slow)
time curl -s http://localhost:8080/api/rates/USD/EUR
# Second call (fast)
time curl -s http://localhost:8080/api/rates/USD/EUR
```

---

### Problem B — Cache Update and Eviction

#### Requirement

Extend Problem A:

* Add:

  * `PUT /api/rates/{from}/{to}?rate=...` — updates the rate (always recalculates and overwrites cache).
  * `DELETE /api/rates/{from}/{to}` — removes cached value (idempotent).
* Use:

  * `@CachePut("rates")` on update.
  * `@CacheEvict("rates")` on delete.
* Return:

  * `PUT` → `200 OK` with new rate JSON.
  * `DELETE` → `204 No Content`.

#### Acceptance criteria

* `PUT` always executes regardless of cache state.
* `DELETE` removes cache entry so next GET recomputes.
* `DELETE` is idempotent (second delete returns 204 again).
* Cache entries for other currency pairs remain unaffected.

#### Suggested Import Path

```kotlin
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -X PUT 'http://localhost:8080/api/rates/USD/EUR?rate=0.95' -i
curl -X DELETE 'http://localhost:8080/api/rates/USD/EUR' -i
```

---

### Problem C — Conditional and Synchronized Cache Loading

#### Requirement

Enhance service to:

* Skip caching when rate result is `null`.
* Synchronize concurrent loads (avoid stampede) using `sync = true`.
* Add `/api/rates/random` returning a random rate between 0.5–1.5 and cache it under key `"random"`.

#### Acceptance criteria

* When rate computation returns `null`, no cache entry created.
* Multiple parallel GET `/api/rates/random` calls trigger only one actual computation.
* TTL expiry (5min) verified via logs (optional printout).

#### Suggested Import Path

```kotlin
import org.springframework.cache.annotation.Cacheable
import kotlin.random.Random
```

#### Command to verify/run

```bash
ab -n 10 -c 10 http://localhost:8080/api/rates/random
```

---

### Problem D — Programmatic Cache Warm-up

#### Requirement

Add a component that preloads (warms) the cache with known pairs on startup:

* Use `ApplicationRunner`.
* Insert rates for `USD/EUR`, `EUR/CHF`, `JPY/USD` via `cacheManager.getCache("rates")?.put(key, value)`.

#### Acceptance criteria

* App startup logs show “Preloaded cache for USD/EUR …”.
* First call for preloaded pairs returns instantly.
* CacheManager used programmatically.

#### Suggested Import Path

```kotlin
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.cache.CacheManager
```

#### Command to verify/run

```bash
./gradlew bootRun
curl -s http://localhost:8080/api/rates/USD/EUR  # instant response
```
