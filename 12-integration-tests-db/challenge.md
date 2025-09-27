````markdown
# 12-integration-tests-db — Challenge 1 (JPA + Flyway + Testcontainers)

### Dependencies
_Add only if this subproject doesn’t already have them._

- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.data.jpa)`
  - `implementation(libs.flyway.core)`
  - `testImplementation(libs.spring.boot.starter.test)`
  - `testImplementation(libs.testcontainers.junitJupiter)`
  - `testImplementation(libs.testcontainers.postgresql)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")`
  - `implementation("org.flywaydb:flyway-core:10.17.3")`
  - `testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")`
  - `testImplementation("org.testcontainers:junit-jupiter:1.20.3")`
  - `testImplementation("org.testcontainers:postgresql:1.20.3")`

---

### Problem A — JPA slice + Postgres container + Flyway boot

#### Requirement
- Create **JPA entity** `UserEntity(id: Long? = null, name: String, email: String)` mapped to table `users`.
- Create **Spring Data** repo `UserRepository : JpaRepository<UserEntity, Long>` with:
  - `fun findByEmail(email: String): UserEntity?`
- Add Flyway migrations under:  
  `src/test/resources/db/migration/`
  - `V1__init.sql` → create `users(id BIGSERIAL PK, name VARCHAR(100) NOT NULL, email VARCHAR(200) NOT NULL)`
  - `V2__users_email_unique.sql` → add `UNIQUE(email)`
- Test class `UserRepositoryPgTest`:
  - Annotate `@DataJpaTest`
  - Configure **Postgres Testcontainer** + `@DynamicPropertySource` to set `spring.datasource.*`
  - Enable Flyway in test profile; disable Hibernate DDL:
    - `src/test/resources/application-test.yml`
      ```yaml
      spring:
        flyway:
          enabled: true
          locations: classpath:db/migration
        jpa:
          hibernate:
            ddl-auto: none
      ```
  - Annotate class with `@ActiveProfiles("test")`.
  - Test should start, run migrations, and `repo.count()` is `0`.

#### Acceptance criteria 
- Test context boots; `flyway_schema_history` contains `V1`, `V2`.
- `repo.count()` returns `0` with a clean DB.

#### Suggested Import Path
```kotlin
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository

// Testcontainers
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.DynamicPropertyRegistry

// Assertions
import kotlin.test.Test
import kotlin.test.assertEquals
````

#### Command to verify/run

```bash
./gradlew :12-integration-tests-db:test --tests "*UserRepositoryPgTest"
```

---

### Problem B — Unique constraint enforced (flush to see DB error)

#### Requirement

* In `UserRepositoryPgTest`, add a test:

  * Save a user with `email="a@x.com"`
  * Attempt to `saveAndFlush` another user with **same email**
  * Expect `DataIntegrityViolationException`

#### Acceptance criteria

* Duplicate insert causes `DataIntegrityViolationException`.
* Using `saveAndFlush` (not just `save`) to force SQL execution.

#### Suggested Import Path

```kotlin
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertFailsWith
```

#### Command to verify/run

```bash
./gradlew :12-integration-tests-db:test --tests "*UserRepositoryPgTest.unique*"
```

---

### Problem C — Case-insensitive search + EM clear

#### Requirement

* Extend repo with:

  * `fun findByNameContainingIgnoreCase(name: String): List<UserEntity>`
* Test:

  * Insert `("Ann","a@x.com")` and `("BANNer","b@x.com")`
  * `entityManager.flush()` + `entityManager.clear()` to avoid 1st-level cache
  * Query `findByNameContainingIgnoreCase("ann")` → should return **both**
  * Assert size and that both emails are present

#### Acceptance criteria

* Query returns both rows (case-insensitive).
* Test uses `flush()` + `clear()`.

#### Suggested Import Path

```kotlin
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertTrue
```

#### Command to verify/run

```bash
./gradlew :12-integration-tests-db:test --tests "*UserRepositoryPgTest.case*"
```

---

### Problem D — Pagination with stable sort (name then id)

#### Requirement

* Add repository:

  * `fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserEntity>`
* Seed (inside test method) multiple rows with duplicate names (e.g., three `"Alex"` with different ids).
* Build `PageRequest.of(0, 5, Sort.by(ASC,"name").and(Sort.by(ASC,"id")))`
* Assert:

  * Page contains expected size
  * For same `name`, ids are ascending (stable ordering)

#### Acceptance criteria

* Returned page size matches request.
* Order within duplicate names is by `id ASC`.

#### Suggested Import Path

```kotlin
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals
```

#### Command to verify/run

```bash
./gradlew :12-integration-tests-db:test --tests "*UserRepositoryPgTest.pagination*"
```

---

### Problem E — Service-level transaction flow (`@SpringBootTest`)

#### Requirement

* Create a tiny service `UserService` (in main src) with:

  * `@Transactional` `fun replace(id: Long, name: String, email: String): UserEntity`

    * Load by id, update both fields, save
* Write `UserServiceIT` with:

  * `@SpringBootTest`, `@Testcontainers`, `@ActiveProfiles("test")`
  * Same Postgres container + `@DynamicPropertySource`
  * Insert a user via repo, then call `replace`
  * Assert both fields updated; also verify **rollback** behavior:

    * Create a method `replaceFailing()` that updates then throws → ensure changes are not persisted

#### Acceptance criteria

* Happy path persists both fields.
* Failing path rolls back (original row unchanged after the call).

#### Suggested Import Path

```kotlin
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
```

#### Command to verify/run

```bash
./gradlew :12-integration-tests-db:test --tests "*UserServiceIT"
```

---

### Hints / Notes

* **Entity & Repo locations**: put them under this module (e.g., `com.example.itdb.*`).
* **Flyway location in tests**: using `src/test/resources/db/migration` isolates migrations for this module’s tests.
* **Disable Hibernate DDL**: `ddl-auto=none` so Flyway remains the single source of truth.
* **`@DataJpaTest`** auto-rolls back after each test → DB cleaned between tests.
* **Testcontainers**: image suggestion → `postgres:16-alpine`.
* **Apple Silicon**: the `postgres:16-alpine` image works on ARM.

```yaml
# src/test/resources/application-test.yml (copy/paste)
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: none
  datasource:
    # URL/credentials are injected by @DynamicPropertySource from the container
```

```
::contentReference[oaicite:0]{index=0}
```
