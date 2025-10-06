````markdown
# 14-api-documentation (OpenAPI) — Challenge 1 (springdoc minimal → precise)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.web)`
  - `implementation(libs.spring.boot.starter.validation)`
  - `implementation(libs.springdoc.openapi.starter.webmvc.ui)`  ← springdoc v2
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")`
  - `implementation("org.springframework.boot:spring-boot-starter-validation:3.3.4")`
  - `implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")`

> Default docs URLs:
> - OpenAPI JSON: `/v3/api-docs`
> - Swagger UI: `/swagger-ui.html` (and `/swagger-ui/index.html`)  
> (springdoc v2 with Boot 3).  

---

## Problem A — Turn it on (zero annotations)

### Requirement
- Configure this subproject on port **8080** (e.g., `application.yml`).
- Create a tiny controller with **one GET** (e.g., `/api/ping` returning `{ "ok": true }`).
- **No OpenAPI annotations yet.** Rely on auto-generation.

### Acceptance criteria
- `GET /v3/api-docs` returns a JSON spec with your `GET /api/ping`.
- `GET /swagger-ui.html` (or `/swagger-ui/index.html`) loads (HTTP 200).

### Suggested Import Path
```kotlin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
````

### Command to verify/run

```bash
./gradlew :14-api-documentation:bootRun

curl -s http://localhost:8080/v3/api-docs | jq '.paths."/api/ping".get'
# expect: null (no annotation yet)

curl -sI http://localhost:8080/swagger-ui.html | grep "200"
```

---

## Problem B — Minimal refinement (@Operation + @ApiResponses)

### Requirement

* Add a **Users** controller with:

  * `GET /api/users/{id}` → returns 200 with a simple `UserResponse` or 404 (throw).
* Add **minimal** docs:

  * `@Operation(summary = "Get user by id")`
  * `@ApiResponses`: 200 and 404 (content type `application/problem+json` for 404 using `ProblemDetail`).

### Acceptance criteria

* `/v3/api-docs` shows:

  * Path `/api/users/{id}` with **200** and **404** responses.
  * Parameter `id` typed `integer` and **required**.
* Swagger UI renders the two responses.

### Suggested Import Path

```kotlin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.*
```

### Command to verify/run

```bash
# check response codes exist in spec
curl -s http://localhost:8080/v3/api-docs | jq '.paths."/api/users/{id}".get.responses | keys'
# expect: ["200","404"]
```

---

## Problem C — POST create → 201 + examples (DTO @Schema)

### Requirement

* Add `POST /api/users` that accepts `CreateUserRequest(name, email)` and returns **201 Created** with `UserResponse`.
* Annotate **lightly**:

  * `@Operation(summary = "Create user")`
  * On DTOs use `@Schema(description, example = "...")` for `name`, `email`, and `id`.
  * `@ApiResponses` for **201**, **400** (Bean Validation) and **409** (email taken), with `ProblemDetail` as schema for 400/409.

### Acceptance criteria

* `/v3/api-docs` shows `requestBody` with your DTO fields and examples.
* Response **201** exists; 400/409 documented with media type `application/problem+json`.

### Suggested Import Path

```kotlin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.http.ResponseEntity
import java.net.URI
```

### Command to verify/run

```bash
# verify 201 documented
curl -s http://localhost:8080/v3/api-docs | jq '.paths."/api/users".post.responses["201"] | keys'

# runtime check
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy Nguyen","email":"huy@example.com"}' | sed -n '1,5p'
# expect: HTTP/1.1 201 + Location header
```

---

## Problem D — Group your docs (users vs orders)

### Requirement

* Add **orders** controller with at least one endpoint (e.g., `POST /api/orders`).
* Configure **two OpenAPI groups** using `GroupedOpenApi`:

  * group `"users"` → paths `/api/users/**`
  * group `"orders"` → paths `/api/orders/**`

### Acceptance criteria

* `/v3/api-docs/users` lists only users endpoints.
* `/v3/api-docs/orders` lists only orders endpoints.
* Swagger UI shows two groups in the top-left selector.

### Suggested Import Path

```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springdoc.core.models.GroupedOpenApi
```

### Command to verify/run

```bash
curl -s http://localhost:8080/v3/api-docs/users | jq '.paths | keys'
curl -s http://localhost:8080/v3/api-docs/orders | jq '.paths | keys'
```

---

## Problem E — Global docs polish (avoid repetition)

### Requirement

* Add a **global OpenAPI bean** with API metadata:

  * Title, version, description at the root (`OpenAPI().info(...)`).
* Add an `OpenApiCustomiser` that ensures **500 Internal error** (Problem Details) is present for **all operations** (if missing).
* Mark one internal endpoint with `@Hidden` so it **doesn’t** appear.

### Acceptance criteria

* `/v3/api-docs` includes `info.title` and `info.version`.
* Any operation missing 500 now shows a `responses["500"]` with `application/problem+json`.
* The `@Hidden` endpoint is **not** listed in paths.

### Suggested Import Path

```kotlin
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomiser
import io.swagger.v3.oas.annotations.Hidden
```

### Command to verify/run

```bash
curl -s http://localhost:8080/v3/api-docs | jq '.info | {title, version}'
# expect your title/version

# pick one path/key and confirm 500 exists
curl -s http://localhost:8080/v3/api-docs | jq '.paths[][].responses["500"].content["application/problem+json"] | type'
# expect: "object"

# ensure hidden path isn't present
curl -s http://localhost:8080/v3/api-docs | jq '.paths | keys' | grep internal && echo "SHOULD NOT SEE"
```
