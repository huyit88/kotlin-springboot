# 17-external-calls (WebClient) — Challenge 1 (HTTPBin via WebClient + timeouts + error mapping)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.webflux)`        # WebClient lives here
  - `implementation(libs.jackson.kotlin)`
  - `testImplementation(libs.spring.boot.starter.test)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-webflux:3.3.4")`
  - `implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")`
  - `testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")`

> Run this module on **port 8080**.

---

## Problem A — Provide a reusable `WebClient` (baseUrl + timeouts)

#### Requirement
- Create a `@Configuration` that exposes a bean:
  - `WebClient` with `baseUrl = "https://httpbin.org"`.
  - **Connect timeout** = 2000 ms, **response timeout** = 3000 ms.
  - Set `maxInMemorySize` = 2 MB.
- No blocking (`.block()`)—we’ll use **Kotlin coroutines** (`awaitSingle()`).

#### Acceptance criteria
- App starts successfully.
- Hitting `/health` on your app returns 200 (simple stub controller).

#### Suggested Import Path
```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import io.netty.channel.ChannelOption
import java.time.Duration
import org.springframework.web.reactive.function.client.ExchangeStrategies
````

#### Command to verify/run

```bash
./gradlew :17-external-calls:bootRun
curl -i http://localhost:8080/health
```

---

## Problem B — GET relay (query passthrough) using `retrieve()`

#### Requirement

* Create a service with a **suspend** function `getEcho(foo: String): Map<String, Any>` that calls:

  * `GET /get?foo={foo}` on **httpbin** (via your `WebClient`).
  * Return a compact map `{ "foo": "<value-from-args>", "origin": "<httpbin-origin>" }` extracted from the httpbin response (`args.foo`, `origin`).
* Create a controller:

  * `GET /api/ext/echo?foo=abc` → returns the map above.

#### Acceptance criteria

* `GET /api/ext/echo?foo=hi` → **200**, JSON includes `"foo":"hi"` and an `"origin"` string.

#### Suggested Import Path

```kotlin
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlinx.coroutines.reactor.awaitSingle
```

#### Command to verify/run

```bash
curl -s "http://localhost:8080/api/ext/echo?foo=hi" | jq .
```

---

## Problem C — POST JSON (DTO in/out) using `retrieve()`

#### Requirement

* Define:

  * `data class CreateReq(val name: String, val email: String)`
  * `data class CreateOut(val name: String, val email: String, val echoed: Boolean)`
* Service suspend function `create(req: CreateReq): CreateOut`:

  * Call `POST /post` with JSON body `{name,email}`.
  * From httpbin’s response, set:

    * `name` and `email` from the sent JSON (`json.name`, `json.email`),
    * `echoed = true` if httpbin echoed the JSON (non-null).
* Controller:

  * `POST /api/ext/users` → returns `201 Created` + the `CreateOut` body.

#### Acceptance criteria

* `POST` with a JSON body returns **201** and `{"name":"...","email":"...","echoed":true}`.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.http.ResponseEntity
import java.net.URI
```

#### Command to verify/run

```bash
curl -i -X POST http://localhost:8080/api/ext/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy","email":"huy@example.com"}'
```

---

## Problem D — Map HTTP errors → domain exceptions (404/5xx)

#### Requirement

* Add domain errors:

  * `class RemoteNotFound(path: String) : RuntimeException("Remote 404 at $path")`
  * `class RemoteFailure(status: Int) : RuntimeException("Remote error $status")`
* Implement a suspend function `getStatus(code: Int): String` that:

  * Calls `GET /status/{code}`.
  * If **200** → return `"OK"`.
  * If **404** → throw `RemoteNotFound("/status/$code")`.
  * If **>= 500** → throw `RemoteFailure(code)`.
  * Otherwise, throw `IllegalStateException("Unhandled $code")`.
  * Use `exchangeToMono` (fine-grained status handling).
* Controller:

  * `GET /api/ext/status/{code}`:

    * Returns **200** `{ "result": "OK" }` for 200.
    * Returns your **Problem Details** JSON for errors (reuse global error advice if present or return `ResponseEntity<ProblemDetail>` manually).

#### Acceptance criteria

* `GET /api/ext/status/200` → **200** `{"result":"OK"}`
* `GET /api/ext/status/404` → **404** `application/problem+json` with a `detail` mentioning 404.
* `GET /api/ext/status/503` → **502/500** (your choice) with problem+json and `detail` mentioning 503.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import io.swagger.v3.oas.annotations.Hidden // if you choose to hide helper endpoints
```

#### Command to verify/run

```bash
curl -i http://localhost:8080/api/ext/status/200
curl -i http://localhost:8080/api/ext/status/404
curl -i http://localhost:8080/api/ext/status/503
```

---

## Problem E — Timeout handling (map to 504)

#### Requirement

* Add suspend function `getSlow(seconds: Long): String` that calls `GET /delay/{seconds}`.

  * Keep your client’s **response timeout = 3s**.
  * For `seconds > 3`, catch the timeout (`reactor.core.Exceptions` / `TimeoutException`) and convert to `ProblemDetail` with **504 Gateway Timeout** in controller.
* Controller:

  * `GET /api/ext/slow?sec=5` → should return **504 problem+json** (due to response timeout).
  * `GET /api/ext/slow?sec=1` → should return **200** with `"ok"` (or any simple string).

#### Acceptance criteria

* `sec=5` → **504** `application/problem+json` with a short “upstream timeout” detail.
* `sec=1` → **200**.

#### Suggested Import Path

```kotlin
import java.util.concurrent.TimeoutException
import org.springframework.http.MediaType
```

#### Command to verify/run

```bash
# 504 due to client-side response timeout (3s)
curl -i "http://localhost:8080/api/ext/slow?sec=5"

# 200 fast path
curl -i "http://localhost:8080/api/ext/slow?sec=1"
```

---

### Notes

* Use **coroutines** (`awaitSingle()`) for clarity; no `.block()`.
* Use `retrieve()` for quick success paths; `exchangeToMono` for precise status handling.
* Keep error mapping **inside the client/service layer**, so controllers remain thin.
* httpbin endpoints used:

  * `GET /get?foo=...`, `POST /post`, `GET /status/{code}`, `GET /delay/{n}`.
