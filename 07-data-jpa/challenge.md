````markdown
# 07-data-jpa — Challenge 1 (CRUD + Derived Queries with H2)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.data.jpa)`
  - `runtimeOnly(libs.h2)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")`
  - `runtimeOnly("com.h2database:h2:2.2.224")`

---

### Problem A — JPA setup with H2 & basic read

#### Requirement
- Configure **H2 in-memory** DB and JPA:
  - `spring.datasource.url=jdbc:h2:mem:appdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`
  - `spring.jpa.hibernate.ddl-auto=update` (**dev only**)
  - `spring.jpa.open-in-view=false`
- Create an `@Entity` `UserEntity` mapped to table `users` with columns:
  - `id: Long?` — primary key, generated (IDENTITY)
  - `name: String` — not null
  - `email: String` — not null, unique (DB-level)
- Create `UserRepository : JpaRepository<UserEntity, Long>`.
- Add `GET /api/users/{id}` → `200` with JSON if found, else `404`.

#### Acceptance criteria 
- App starts; Hibernate creates `users` table.
- `GET /api/users/999` returns **404** (empty body is fine).
- `open-in-view` is **false**.

#### Suggested Import Path
```kotlin
// Entity
import jakarta.persistence.*
/* @Entity, @Id, @GeneratedValue, @Table, @Column */

// Repository
import org.springframework.data.jpa.repository.JpaRepository

// Controller
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
````

#### Command to verify/run

```bash
./gradlew :07-data-jpa:bootRun

# expect 404 (no rows yet)
curl -i http://localhost:8080/api/users/1
# HTTP/1.1 404
```

---

### Problem B — Create (POST) with 201 + Location

#### Requirement

* `POST /api/users` with body `{"name":"<str>","email":"<str>"}`:

  * Persist via repository.
  * Return **201 Created**, `Location: /api/users/{id}`, and body with `id,name,email`.
* Use a tiny request DTO (no validation annotations yet).
* Constructor injection; service method annotated `@Transactional`.

#### Acceptance criteria

* Posting a valid body persists a row.
* Response is **201** with `Location` header and correct JSON body.

#### Suggested Import Path

```kotlin
// Service/tx
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Web
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
```

#### Command to verify/run

```bash
# create
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy","email":"huy@example.com"}'
# expect: HTTP/1.1 201
# Location: /api/users/1
# body: {"id":1,"name":"Huy","email":"huy@example.com"}

# read back
curl -s http://localhost:8080/api/users/1
# expect: {"id":1,"name":"Huy","email":"huy@example.com"}
```

---

### Problem C — Uniqueness & derived queries

#### Requirement

* Add DB unique constraint on `email` (via `@Column(unique = true)`).
* In repository, add:

  * `fun findByEmail(email: String): UserEntity?`
  * `fun existsByEmail(email: String): Boolean`
* Add `GET /api/users/by-email?email=<email>`:

  * If found → **200** with user JSON.
  * If missing → **404**.
* Add `GET /api/users/exists?email=<email>`:

  * Always **200** → `{"exists": true|false}`.

#### Acceptance criteria

* Duplicate insert attempts for the **same email** fail at DB level (you may return **500** for now; custom error handling comes later).
* `findByEmail` and `existsByEmail` endpoints behave as specified.

#### Suggested Import Path

```kotlin
// Repository
import org.springframework.data.jpa.repository.JpaRepository

// Web
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
```

#### Command to verify/run

```bash
# exists
curl -s "http://localhost:8080/api/users/exists?email=huy@example.com"
# expect: {"exists":true}

# lookup
curl -i "http://localhost:8080/api/users/by-email?email=huy@example.com"
# expect: 200 + user JSON

curl -i "http://localhost:8080/api/users/by-email?email=missing@example.com"
# expect: 404
```

---

### Problem D — Replace (PUT) and Delete

#### Requirement

* `PUT /api/users/{id}` strictly **replaces** the row’s `name` and `email`:

  * If target not found → **404** (no upsert).
  * If found → **200** with updated JSON.
* `DELETE /api/users/{id}`:

  * **204 No Content** by default.
  * Idempotent: deleting a missing id still **204**.

#### Acceptance criteria

* PUT on missing id → **404**.
* PUT on existing id updates and returns **200**.
* DELETE returns **204** whether the row existed or not.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
```

#### Command to verify/run

```bash
# replace (assumes id=1 exists)
curl -i -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy N.","email":"huy@example.com"}'
# expect: 200 with updated JSON

# delete (idempotent)
curl -i -X DELETE http://localhost:8080/api/users/1
# expect: 204

# delete again
curl -i -X DELETE http://localhost:8080/api/users/1
# expect: 204
```

---

### Problem E — DTO projection read (lightweight view)

#### Requirement

* Create a Kotlin DTO `UserView(id: Long, name: String)`.
* Add repository method with JPQL projection:

  ```kotlin
  @Query("select new com.example.data.UserView(u.id, u.name) from UserEntity u where u.email like %:domain")
  fun findViewsByEmailDomain(@Param("domain") domain: String): List<UserView>
  ```
* Add `GET /api/users/views?domain=@example.com` → returns array of `{id,name}`.

#### Acceptance criteria

* Endpoint returns an array of `UserView` (no `email` field).
* JPQL compiles and runs against H2.

#### Suggested Import Path

```kotlin
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -s "http://localhost:8080/api/users/views?domain=@example.com"
# expect: [{"id":2,"name":"..."}] (list may be empty if none match)
```

---

### Notes

* Keep **entities simple** (no relationships yet).
* Use **constructor injection** everywhere.
* Mark **write** service methods with `@Transactional`; reads can be `@Transactional(readOnly = true)`.
* Keep `open-in-view=false`; load data fully in the service layer.
* We’ll introduce **migrations** and **error contracts** in later topics.

```yaml
# Suggested application.yml (dev scope)
server:
  port: 8080
spring:
  datasource:
    url: jdbc:h2:mem:appdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
```
