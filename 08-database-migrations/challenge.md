````markdown
# 08-database-migrations — Challenge 1 (Flyway with H2)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.data.jpa)`  _(if missing)_
  - `runtimeOnly(libs.h2)`  _(if missing)_
  - `implementation(libs.flyway.core)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")`
  - `runtimeOnly("com.h2database:h2:2.2.224")`
  - `implementation("org.flywaydb:flyway-core:10.17.3")`

---

### Problem A — Initialize schema with Flyway (V1)

#### Requirement
- Configure Flyway to scan `classpath:db/migration`.
- Create `V1__init.sql` that creates table `users`:
  - `id BIGINT PRIMARY KEY AUTO_INCREMENT`
  - `name VARCHAR(100) NOT NULL`
  - `email VARCHAR(200) NOT NULL`
- Add a tiny read-only endpoint to verify: `GET /api/users/count` → `{"count": <int>}`.

#### Acceptance criteria 
- On app start, Flyway creates `flyway_schema_history` and runs `V1`.
- `GET /api/users/count` returns `{"count":0}`.
- JPA `open-in-view=false`.

#### Suggested Import Path
```kotlin
// Controller / JDBC
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
````

#### Command to verify/run

```bash
./gradlew :08-database-migrations:bootRun
curl -s http://localhost:8080/api/users/count
# expect: {"count":0}
```

---

### Problem B — Versioned seed data (V2)

#### Requirement

* Create `V2__seed_users.sql` that inserts **two** rows into `users`:

  * `('Huy','huy@example.com')`
  * `('Alice','alice@example.com')`
* Keep this as a **versioned** migration (not repeatable).

#### Acceptance criteria

* After restart (or clean run), `/count` reports `{"count":2}`.
* `flyway_schema_history` shows `V1` then `V2`.

#### Suggested Import Path

*(same as Problem A)*

#### Command to verify/run

```bash
./gradlew :08-database-migrations:clean :08-database-migrations:bootRun
curl -s http://localhost:8080/api/users/count
# expect: {"count":2}
```

---

### Problem C — Repeatable reference data (R\_\_)

#### Requirement

* Create a new table via **versioned** migration `V3__create_countries.sql`:

  * `code VARCHAR(2) PRIMARY KEY`, `name VARCHAR(100) NOT NULL`
* Create **repeatable** migration `R__countries_seed.sql` to upsert a few rows (H2‐friendly):

  * Use H2 `MERGE`:

    ```sql
    MERGE INTO countries (code, name) KEY(code) VALUES ('VN','Vietnam');
    MERGE INTO countries (code, name) KEY(code) VALUES ('US','United States');
    MERGE INTO countries (code, name) KEY(code) VALUES ('DE','Germany');
    ```
* Add `GET /api/countries` → `["VN","US","DE"]` (order doesn’t matter).

#### Acceptance criteria

* On run, `countries` exists and is seeded.
* Editing `R__countries_seed.sql` (e.g., renaming a country) re-applies on next run (Flyway detects checksum change).

#### Suggested Import Path

```kotlin
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
./gradlew :08-database-migrations:bootRun
curl -s http://localhost:8080/api/countries
# expect: ["VN","US","DE"]   (array order may vary)
```

---

### Problem D — Constraint change via migration (V4)

#### Requirement

* Create `V4__add_users_email_unique.sql` to add a **unique** constraint on `users.email`.

  * For H2: `ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE(email);`
* Add a tiny insert endpoint to demonstrate constraint is enforced:

  * `POST /api/users` with body `{"name":"<str>","email":"<str>"}` inserts a row using `JdbcTemplate.update(...)`.
  * On duplicate email, let it bubble and return **500** (we will add global error handling later).

#### Acceptance criteria

* Inserting a new unique email → **200** / any success JSON (e.g., `{"rows":1}`).
* Inserting an existing email → **500** from DB constraint violation.

#### Suggested Import Path

```kotlin
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
```

#### Command to verify/run

```bash
# success (new email)
curl -s -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob","email":"bob@example.com"}'
# expect: HTTP/1.1 200 ... body like {"rows":1}

# duplicate (uses seeded email)
curl -s -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Dup","email":"huy@example.com"}'
# expect: HTTP/1.1 500
```

---

### Minimal scaffolding hints (no full dumps)

* **Flyway location**: put SQL files under
  `08-database-migrations/src/main/resources/db/migration/`

* **Example application.yml**

  ```yaml
  server:
    port: 8080
  spring:
    datasource:
      url: jdbc:h2:mem:appdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
      driver-class-name: org.h2.Driver
    jpa:
      open-in-view: false
      hibernate:
        ddl-auto: none     # Flyway owns schema
    flyway:
      enabled: true
      locations: classpath:db/migration
  ```
---

### Notes

* **Never edit** an applied `V*` file—create a new version instead.
* Keep `ddl-auto=none` when using Flyway (Flyway is the source of truth).
* For team workflows, rebase or renumber `V*` before merge to avoid collisions.
* H2 syntax differs from Postgres/MySQL; avoid vendor-specific SQL unless intended.
