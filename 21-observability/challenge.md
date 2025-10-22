# Topic 21: Observability

---

### Dependencies

Add the following to enable metrics and actuator endpoints:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

### Problem A — Health & Info Endpoints

#### Requirement

* Enable and expose:

  * `/actuator/health`
  * `/actuator/info`
* Add custom info properties in `application.yml`:

  ```yaml
  info:
    app:
      name: observability-demo
      version: 1.0.0
  management:
    endpoints:
      web:
        exposure:
          include: health,info
  ```

#### Acceptance criteria

* `GET /actuator/health` → returns `"status": "UP"`.
* `GET /actuator/info` → returns `"app.name"` and `"app.version"`.
* Changing config reflects instantly after restart.

#### Suggested Import Path

```kotlin
import org.springframework.boot.actuate.health.*
import org.springframework.context.annotation.*
```

#### Command to verify/run

```bash
./gradlew bootRun
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/info
```

---

### Problem B — Custom Health Indicator

#### Requirement

Create a health check for a simulated cache component.

* Bean `CacheHealthIndicator` implements `HealthIndicator`.
* Randomly returns UP or DOWN (simulate flaky service).
* Include details like `"hits"`, `"misses"`.

#### Acceptance criteria

* `/actuator/health` shows `"cache": {"status": "UP|DOWN"}`.
* Aggregated `"status"` reflects cache state (DOWN when cache DOWN).

#### Suggested Import Path

```kotlin
import org.springframework.boot.actuate.health.*
import org.springframework.stereotype.*
```

#### Command to verify/run

```bash
watch -n 1 curl -s http://localhost:8080/actuator/health | jq
```

---

### Problem C — Custom Metric Counter

#### Requirement

Track order creation events.

* Inject `MeterRegistry`.
* Increment a counter `"orders.created"` inside a service method.
* Expose metrics endpoint `/actuator/metrics/orders.created`.

#### Acceptance criteria

* Counter increases on each `POST /api/orders`.
* `curl /actuator/metrics/orders.created` shows the new count.
* Works with Prometheus registry (enabled via Actuator).

#### Suggested Import Path

```kotlin
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -X POST http://localhost:8080/api/orders
curl -s http://localhost:8080/actuator/metrics/orders.created | jq
```

---

### Problem D — Timed Method (Performance Metric)

#### Requirement

Measure execution time of a simulated “search” operation.

* Use `@Timed("search.latency")` on `search()` method.
* `GET /api/search?q=abc` → returns mock list.
* View timer in `/actuator/metrics/search.latency`.

#### Acceptance criteria

* Each call increases timer count.
* Metrics include `mean`, `max`, and `totalTime`.
* Simulate variable delay (100–500ms) to observe changing values.

#### Suggested Import Path

```kotlin
import io.micrometer.core.annotation.Timed
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
for i in {1..5}; do curl -s "http://localhost:8080/api/search?q=abc"; done
curl -s http://localhost:8080/actuator/metrics/search.latency | jq
```

---

### Problem E — Structured Logging

#### Requirement

Add a controller that logs business events with parameters.

* Endpoint: `POST /api/payments`
* Log structure:

  ```
  payment_created id=123 amount=99.50 status=OK
  ```
* Use parameterized logging (`log.info("payment_created id={} amount={}", id, amount)`).

#### Acceptance criteria

* Logs appear in console with proper values.
* No string concatenation in log statements.
* Each log line includes timestamp and thread name.

#### Suggested Import Path

```kotlin
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -X POST -d 'id=123&amount=99.50' http://localhost:8080/api/payments
```
