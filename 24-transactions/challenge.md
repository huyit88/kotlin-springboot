# CHALLENGE.md — Topic 24: Transactions

### Dependencies

Add only what’s new for this module (if not already in your root):

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}
```

---

### Problem A — Atomic Money Transfer (+ rollback on failure)

#### Requirement

Implement an **atomic** transfer between two accounts using `@Transactional`.

* Entity: `Account(id: Long, owner: String, balance: BigDecimal)`
* Repository: `AccountRepository : JpaRepository<Account, Long>`
* Service method:

  * `transfer(fromId, toId, amount)` annotated with `@Transactional`
  * Steps: load both, check `from.balance >= amount`, subtract & add, save.
  * If insufficient funds → throw `IllegalStateException` → **entire transaction rolls back**.
* Endpoint: `POST /api/tx/transfer?from=1&to=2&amount=50.00` → `200 OK` on success; `400` with message on insufficient funds.
* Seed data: 2 accounts (e.g., id=1 balance 100; id=2 balance 0).

#### Acceptance criteria

* Successful transfer moves money exactly once; balances reflect changes.
* Insufficient funds → **no change** to either balance (verified by GET).
* Idempotency: calling once succeeds; calling again may 400 if insufficient.
* GET helper: `GET /api/accounts/{id}` → `200` with JSON.

#### Suggested Import Path

```kotlin
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
```

#### Command to verify/run

```bash
./gradlew :24-transactions:bootRun
curl -s http://localhost:8080/api/accounts/1
curl -s -X POST "http://localhost:8080/api/tx/transfer?from=1&to=2&amount=50.00" -i
curl -s http://localhost:8080/api/accounts/1
curl -s http://localhost:8080/api/accounts/2
# Try overdraft (should rollback):
curl -s -X POST "http://localhost:8080/api/tx/transfer?from=1&to=2&amount=1000.00" -i
```

---

### Problem B — Propagation: `REQUIRES_NEW` Audit Persists on Outer Failure

#### Requirement

Add an **audit log** that must persist **even when the transfer fails**.

* Entity: `AuditLog(id, action, details, createdAt)`
* Service `AuditService.log(action, details)` annotated with `@Transactional(propagation = REQUIRES_NEW)`
* Modify transfer flow:

  * Always call `audit.log("TRANSFER_ATTEMPT", "...")` **before** validating funds.
  * If funds insufficient, throw `IllegalStateException` to rollback the transfer.
* Endpoint: `GET /api/audits` returns all logs.

#### Acceptance criteria

* On **successful transfer**, you see at least one audit row.
* On **insufficient funds**, transfer is rolled back **but** an audit row still exists (separate committed tx).
* Verify by calling overdraft; logs should capture the attempt.

#### Suggested Import Path

```kotlin
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import jakarta.persistence.*
```

#### Command to verify/run

```bash
# Cause overdraft
curl -s -X POST "http://localhost:8080/api/tx/transfer?from=1&to=2&amount=9999.00"
# Audit must still be present:
curl -s http://localhost:8080/api/audits
```

---

### Problem C — Isolation: Prevent Lost Updates (SELECT … FOR UPDATE)

#### Requirement

Demonstrate isolation with two-step update.

* Add service method `loadForUpdate(id)` that reads an account **pessimistically**:

  * Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` on a repository query OR `entityManager.lock(entity, PESSIMISTIC_WRITE)`.
* Endpoint 1: `POST /api/tx/deposit/lock?id=1&amount=10.00`

  * Starts a transaction, locks row, sleeps 10s, applies deposit, commits.
* Endpoint 2: `POST /api/tx/deposit/plain?id=1&amount=10.00`

  * Same deposit without lock/sleep (normal JPA save).

#### Acceptance criteria

* When you call **both** quickly (lock first, then plain), the **plain** one must **wait** or **fail** with lock timeout (depending on H2 config), ensuring **no lost update**.
* Final balance should equal the **sum** of both deposits, not one overwriting the other.

#### Suggested Import Path

```kotlin
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.jpa.repository.*
import org.springframework.data.jpa.repository.Lock
import jakarta.persistence.LockModeType
```

#### Command to verify/run

```bash
# In one terminal
curl -s -X POST "http://localhost:8080/api/tx/deposit/lock?id=1&amount=10.00"
# Quickly in another terminal (before 10s sleep ends)
curl -s -X POST "http://localhost:8080/api/tx/deposit/plain?id=1&amount=10.00"
# Check result
curl -s http://localhost:8080/api/accounts/1
```

---

### Problem D — Programmatic Transactions (`TransactionTemplate`)

#### Requirement

Show explicit control using `TransactionTemplate` to perform a **transfer+audit** in one place.

* Bean: `TransactionTemplate` (auto-configured via Data JPA).
* Service method `transferWithTemplate(fromId, toId, amount)`:

  * Use `transactionTemplate.execute { ... }` block for the transfer.
  * Inside, throw on insufficient funds (causes rollback).
  * After the block, call `audit.log(...)` using `REQUIRES_NEW` (from Problem B) regardless of outcome.

#### Acceptance criteria

* Success → balances updated, audit logged.
* Failure → balances unchanged, audit still logged.
* Behavior mirrors Problems A+B but written programmatically.

#### Suggested Import Path

```kotlin
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.TransactionStatus
```

#### Command to verify/run

```bash
curl -s -X POST "http://localhost:8080/api/tx/template?from=1&to=2&amount=5.00"
curl -s http://localhost:8080/api/audits
```

---

### Problem E — Self-Invocation Pitfall & Fix

#### Requirement

Illustrate why `@Transactional` on a method **inside the same class** won’t trigger when called directly.

* Create `BadService` with:

  * `@Transactional fun innerWrite()` that updates an account.
  * `fun outerCallsInner()` that calls `innerWrite()` directly.
* Add controller to invoke `outerCallsInner`.
* Create `GoodService` where `outer` calls **another bean’s** `innerWrite()` (moved to `InnerService`).

#### Acceptance criteria

* Calling the **bad** endpoint shows that transactional behavior (e.g., rollback on thrown exception) **did not** apply.
* Calling the **good** endpoint shows that transactional behavior **did** apply (rollback works).
* Include a deliberate exception after write to verify rollback.

#### Suggested Import Path

```kotlin
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
# Bad path (no proxy → no tx)
curl -s http://localhost:8080/api/accounts/1
curl -s -X POST "http://localhost:8080/api/tx/bad?id=1&amount=5.00"
# Good path (separate bean → tx applies)
curl -s -X POST "http://localhost:8080/api/tx/good?id=1&amount=5.00"
```

---

### Notes / Hints

* Use H2 in-memory with `spring.jpa.hibernate.ddl-auto=create-drop` for quick resets.
* For Problem C, H2 lock wait can be tuned:

  ```properties
  spring.jpa.properties.hibernate.jdbc.time_zone=UTC
  spring.jpa.properties.hibernate.jdbc.batch_size=20
  spring.jpa.properties.hibernate.generate_statistics=false
  spring.jpa.properties.hibernate.show_sql=false
  spring.jpa.properties.hibernate.format_sql=false
  spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
  spring.jpa.properties.hibernate.connection.handling_mode=DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION
  spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
  spring.jpa.properties.hibernate.default_batch_fetch_size=50
  spring.datasource.url=jdbc:h2:mem:txdb;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=2000
  ```
* Keep controller endpoints thin; all transactional work stays in services.
* Consistent HTTP semantics: all GET → `200`; POST “actions” → `200` unless you choose `204` for no body.
