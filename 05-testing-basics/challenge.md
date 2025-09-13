# 05-testing-basics — Challenge 1 (Problem Set)

### Dependencies

> Add only if not already present in your root.

* **Version catalog (preferred)**

  * `testImplementation(libs.junit.jupiter)`
  * `testImplementation(libs.junit.jupiter.params)`

* **Direct coordinates (alternative)**

  * `testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")`
  * `testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")`

---

### Problem A — Price calculator (pure unit)

#### Requirement

* Implement `PriceCalculator(taxRate: Double)` with `fun total(netAmount: Double): Double`.
* Rules:

  * If `netAmount < 0`, throw `IllegalArgumentException("netAmount must be >= 0")`.
  * Otherwise: `total = netAmount * (1 + taxRate)`.
* Write two unit tests (no Spring):

  * `total adds tax to net amount`
  * `negative net amount throws`
* Favor multi-line, readable code and AAA structure.

#### Acceptance criteria

* Both tests pass.
* Failure path asserts exception **type** and **exact message** `"netAmount must be >= 0"`.

#### Suggested Import Path

* `05-testing-basics/src/test/kotlin/com/example/pricing/PriceCalculatorTest.kt`

  ```kotlin
  import org.junit.jupiter.api.Test
  import org.junit.jupiter.api.Assertions.assertEquals
  import org.junit.jupiter.api.Assertions.assertThrows
  ```

#### Command to verify/run

```bash
./gradlew :05-testing-basics:test --tests "com.example.pricing.PriceCalculatorTest"
# Expect: 2 tests, 0 failures
```

---

### Problem B — Service with in-memory fake repository

#### Requirement

* Domain: `data class User(id: Long = 0, name: String, email: String)`.
* Create `UserRepository` interface (`save`, `findById`).
* Implement `InMemoryUserRepository` that assigns incremental IDs starting at **1**.
* `UserService.createUser(name, email)` uses the repo and returns saved `User`.
* Tests:

  * `createUser assigns an id`
  * `findById returns saved user`
* No mocks; use the in-memory fake. AAA style.

#### Acceptance criteria

* Both tests pass.
* `saved.id >= 1`.
* `repository.findById(saved.id)` returns a value-equal `User`.

#### Suggested Import Path

* `05-testing-basics/src/test/kotlin/com/example/users/UserServiceTest.kt`

  ```kotlin
  import org.junit.jupiter.api.Test
  import org.junit.jupiter.api.Assertions.assertEquals
  import org.junit.jupiter.api.Assertions.assertNotNull
  import org.junit.jupiter.api.Assertions.assertTrue
  ```

#### Command to verify/run

```bash
./gradlew :05-testing-basics:test --tests "com.example.users.UserServiceTest"
# Expect: tests for create + find pass
```

---

### Problem C — Parameterized tests (table-driven)

#### Requirement

* Implement `fun square(n: Int): Int = n * n`.
* Parameterized test with `@CsvSource` for cases: `(0,0)`, `(2,4)`, `(3,9)`, `(10,100)`.
* Keep production code minimal and test readable.

#### Acceptance criteria

* All four parameter rows pass.
* No Spring/JUnit Vintage pulled in.

#### Suggested Import Path

* `05-testing-basics/src/test/kotlin/com/example/math/SquareTest.kt`

  ```kotlin
  import org.junit.jupiter.params.ParameterizedTest
  import org.junit.jupiter.params.provider.CsvSource
  import org.junit.jupiter.api.Assertions.assertEquals
  ```

#### Command to verify/run

```bash
./gradlew :05-testing-basics:test --tests "com.example.math.SquareTest"
# Expect: 4/4 rows pass
```

---

### Problem D — Time-dependent logic with `Clock`

#### Requirement

* Implement `NoonChecker(clock: Clock)` with `fun isUtcNoon(): Boolean` (true only when UTC hour == 12).
* Tests with `Clock.fixed(...)`:

  * `returns true at exactly 12 UTC`
  * `returns false at 11 UTC`
* Do not use system time in tests.

#### Acceptance criteria

* Both tests pass reliably (no flakiness).
* `Clock.fixed` used in each test.

#### Suggested Import Path

* `05-testing-basics/src/test/kotlin/com/example/time/NoonCheckerTest.kt`

  ```kotlin
  import org.junit.jupiter.api.Test
  import org.junit.jupiter.api.Assertions.assertTrue
  import org.junit.jupiter.api.Assertions.assertFalse
  import java.time.Clock
  import java.time.Instant
  import java.time.ZoneOffset
  ```

#### Command to verify/run

```bash
./gradlew :05-testing-basics:test --tests "com.example.time.NoonCheckerTest"
# Expect: 2 tests, 0 failures
```

---

### Problem E — Test data builder (readable fixtures)

#### Requirement

* In `src/test`, create builder:
  `fun aUser(id: Long = 0, name: String = "Huy", email: String = "huy@example.com"): User`
* Tests:

  * `builder provides sensible defaults`
  * `builder lets me override selected fields`
* Optionally refactor one other test to use the builder for readability.

#### Acceptance criteria

* Both builder tests pass.
* Builder improves readability in at least one other test (optional but recommended).

#### Suggested Import Path

* `05-testing-basics/src/test/kotlin/com/example/builder/BuildersTest.kt`

  ```kotlin
  import com.example.users.User
  import org.junit.jupiter.api.Test
  import org.junit.jupiter.api.Assertions.assertEquals
  ```

#### Command to verify/run

```bash
./gradlew :05-testing-basics:test --tests "com.example.builder.BuildersTest"
# Expect: 2 tests, 0 failures
```
