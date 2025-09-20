# 10-pagination-sorting — Challenge 1 (Offset Pagination + Sorting)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.web)`
  - _For Problems C–E (DB-backed):_ `implementation(libs.spring.boot.starter.data.jpa)` and `runtimeOnly(libs.h2)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")`
  - _For Problems C–E:_ `implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")`, `runtimeOnly("com.h2database:h2:2.2.224")`

---

### Problem A — In-memory pagination (+ envelope)

#### Requirement
- Seed an **in-memory** list of at least **8** users: `User(id: Long, name: String, email: String)`.
- Implement `GET /api/memory/users` with query params:
  - `page` (0-based, default **0**), `size` (default **5**, clamp to **1..50**),
  - `sort` (default **`id,asc`**) with format `field,direction`, where `field ∈ {id,name,email}` and `direction ∈ {asc,desc}`.
- Response is an **envelope**:
  ```json
  {
    "content": [ { "id":1, "name":"...", "email":"..." } ],
    "page": 0,
    "size": 5,
    "totalElements": 12,
    "totalPages": 3,
    "hasNext": true
  }
````

* Sorting and paging must happen **in-memory** (no DB).

#### Acceptance criteria

* `page=0&size=3` returns 3 items; `hasNext=true` if more remain.
* `sort=name,desc` orders by `name` descending.
* `size` outside 1..50 gets coerced into the range.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
./gradlew :10-pagination-sorting:bootRun

curl -s "http://localhost:8080/api/memory/users?page=0&size=3"
# expect: content length 3, page 0, hasNext true

curl -s "http://localhost:8080/api/memory/users?page=1&size=3&sort=name,desc"
# expect: content length 3, names in descending order
```

---

### Problem B — Sort whitelisting + multi-key tie-breaker

#### Requirement

* Extend Problem A handler to:

  * **Whitelist** sort fields to `{id,name,email}`. If an unknown field is provided, **fall back** to `id,asc` (do not 400 for this challenge).
  * Support **multiple** `sort` params (e.g., `?sort=name,asc&sort=id,asc`) and apply them in given order (name then id).
  * Always end with a deterministic **tie-breaker** on `id,asc` if not already present.
* Include the **effective sort keys** in the envelope as:

  ```json
  "sort": ["name,asc","id,asc"]
  ```

#### Acceptance criteria

* `sort=unknown,desc` falls back to `["id,asc"]`.
* `sort=name,asc` implies final sort is `["name,asc","id,asc"]`.
* Order is stable across calls.

#### Suggested Import Path

```kotlin
import java.util.Comparator
```

#### Command to verify/run

```bash
curl -s "http://localhost:8080/api/memory/users?sort=unknowne%2Cdesc"
# expect: envelope.sort == ["id,asc"]

curl -s "http://localhost:8080/api/memory/users?sort=name%2Casc"
# expect: envelope.sort includes ["name,asc","id,asc"]
```

---

### Problem C — DB-backed pagination with `Pageable` (H2 + Spring Data)

#### Requirement

* Add **H2** datasource and JPA; set `open-in-view=false`.
* Create `UserEntity(id: Long? = null, name: String, email: String)` mapped to table `users`.
* `UserRepository : JpaRepository<UserEntity, Long>` with:

  * `fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<UserEntity>`
* Seed at least **12** rows on startup (CommandLineRunner).
* Implement `GET /api/users?q=<substring>` taking **`Pageable`** (Spring binds `page`, `size`, `sort`).

  * Map `Page<UserEntity>` → `Page<UserResponse>` (id, name, email).
  * Default sort if none provided: `id,asc`.

#### Acceptance criteria

* `GET /api/users?page=0&size=5` returns a **Page** with `content.size == 5`, and page metadata present.
* `q` filters by `name` (case-insensitive).
* `sort=name,desc` orders by name descending.

#### Suggested Import Path

```kotlin
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
./gradlew :10-pagination-sorting:bootRun

curl -s "http://localhost:8080/api/users?page=0&size=5"
# expect: 5 items in content, Page metadata (totalElements, totalPages)

curl -s "http://localhost:8080/api/users?q=a&sort=name%2cdesc&page=0&size=5"
# expect: names matching 'a' (case-insensitive), descending by name
```

---

### Problem D — Stable default sort (name + id)

#### Requirement

* For the DB endpoint in Problem C, set default sort to **name,asc then id,asc**.
* Prove stability when multiple rows share the same name:

  * Insert (or ensure) duplicates: e.g., names `"Alex"` with different ids.
  * Response with `sort=name,asc` must order rows with same name by `id,asc`.

#### Acceptance criteria

* With duplicates, ordering by `name,asc` is stable by `id,asc` as tie-breaker.
* Explicit multi-sort `?sort=name,asc&sort=id,asc` yields the same order as default.

#### Suggested Import Path

```kotlin
import org.springframework.data.domain.Sort
import org.springframework.data.domain.PageRequest
```

#### Command to verify/run

```bash
curl -s "http://localhost:8080/api/users?sort=name%2casc&page=0&size=20"
# expect: users grouped by name asc; within 'Alex', ids strictly increasing
```

---

### Problem E — `Slice` endpoint (no total count)

#### Requirement

* Add a repository method returning a **Slice**:

  * `fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Slice<UserEntity>`
* Implement `GET /api/users/slice?q=<substring>&page=<n>&size=<m>&sort=<...>`:

  * Return envelope: `{ "content":[...], "page": n, "size": m, "hasNext": true|false }`
  * No `totalElements` / `totalPages` (avoid count query).

#### Acceptance criteria

* With a dataset larger than `size`, `hasNext=true` for non-final pages.
* Sorting works the same as in Problem C.

#### Suggested Import Path

```kotlin
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
```

#### Command to verify/run

```bash
curl -s "http://localhost:8080/api/users/slice?q=e&page=0&size=5"
curl -s "http://localhost:8080/api/slice/users?q=e&page=0&size=5"
# expect: 'first/last' reflects whether more pages exist
```
