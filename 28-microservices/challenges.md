# CHALLENGE.md — Topic 28: Microservices

> Assumption: you’ll create **separate Spring Boot apps** in the same repo, e.g.:
>
> * `customers-service`
> * `orders-service`
>   Each with its own `Application.kt`, `build.gradle.kts`, and `application.yml`.

---

### Problem A — Split a Monolith: Customers vs Orders

#### Requirement

Create **two independent services** with their own HTTP APIs and in-memory data.

**Customers Service** (`customers-service`):

* Runs on port **8081**.
* In-memory store of customers:

  ```json
  { "id": 1, "name": "Ada" }
  { "id": 2, "name": "Alan" }
  ```
* Endpoints:

  * `GET /api/customers/{id}` → `200` with JSON or `404` if missing.

**Orders Service** (`orders-service`):

* Runs on port **8082**.
* In-memory store of orders:

  ```json
  { "id": 100, "customerId": 1, "total": 42.0 }
  { "id": 101, "customerId": 2, "total": 99.0 }
  ```
* Endpoints:

  * `GET /api/orders/{id}` → `200` with JSON or `404` if missing.

Rules:

* Each service has its **own Spring Boot app** and port.
* No shared code module; copy small types if needed.

#### Acceptance criteria

* Both services start independently on 8081 and 8082.
* `GET /api/customers/1` from customers-service returns customer JSON.
* `GET /api/orders/100` from orders-service returns order JSON.
* Stopping one service does **not** affect the other’s availability.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
```

#### Command to verify/run

```bash
# Terminal 1
cd 28-microservices/customers-service
../../gradlew bootRun

# Terminal 2 - Start orders-service
cd 28-microservices/orders-service
../../gradlew bootRun


# Verify
curl -s http://localhost:8081/api/customers/1
curl -s http://localhost:8082/api/orders/100
```

---

### Problem B — Synchronous Service-to-Service Call (Order with Customer Info)

#### Requirement

Extend **orders-service** to call **customers-service** via HTTP to enrich responses.

* Keep both services & data from Problem A.
* In orders-service, add endpoint:

  * `GET /api/orders/{id}/details`
* It should:

  1. Load order from its own in-memory store.
  2. Call customers-service (`http://localhost:8081/api/customers/{customerId}`) to fetch customer.
  3. Return combined JSON:

     ```json
     {
       "orderId": 100,
       "total": 42.0,
       "customer": {
         "id": 1,
         "name": "Ada"
       }
     }
     ```
* If order is missing → `404`.
* If customer-service returns `404` or is down:

  * Respond with `502 Bad Gateway` and JSON:

    ```json
    { "error": "customer-service unavailable" }
    ```

Use **WebClient** (since you already covered external calls) inside orders-service.

#### Acceptance criteria

* Happy path returns `200` + combined JSON.
* Stopping customers-service and calling `/api/orders/{id}/details` returns `502` with error JSON.
* Orders-service must still be up even if customers-service is down.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
```

#### Command to verify/run

```bash
# Both up
curl -s http://localhost:8082/api/orders/100/details | jq

# Stop customers-service, keep orders-service running
curl -i http://localhost:8082/api/orders/100/details
```

---

### Problem C — Bounded Contexts & DTOs (No Shared Domain)

#### Requirement

Ensure **each service owns its own model types** and does not share JARs for domain.

* In customers-service:

  ```kotlin
  data class CustomerResponse(val id: Long, val name: String)
  ```
* In orders-service:

  ```kotlin
  data class RemoteCustomerDto(val id: Long, val name: String)
  data class OrderDetailsResponse(
      val orderId: Long,
      val total: Double,
      val customer: RemoteCustomerDto?
  )
  ```
* Do **not** create a common library for these DTOs.
* When calling customers-service, orders-service deserializes into its own `RemoteCustomerDto`.

#### Acceptance criteria

* Removing customers-service from the project (just hypothetically) should not break orders-service compile (only runtime calls fail).
* No `:common-domain` module or similar — strictly separate code.
* Orders-service compiles & runs with its **own** DTO types.

#### Suggested Import Path

```kotlin
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
```

(Use `@JsonIgnoreProperties(ignoreUnknown = true)` on `RemoteCustomerDto` if you add extra fields later.)

#### Command to verify/run

```bash
# Just confirm the detail endpoint still works and structures use local DTOs
curl -s http://localhost:8082/api/orders/100/details | jq
```

---

### Problem D — Fault Isolation & Timeouts

#### Requirement

Demonstrate that slow/downstream services do not hang the caller indefinitely.

In orders-service:

* Configure WebClient with a **read timeout** of 1 second when calling customers-service.
* Simulate a slow customers-service endpoint:

  * In customers-service, add `GET /api/customers/slow/{id}` that:

    * `Thread.sleep(3000)` then returns a normal customer.
* Modify orders-service to:

  * Call `/api/customers/slow/{customerId}` for details.
  * If timeout occurs, handle it and return:

    * HTTP `504 Gateway Timeout` with JSON:

      ```json
      { "error": "customer-service timeout" }
      ```

#### Acceptance criteria

* Calling slow endpoint directly on customers-service hangs ~3s then returns `200`.
* Calling `/api/orders/{id}/details` (which uses slow customers endpoint) returns in ~1s with `504`.
* Orders-service thread pool stays healthy; no stuck threads or app crash.

#### Suggested Import Path

```kotlin
import reactor.util.retry.Retry
import java.time.Duration
```

(Use timeout operators or WebClient `responseTimeout` options.)

#### Command to verify/run

```bash
# Slow customers endpoint (direct)
curl -w "\nTime: %{time_total}\n" -s http://localhost:8081/api/customers/slow/1

# Through orders-service (should be ~1s and 504)
curl -w "\nTime: %{time_total}\n" -i http://localhost:8082/api/orders/100/details\?useSlow=true
```

---

### Problem E — Simple “API Gateway” Stub (Fan-out via Orders Service)

#### Requirement

Use orders-service as a tiny **“gateway-like” facade** for a client:

* Add endpoint in orders-service:

  * `GET /api/client/orders/{id}/summary`
* It should:

  1. Load order (local).
  2. Call customers-service to fetch customer.
  3. Return a **flattened** client-oriented response:

     ```json
     {
       "id": 100,
       "customerName": "Ada",
       "total": 42.0
     }
     ```
* Do **not** expose the internal structure (`customerId`) to the client.

#### Acceptance criteria

* Client only needs to talk to orders-service (one base URL).
* Stopping customers-service still leads to a clean error from orders-service (`502`/`504` per your earlier logic).
* You’re not using a real gateway product yet (that’s fine; this is conceptual).

#### Suggested Import Path

*(Same imports as Problem B; reuse WebClient & controllers.)*

#### Command to verify/run

```bash
curl -s http://localhost:8082/api/client/orders/100/summary | jq
```

---

## Notes / Hints

* **Ports**: keep `customers-service` on 8081, `orders-service` on 8082 to avoid confusion.
* **Resilience**: you already learned circuit breakers & retries; here, just ensure **timeouts + error mapping**.
* **Independence**: each service must be able to **build, run, and deploy** independently.
* **No shared DB**: still in-memory; if you add DB later, each service must own its own schema.

✅ **End of CHALLENGE.md**
