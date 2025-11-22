# Topic 27: Architecture

### Dependencies

(Use only what’s already available: Spring Web, Spring Data JPA, H2.)

---

## Problem A — Package-by-Feature (Clean Layering)

### Requirement

Reorganize a simple **Book** domain using **package-by-feature** with strict layering:

```
com.example.book
 ├── Book.kt
 ├── BookRepository.kt      (domain port)
 ├── BookService.kt         (domain logic)
 ├── BookController.kt      (web adapter)
 └── BookRepositoryJpa.kt   (db adapter)
```

Rules:

* `Book.kt` = pure Kotlin data class (no annotations except JPA if needed).
* `BookRepository` = domain port (interface).
* `BookRepositoryJpa` = adapter implementing port using Spring Data JPA.
* `BookService` uses the **port**, not the JPA interface.
* `BookController` uses the service, not databases.
* Endpoints:

  * `GET /api/books/{id}` → 200 or 404
  * `POST /api/books` → create + return 201 + Location

Seed with 2 books in a `@PostConstruct` inside the JPA adapter (or config class).

### Acceptance criteria

* Package structure exactly as above.
* No layer violations:

  * controller → service → domain port → adapter → JPA.
* All endpoints work as expected with in-memory H2.

### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.*
import org.springframework.data.jpa.repository.*
import jakarta.persistence.*
```

### Command to verify/run

```bash
./gradlew :27-architecture:bootRun
curl -s http://localhost:8080/api/books/1
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"title":"DDD","author":"Evans"}' http://localhost:8080/api/books -i
```

---

## Problem B — Hexagonal Architecture (Ports & Adapters)

### Requirement

Define a small **Payment** module using **hexagonal (ports + adapters)**:

**Domain core (`com.example.payment.domain`):**

* `Payment` data class.
* `PaymentProcessor` service:

  * `fun process(amount: BigDecimal): PaymentResult`.

**Ports (`domain.port`):**

* `PaymentGateway`:

  ```kotlin
  interface PaymentGateway { fun charge(amount: BigDecimal): Boolean }
  ```

**Adapters (`adapter.out`)**:

* Implement `PaymentGateway` with a fake gateway that:

  * randomly succeeds/fails (e.g. `Random.nextBoolean()`).

**Adapters (`adapter.in.web`)**:

* Controller exposing:

  * `POST /api/payments?amount=...` → returns 200 with `PaymentResult`.
* Controller must depend **only** on `PaymentProcessor` (domain).

Rules:

* Domain cannot depend on Spring or adapters.
* Gateway implementation depends on Spring + optionally JPA (but keep in-memory).

### Acceptance criteria

* Success/failure path both handled (random).
* Package structure enforces hexagonal boundaries.
* Processor calls the gateway port, not a JPA repo.

### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.*
```

### Command to verify/run

```bash
curl -s -X POST "http://localhost:8080/api/payments?amount=20.00"
```

---

## Problem C — Modular Monolith (Two Features, No Leaks)

### Requirement

Build two independent modules inside the same Spring Boot app:

```
com.example.catalog
  ├── CatalogController
  ├── CatalogService
  └── CatalogRepository (in-memory)

com.example.customer
  ├── CustomerController
  ├── CustomerService
  └── CustomerRepository (in-memory)
```

Rules:

* No cross-imports allowed (catalog must not depend on customer, and vice versa).
* Both modules expose their own endpoints:

  * `GET /api/catalog/{id}`
  * `GET /api/customers/{id}`
* Both seeds with a couple of entries via `@PostConstruct`.

### Acceptance criteria

* Each feature slice works independently.
* No accidental package imports across slices.
* Both endpoints return 200 or 404.

### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.*
```

### Command to verify/run

```bash
curl -s http://localhost:8080/api/catalog/1
curl -s http://localhost:8080/api/customers/1
```

---

## Problem D — Cross-Module Use Case via Application Service Layer

### Requirement

Create a `PlaceOrder` use case that spans two modules: **customer** and **catalog**.

Constraints:

* **Domain modules cannot call each other directly**.
* Introduce a new `orders` application-level module:

  ```
  com.example.orders
    ├── PlaceOrderService
    └── OrderController
  ```
* `PlaceOrderService` depends on:

  * `CustomerService` (to validate customer exists)
  * `CatalogService` (to fetch product)
* It does **not** own domain; it orchestrates the use case.

Endpoint:

* `POST /api/orders?customerId=1&productId=10`

  * Returns:

    ```json
    {
      "customer": "...",
      "product": "...",
      "status": "PLACED"
    }
    ```

### Acceptance criteria

* No package cycles: catalog ↛ customer; both only imported by orders.
* Successful and failing cases validated:

  * Missing customer → 404
  * Missing product → 404

### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.*
```

### Command to verify/run

```bash
curl -s -X POST "http://localhost:8080/api/orders?customerId=1&productId=10" | jq
```

---

## Problem E — Enforce Boundaries with Gradle (Bonus)

### Requirement

Convert the monolith into **multi-module Gradle** structure:

```
:module-catalog
:module-customer
:module-orders
:app (Spring Boot)
```

Rules:

* `module-orders` depends on catalog + customer.
* catalog & customer do **NOT** depend on each other.
* `app` depends on all three modules.
* Root `settings.gradle.kts` includes all modules.
* `app` contains the main `Application.kt`.

*No need to write full build files; just structure + minimal dependency declarations.*

### Acceptance criteria

* Build succeeds: `./gradlew build`
* Cycles prevented (Gradle enforces direction).
* App runs and all endpoints still work.

### Suggested Import Path

*(N/A — Gradle setup only.)*

### Command to verify/run

```bash
./gradlew build
./gradlew :app:bootRun
```

---

## Notes / Hints

* For layering problems, **imports** are the easiest way to detect violations.
* Use constructor injection everywhere.
* Keep domain pure in hexagonal example (no Spring annotations).
* Prefer small, dedicated packages over “util” or “common”.
* Use clear module naming conventions: `adapter.in`, `adapter.out`, `domain`, etc.

✅ **End of CHALLENGE.md**
