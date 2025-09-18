# 09-dto-mapping — Challenge 1 (Explicit Mappers + Cross-feature Port)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.web)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")`

---

### Problem A — Don’t expose entities; map to response DTO

#### Requirement
- Create domain model `User(id: Long?, name: String, email: String)` (no framework annotations).
- Create response DTO `UserResponse(id: Long, name: String, email: String)`.
- Create mapper with **pure** functions:
  - `fun User.toResponse(): UserResponse` (require non-null `id`).
- Implement read endpoint:
  - `GET /api/users/{id}` → **200** with `UserResponse` if found; **404** if missing.
- Use an **in-memory** repository/service to keep scope pure mapping (no JPA here).

#### Acceptance criteria
- Endpoint returns `{"id":<n>,"name":"...","email":"..."}` (no extra fields).
- No JPA or persistence annotations in `User` or DTO.
- Mapper contains all conversion logic (controller doesn’t build DTOs inline).

#### Suggested Import Path
```kotlin
// Web
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// Mapper (top-level functions in same package are fine)
````

#### Command to verify/run

```bash
./gradlew :09-dto-mapping:bootRun

# Seed your in-memory store in app init (e.g., user id=1).
curl -s -i http://localhost:8080/api/users/1
# expect: HTTP/1.1 200  +  {"id":1,"name":"Huy","email":"huy@example.com"}

curl -s -i http://localhost:8080/api/users/999
# expect: HTTP/1.1 404
```

---

### Problem B — Request DTO → domain + PUT replace (strict)

#### Requirement

* Create request DTO `UpdateUserPutRequest(name: String, email: String)`.
* Add mapper: `fun UpdateUserPutRequest.toDomain(existing: User): User` that returns a **strict replace** (name & email set from request).
* Implement `PUT /api/users/{id}`:

  * **404** if user missing.
  * **200** with `UserResponse` after replace.

#### Acceptance criteria

* Controller never accepts/returns the domain `User` directly.
* Mapping is performed via the functions (no inline field juggling in controller).
* PUT replaces both fields; no partials.

#### Suggested Import Path

```kotlin
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -s -i -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy N.","email":"huy@example.com"}'
# expect: HTTP/1.1 200 + {"id":1,"name":"Huy N.","email":"huy@example.com"}
```

---

### Problem C — PATCH with nullable-field DTO (partial update)

#### Requirement

* Create `UpdateUserPatchRequest(name: String? = null, email: String? = null)`.
* Add domain extension:

  * `fun User.applyPatch(p: UpdateUserPatchRequest): User` (ignore nulls; keep existing values).
  * If payload is **both null**, return **400** (empty patch not allowed).
* Implement `PATCH /api/users/{id}`:

  * **404** if missing.
  * **400** when empty patch.
  * **200** with `UserResponse` after partial update.

#### Acceptance criteria

* Nullable-field PATCH works as specified.
* Controller returns correct HTTP codes (200/400/404) per rules.
* Mapping logic is centralized (no field-by-field in controller).

#### Suggested Import Path

```kotlin
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
# empty patch -> 400
curl -s -i -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" -d '{}'
# expect: HTTP/1.1 400

# partial patch -> 200
curl -s -i -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" -d '{"name":"Huy Updated"}'
# expect: HTTP/1.1 200 + {"id":1,"name":"Huy Updated","email":"huy@example.com"}
```

---

### Problem D — Cross-feature access via port (no entities/repositories leaked)

#### Requirement

* In `users` package, expose a **read port** and DTO:

  * `data class UserSummary(id: Long, name: String, email: String)`
  * `interface UserReadPort { fun getSummary(userId: Long): UserSummary? }`
  * Implement `UserFacade` that adapts your in-memory service to `UserReadPort`.
* In a new feature `orders`, create minimal API:

  * `POST /api/orders` with body `{"userId": <long>, "item": "<str>"}`.
  * Handler retrieves `UserSummary` via `UserReadPort`:

    * If summary missing → **404**.
    * Else return **201** with `{"orderId":1,"user":{"id":...,"name":"..."}}` (do **not** include user email if you don’t need it).
  * Keep orders in memory.

#### Acceptance criteria

* `orders` layer **only** depends on `UserReadPort` and `UserSummary`.
* `orders` does **not** import/see:

  * `User` domain,
  * users’ in-memory repository/service types,
  * any users’ web DTOs.
* `POST /api/orders` returns **201** with Location `/api/orders/{id}`.

#### Suggested Import Path

```kotlin
// Port & facade live under com.example.users.*
import org.springframework.stereotype.Service

// Orders controller
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
```

#### Command to verify/run

```bash
# assuming user id=1 exists in your in-memory users
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"item":"Book"}'
# expect: HTTP/1.1 201
# Location: /api/orders/1
# body: {"orderId":1,"user":{"id":1,"name":"Huy"},"item":"Book"}

# missing user -> 404
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":999,"item":"Book"}'
# expect: 404
```

---

### Problem E — List/collection mapping helper

#### Requirement

* Add collection mapper:

  * `fun List<User>.toResponses(): List<UserResponse>`
* Implement `GET /api/users` that returns an array of responses using the collection mapper.
* The controller must **not** map each item inline; it calls the helper.

#### Acceptance criteria

* `GET /api/users` returns `[]` or a list of `UserResponse`.
* Collection mapping is used (single call), not manual `map{}` inside the controller.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -s http://localhost:8080/api/users
# expect: [{"id":1,"name":"Huy","email":"huy@example.com"}, ...]
```
