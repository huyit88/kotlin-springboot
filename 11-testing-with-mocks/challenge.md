# 11-testing-with-mocks — Challenge 1 (Unit + Web slice + Spring mocks)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `testImplementation(libs.spring.boot.starter.test)`  _(JUnit 5, MockMvc, AssertJ, Mockito)_
  - `testImplementation(libs.mockk)`  _(pure unit tests with MockK)_
  - `testImplementation(libs.springmockk)`  _(for `@MockkBean` in Spring tests)_
- Direct coordinates (alternative):
  - `testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")`
  - `testImplementation("io.mockk:mockk:1.13.12")`
  - `testImplementation("com.ninja-squad:springmockk:4.0.2")`

> Tip: keep production code tiny/in-memory; your focus is the **tests**.

---

### Problem A — Pure unit test with MockK (no Spring)

#### Requirement
- Create `UserRepository` (interface) and `UserService` (class) with methods:
  - `existsByEmail(email: String): Boolean`
  - `save(user: User): User`
  - In `UserService.create(name, email)`:
    - if `existsByEmail(email)` → throw `IllegalStateException("email taken")`
    - else save and return the saved `User` with non-null `id`.
- Write `UserServiceTest` (no Spring):
  - Mock the repo with **MockK**.
  - Test **happy path** and **email taken** path.
  - Verify interactions (called once / not called).

#### Acceptance criteria
- Both tests pass.
- `verify`/`confirmVerified` used appropriately.
- No Spring annotations or context started.

#### Suggested Import Path
```kotlin
import io.mockk.*
import kotlin.test.*
````

#### Command to verify/run

```bash
./gradlew :11-testing-with-mocks:test --tests "*UserServiceTest"
```

---

### Problem B — Controller slice with `@WebMvcTest` + `@MockkBean`

#### Requirement

* Create `UserController` with endpoint `GET /api/users/{id}`:

  * Delegates to `UserService.get(id): User?`
  * Returns **200** with JSON when found, **404** otherwise.
* Write `UserControllerWebTest`:

  * Annotate `@WebMvcTest(UserController::class)`
  * Inject `MockMvc`
  * Replace service with `@MockkBean lateinit var service: UserService`
  * Stub `service.get(1)` to return a `User`
  * Assert JSON via `jsonPath`
  * Add a test for 404

#### Acceptance criteria

* Both tests pass.
* No DB or full context started (slice only).

#### Suggested Import Path

```kotlin
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.beans.factory.annotation.Autowired;
```

#### Command to verify/run

```bash
./gradlew :11-testing-with-mocks:test --tests "*UserControllerWebTest"
```

---

### Problem C — Spring Boot test with `@MockkBean` (replace external client)

#### Requirement

* Define an external boundary:

  * `interface Mailer { fun send(to: String, subject: String, body: String): Boolean }`
* `SignupService.register(name,email)`:

  * Calls `mailer.send(...)`
  * Returns a simple result object `{ ok: Boolean }`
* Write `SignupFlowIT` with `@SpringBootTest`:

  * `@MockkBean lateinit var mailer: Mailer`
  * Stub success path (`returns true`) and failure path (`returns false`)
  * Assert the returned result and **verify** the call happened once with expected args

#### Acceptance criteria

* Both tests pass.
* `@MockkBean` effectively replaces the real `Mailer` bean.
* Interaction verified (exact invocation count + argument matchers).

#### Suggested Import Path

```kotlin
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
```

#### Command to verify/run

```bash
./gradlew :11-testing-with-mocks:test --tests "*SignupFlowIT"
```

---

### Problem D — Argument capture & behavior verification

#### Requirement

* Extend `UserService.create` unit test:

  * Capture the `User` passed to `repo.save(...)` using `slot<User>()`.
  * Assert the captured object has the exact `name`/`email` you passed in.
  * Make `save` answer with the same captured user but with `id = 42L`.
* Add verification that `existsByEmail` is checked **before** `save` (order).

#### Acceptance criteria

* Test passes with captured values asserted.
* Use `verifyOrder { existsByEmail(...); save(any()) }`.

#### Suggested Import Path

```kotlin
import io.mockk.slot
import io.mockk.verifyOrder
```

#### Command to verify/run

```bash
./gradlew :11-testing-with-mocks:test --tests "*UserServiceTest"
```

---

### Problem E — Error simulation and assertion

#### Requirement

* Add a new service method `UserService.load(id: Long): User` that:

  * Calls `repo.findById(id): User?`
  * Throws `NoSuchElementException("user $id not found")` if null
* Unit test:

  * Mock repo to return `null` for a given id
  * Assert `assertFailsWith<NoSuchElementException>` and check message text

#### Acceptance criteria

* Failing path covered; message contains the id.
* Repo `findById` verified invoked once; no extra calls.

#### Suggested Import Path

```kotlin
import kotlin.test.assertFailsWith
import io.mockk.verify
```

#### Command to verify/run

```bash
./gradlew :11-testing-with-mocks:test --tests "*UserServiceTest"
```

---

### Notes

* Prefer **MockK** for Kotlin unit tests; it has better nullability and DSL.
* In Spring tests, `@MockkBean` (from **springmockk**) lets you keep MockK; otherwise use `@MockBean` (Mockito).
* Keep production code minimal/in-memory; don’t involve DB here (integration tests come later).
