# Topic 22: Tracing

### Dependencies

Add tracing + actuator (logging-only verification; no external backend required):

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    // optional (dev): logs spans to console if you wire it — skip unless you want it
    runtimeOnly("io.opentelemetry:opentelemetry-exporter-logging")
}
```

---

### Problem A — Turn On Tracing & Surface IDs in Logs

#### Requirement

* Configure **100% sampling** and put `traceId`/`spanId` into your log pattern.
* Create `GET /api/ping` that returns `"pong"` (200).

`application.yml` minimal:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

Add a Logback pattern (either in `application.yml` or `logback-spring.xml`) so each line shows IDs, e.g.:

```
%d{HH:mm:ss.SSS} %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n
```

#### Acceptance criteria

* Calling `GET /api/ping` logs one line with **non-empty** `traceId` and `spanId`.
* Response is `200 OK` with body `pong`.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
./gradlew :22-tracing:bootRun
curl -s http://localhost:8080/api/ping
# Check console log: ... [<traceId>,<spanId>] ...
```

---

### Problem B — Manual Child Span with Tags & Error

#### Requirement

* Create `GET /api/report` that:

  * Starts a **child span** named `generate-report`.
  * Adds tags: `report.type=daily`, `rows=<number>`.
  * Simulates work (sleep 200–400 ms).
  * On query `?fail=true`, throw an exception and **record it on the span**.
* Return `200 OK` with a small JSON `{ "status": "ok" }` when success.
* On failure, return `500` (default Spring Boot error is fine).

#### Acceptance criteria

* Success path: logs show the request with **same traceId, new spanId** created for `generate-report`.
* Failure path: logs include the **error** and you still see the **same traceId** for correlation.
* HTTP: `200 OK` on success, `500` when `?fail=true`.

#### Suggested Import Path

```kotlin
import io.micrometer.tracing.Tracer
import io.micrometer.tracing.Span
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -s http://localhost:8080/api/report
curl -s -i "http://localhost:8080/api/report?fail=true"
```

---

### Problem C — Span Composition (Multiple Child Spans)

#### Requirement

* Add `GET /api/compose` that orchestrates two internal steps:

  * `stepOne()` → child span `load-input`
  * `stepTwo()` → child span `compute-output`
* Each step sleeps 100–300 ms and logs one line.
* Controller returns `200 OK` with JSON:

  ```json
  { "steps": ["load-input","compute-output"], "status": "ok" }
  ```

#### Acceptance criteria

* A single **traceId** appears across controller + both steps.
* Each step has a **distinct spanId**; logs show both span names.
* Timer durations differ (due to random sleeps).

#### Suggested Import Path

```kotlin
import io.micrometer.tracing.Tracer
import io.micrometer.tracing.Span
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -s http://localhost:8080/api/compose
# Inspect console: same traceId, different spanIds for 'load-input' and 'compute-output'
```

---

### Problem D — Propagate Trace ID to Clients (Response Header)

#### Requirement

* For any controller response, **echo the current trace ID** in header `X-Trace-Id`.
* Implement via a `HandlerInterceptor` or a `@ControllerAdvice` using `MDC`/`Tracer`.
* Test it on `GET /api/ping`.

#### Acceptance criteria

* `curl -i /api/ping` shows header `X-Trace-Id: <non-empty>`.
* The header value matches the `traceId` printed in logs for that request.

#### Suggested Import Path

```kotlin
import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
```

#### Command to verify/run

```bash
curl -i http://localhost:8080/api/ping | grep X-Trace-Id
# Compare with console logs' traceId
```

---

### Problem E (Optional) — Wire a Console Span Exporter

#### Requirement

* If you want to **see spans printed** (beyond log MDC), wire a simple logging exporter for OpenTelemetry:

  * Define a `SdkTracerProvider` with `LoggingSpanExporter` and register it as a bean.
  * Keep sampling at 1.0.
* Hit any endpoint and observe **structured span begin/end** lines.

#### Acceptance criteria

* Console shows exporter lines (start/finish, attributes, status).
* No external Zipkin/Tempo required.

#### Suggested Import Path

```kotlin
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
```

#### Command to verify/run

```bash
./gradlew bootRun
curl -s http://localhost:8080/api/report
# Observe exporter output in console
```
