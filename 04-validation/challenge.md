# 04-validation — Challenge 1 (Problem Set)

## Problem A — Request DTO validation (POST)

**Goal**
Reject bad input early using Bean Validation on a request body.

**Requirements**

* Endpoint: `POST /api/users`
* Request DTO fields:

  * `name: String` — **@NotBlank**
  * `email: String` — **@Email**
* Behavior:

  * On valid input: create **in-memory** user with incremental `id: Long`, return **201 Created** and `Location: /api/users/{id}`, body: `{"id":<id>,"name":"...","email":"..."}`.
  * On invalid input: return **400 Bad Request** with a JSON body containing field errors (use Spring’s default; don’t customize yet).

**Constraints**

* Use `@Valid` on the controller parameter and `@field:` target in Kotlin data class.
* No DB; store in a simple in-memory map/list.
* Constructor injection only.

**Files to create/update**

* `04-validation/src/main/kotlin/.../api/UserController.kt` — controller with `create()` action.
* `04-validation/src/main/kotlin/.../api/dto/CreateUserReq.kt` — DTO with annotations.
* `04-validation/src/main/kotlin/.../core/UserService.kt` — in-memory create logic.

**Verification**

```bash
# valid
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy Nguyen","email":"huy@example.com"}'
# expect: 201, Location header set, JSON with id/name/email

# invalid: blank name
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"   ","email":"huy@example.com"}'
# expect: 400; body mentions "name" and "must not be blank"

# invalid: bad email
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy","email":"not-an-email"}'
# expect: 400; body mentions "email" and "must be a well-formed email address"
```

**Acceptance criteria**

* Valid request → **201** with `Location` and JSON body containing generated `id`.
* Invalid name → **400** with an error mentioning `name`.
* Invalid email → **400** with an error mentioning `email`.
* No field/setter injection; only constructor injection used.

---

## Problem B — Query & path parameter validation (GET)

**Goal**
Validate simple scalars coming via query/path parameters.

**Requirements**

* Endpoints:

  * `GET /api/math/square?n={n}` — returns `{"result": n*n}`; **@Positive** on `n`.
  * `GET /api/users/{id}` — **@Min(1)** on `id`; return `200` with the user if exists in the in-memory store, else **404**.
* Behavior:

  * If validation fails (negative/zero `n`, or `id < 1`), return **400** (default Spring error).

**Constraints**

* Annotate parameters directly in the controller method(s).
* Do not implement pagination or extra query features.

**Files to create/update**

* `04-validation/src/main/kotlin/.../api/MathController.kt` — `square()` action.
* `04-validation/src/main/kotlin/.../api/UserController.kt` — add `getById()` action.

**Verification**

```bash
# ok
curl -s "http://localhost:8080/api/math/square?n=5"
# expect: {"result":25}

# invalid n (0)
curl -i -s "http://localhost:8080/api/math/square?n=0"
# expect: 400; body mentions "must be greater than 0"

# ok find-by-id (assuming id 1 exists from Problem A)
curl -i -s "http://localhost:8080/api/users/1"
# expect: 200; JSON user

# invalid id (0)
curl -i -s "http://localhost:8080/api/users/0"
# expect: 400; mentions "must be greater than or equal to 1"
```

**Acceptance criteria**

* `square` with positive `n` → **200** and correct math.
* `square` with `n <= 0` → **400**.
* `getById` with `id >= 1`:

  * existing user → **200** JSON,
  * missing → **404** (empty body or minimal message).
* `getById` with `id < 1` → **400**.

---

## Problem C — Nested & collection validation (POST)

**Goal**
Validate nested objects and collections with element constraints.

**Requirements**

* Endpoint: `POST /api/groups`
* Request DTO:

  ```kotlin
  data class CreateGroupReq(
    @field:NotBlank val name: String,
    @field:Valid val owner: OwnerDto,
    @field:Size(min = 1) val members: List<@Email String>
  )
  data class OwnerDto(
    @field:NotBlank val name: String,
    @field:Email val email: String
  )
  ```
* Behavior:

  * Valid: create an in-memory group (`id`, `name`, `owner`, `members`) and return **201** with `Location`.
  * Invalid: **400**; body lists the first-level or nested field(s) that failed (e.g., `owner.email`, `members[0]`).

**Constraints**

* Use `@Valid` on the DTO parameter to cascade into `owner`.
* Use element constraint for `members` list.
* Keep in-memory store simple.

**Files to create/update**

* `04-validation/src/main/kotlin/.../api/GroupController.kt` — `create()` action.
* `04-validation/src/main/kotlin/.../api/dto/CreateGroupReq.kt` — DTOs.
* `04-validation/src/main/kotlin/.../core/GroupService.kt` — in-memory create logic.

**Verification**

```bash
# valid
curl -i -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"team-1","owner":{"name":"Huy","email":"huy@example.com"},"members":["a@example.com","b@example.com"]}'
# expect: 201 with Location and group JSON

# invalid: empty members
curl -i -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"team-1","owner":{"name":"Huy","email":"huy@example.com"},"members":[]}'
# expect: 400; mentions "size must be at least 1"

# invalid: bad owner email
curl -i -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"team-1","owner":{"name":"Huy","email":"bad"},"members":["a@example.com"]}'
# expect: 400; mentions "owner.email"
```

**Acceptance criteria**

* Valid group creation → **201** with `Location` and correct JSON body.
* Empty `members` → **400** with a message referencing the list size.
* Invalid nested field (e.g., `owner.email`) → **400** with path-like reference.

---

### Commands (for all problems)

```bash
./gradlew :04-validation:bootRun
```

### Notes

* In Kotlin, remember `@field:` on DTO properties (e.g., `@field:NotBlank`).
* Use `@Valid` on controller parameters to trigger validation of DTOs (and nested).
* Do **not** customize error responses yet; rely on Spring’s defaults (global error handling is a later topic).
