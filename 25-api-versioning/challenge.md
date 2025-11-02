# Topic 26: API Versioning

### Dependencies

(Already covered by your web starter.)

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

---

### Problem A — Path Versioning (`/api/v1` vs `/api/v2`)

#### Requirement

Expose a **User** read endpoint with two versions, different response shapes:

* `GET /api/v1/users/{id}` → returns:

  ```json
  { "id": 1, "name": "Ada" }
  ```
* `GET /api/v2/users/{id}` → returns:

  ```json
  { "id": 1, "fullName": "Ada Lovelace" }
  ```

Rules:

* In-memory user map; no DB.
* Different controller classes or `@RequestMapping` bases per version.
* Keep HTTP semantics: **GET → 200** or **404** when missing.

#### Acceptance criteria

* Hitting each version returns the **correct shape** and **200**.
* Unknown ID returns **404** (both versions).
* v1 and v2 endpoints **coexist** without mapping conflicts.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
```

#### Command to verify/run

```bash
./gradlew :25-api-versioning:bootRun
curl -s http://localhost:8080/api/v1/users/1
curl -s http://localhost:8080/api/v2/users/1
curl -i http://localhost:8080/api/v1/users/999   # expect 404
```

---

### Problem B — Header Versioning (`X-API-Version`)

#### Requirement

Expose a single URL with **header-based** version selection:

* Endpoint: `GET /api/users/{id}`
* Routing rules:

  * If header `X-API-Version: 1` → return v1 shape `{ "id": ..., "name": ... }`
  * If header `X-API-Version: 2` → return v2 shape `{ "id": ..., "fullName": ... }`
  * If header missing or unsupported → **400 Bad Request** with a small error JSON.

Implementation notes:

* Use **two handler methods** with `@GetMapping(headers = ["X-API-Version=1"])` and `...=2`.
* Share the same in-memory user source as Problem A.

#### Acceptance criteria

* Correct handler chosen strictly by header.
* No header → **400** JSON: `{ "error": "version header required" }`.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
```

#### Command to verify/run

```bash
curl -s -H "X-API-Version: 1" http://localhost:8080/api/users/1
curl -s -H "X-API-Version: 2" http://localhost:8080/api/users/1
curl -i http://localhost:8080/api/users/1               # expect 400
```

---

### Problem C — Media-Type Versioning (Content Negotiation)

#### Requirement

Expose version via **custom media type**:

* Endpoint: `GET /api/profile/{id}`
* Supported `Accept` values:

  * `application/vnd.demo.user.v1+json`
  * `application/vnd.demo.user.v2+json`
* Return body matches v1/v2 shapes as above.
* If `Accept` missing or not supported → **406 Not Acceptable**.

#### Acceptance criteria

* With v1 media type → v1 shape; with v2 → v2 shape.
* Unsupported `Accept` → **406**.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
```

#### Command to verify/run

```bash
curl -s -H "Accept: application/vnd.demo.user.v1+json" http://localhost:8080/api/profile/1
curl -s -H "Accept: application/vnd.demo.user.v2+json" http://localhost:8080/api/profile/1
curl -i -H "Accept: application/vnd.unknown+json" http://localhost:8080/api/profile/1  # 406
```

---

### Problem D — Deprecation Signaling for v1

#### Requirement

Mark **v1** as deprecated and communicate a **sunset date**:

* For every v1 response, add headers:

  * `Deprecation: true`
  * `Sunset: 2026-01-01`
  * `Link: </policy>; rel="deprecation"`
* Keep response body unchanged.

Tip: use a `@ControllerAdvice` or `ResponseBodyAdvice` to add headers for all v1 controllers.

#### Acceptance criteria

* Any `GET /api/v1/...` includes the three headers.
* v2 responses do **not** include these headers.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.http.*
```

#### Command to verify/run

```bash
curl -i http://localhost:8080/api/v1/users/1 | grep -E "Deprecation|Sunset|Link"
curl -i http://localhost:8080/api/v2/users/1 | grep -E "Deprecation|Sunset|Link"  # should show nothing
```

---

### Problem E — Shared Domain, Thin Adapters

#### Requirement

Avoid logic drift: keep **one domain model** and map to v1/v2 DTOs.

* Domain: `User(id: Long, fullName: String)`
* Mappers:

  * `toV1(): UserV1(name = fullName)` → split or trim as needed.
  * `toV2(): UserV2(fullName = fullName)`
* Controllers must use the **same** in-memory repository/service.

#### Acceptance criteria

* Changing domain data updates both versions correctly.
* No duplicated business logic across controllers.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.Service
```

#### Command to verify/run

```bash
# After boot, hit both versions; confirm consistent "id/fullName" reflected as expected
curl -s http://localhost:8080/api/v1/users/1
curl -s http://localhost:8080/api/v2/users/1
```

---

## Notes / Hints

* **Don’t mix schemes** for the *same* endpoint; each problem isolates one approach.
* In Kotlin, prefer data classes for DTOs; keep controllers thin and explicit.
* Return codes per your rules: all GETs → **200** or **404**; **400/406** for negotiation errors; **no redirects**.
