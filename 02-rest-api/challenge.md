# 02 – REST API — Challenges (Scoped)

> **This topic covers only REST fundamentals**: routing, verbs, status codes, basic JSON, and minimal error signaling via `@ResponseStatus`.
> **Deferred to later topics:** validation, DTO mapping frameworks, global error contract, pagination/sorting, persistence/JPA, security, OpenAPI.

---

## Flashcard — REST API (Recap)

**What is it?**
HTTP endpoints exposing resources (e.g., `Book`) via **GET/POST/PUT/PATCH/DELETE** with JSON.

**When to use it?**
Clients (web/mobile/services) need CRUD over HTTP with clear, stateless semantics.

**Why it matters?**
It’s the standard contract for backends; clear separation of controller/service/repository.

**How does it work?**

* Routing: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`
* Binding: `@PathVariable`, `@RequestParam`, `@RequestBody`
* Serialization: Jackson ⇄ Kotlin data classes
* Status codes: 200/201/204/400/404… (just the essentials here)

**How to run**

```bash
./gradlew :02-rest-api:bootRun
# Base: http://localhost:8080
```

---

## Starter code (kept minimal & in-memory)

```kotlin
data class Book(val id: Long?, val title: String)

@RestController
@RequestMapping("/api/books")
class BookController {
  private val books = mutableListOf(Book(1, "Kotlin in Action"))

  @GetMapping fun all() = books

  @GetMapping("/{id}")
  fun byId(@PathVariable id: Long) =
    books.find { it.id == id } ?: throw NotFound(id)

  @PostMapping
  fun create(@RequestBody book: Book): Book {
    val nextId = (books.maxOfOrNull { it.id ?: 0 } ?: 0) + 1
    return book.copy(id = nextId).also { books += it }
  }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFound(id: Long) : RuntimeException("Book $id not found")
```

---

## C01 — List & Create (GET / POST)

**Goal**
Return all books; create a new book.

**Accept**

* `GET /api/books` → `200` + JSON array
* `POST /api/books` → `200` + created resource (we’ll refine to 201 later)

**Verify**

```bash
curl -sS http://localhost:8080/api/books | jq
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{ "title": "Clean Architecture" }'
```

**Notes (scope):** no validation of title yet.

---

## C02 — Get by ID + 404

**Goal**
Fetch single resource; return `404` if missing.

**Accept**

* `GET /api/books/1` → `200` with book
* `GET /api/books/9999` → `404` (via `NotFound`)

**Verify**

```bash
curl -i http://localhost:8080/api/books/1
curl -i http://localhost:8080/api/books/9999
```

**Notes (scope):** 404 mapping via `@ResponseStatus` only; global error JSON is deferred.

---

## C03 — Delete (idempotent or strict)

**Goal**
Remove by id.

**Pick one contract**

* **Idempotent (recommended here):** always `204 No Content`, even if it didn’t exist.
* **Strict:** `204` if removed, `404` if not found.

**Idempotent example**

```kotlin
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
fun delete(@PathVariable id: Long) {
  books.removeIf { it.id == id }
}
```

**Verify**

```bash
curl -i -X DELETE http://localhost:8080/api/books/2  # 204
curl -i -X DELETE http://localhost:8080/api/books/2  # 204 (still fine)
```

---

## C04 — Replace (PUT)

**Goal**
Replace the whole resource at `{id}`.

**Contract (strict for now)**

* If exists → **200 OK** with replaced resource
* If missing → **404 Not Found** (upsert will come later)

**Example**

```kotlin
@PutMapping("/{id}")
fun put(@PathVariable id: Long, @RequestBody book: Book): ResponseEntity<Book> {
  val index = books.indexOfFirst { it.id == id }
  if (index == -1) throw NotFound(id)

  val updated = book.copy(id = id)
  books[index] = updated
  return ResponseEntity.ok(updated)
}
```

**Verify**

```bash
curl -i -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{ "id": 999, "title": "Refactoring" }'   # expect 200, id forced to 1

curl -i -X PUT http://localhost:8080/api/books/9999 \
  -H "Content-Type: application/json" \
  -d '{ "title": "New" }'                       # expect 404
```

**Notes (scope):** no body–path id mismatch validation beyond overwriting; that’ll be formalized with validation later.

---

## C05 — Partial Update (PATCH)

**Goal**
Update only provided fields.

**Minimal approach for this topic**
Use a tiny DTO with nullable fields; reject empty payload.

```kotlin
data class UpdateBookRequest(val title: String? = null)

@PatchMapping("/{id}")
fun update(@PathVariable id: Long, @RequestBody req: UpdateBookRequest): ResponseEntity<Any> {
  val index = books.indexOfFirst { it.id == id }
  if (index == -1) throw NotFound(id)

  if (req.title == null) return ResponseEntity.badRequest().build() // minimal guard

  val current = books[index]
  val updated = current.copy(title = req.title)
  books[index] = updated
  return ResponseEntity.ok(updated)
}
```

**Verify**

```bash
curl -i -X PATCH http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{ "title": "Domain-Driven Design" }'      # 200

curl -i -X PATCH http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{}'                                       # 400

curl -i -X PATCH http://localhost:8080/api/books/9999 \
  -H "Content-Type: application/json" \
  -d '{ "title": "Anything" }'                  # 404
```

**Notes (scope):** no JSON Patch/Merge-Patch; no validation—just minimal partial update.

---

## C06 — 201 Created + Location (POST best practice)

**Goal**
On create, return `201 Created` and `Location` header.

**Example**

```kotlin
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@PostMapping
fun create201(@RequestBody book: Book): ResponseEntity<Book> {
  val nextId = (books.maxOfOrNull { it.id ?: 0 } ?: 0) + 1
  val created = book.copy(id = nextId).also { books += it }

  val location = ServletUriComponentsBuilder
    .fromCurrentRequestUri()
    .path("/{id}")
    .buildAndExpand(created.id)
    .toUri()

  return ResponseEntity.created(location).body(created)
}
```

**Verify**

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{ "title": "Effective Kotlin" }'
# Expect: 201 Created + Location: /api/books/{id}
```

**Notes (scope):** response body presence is fine either way; headers are the key point.

---

## C07 — Basic error signaling with `@ResponseStatus`

**Goal**
Map “not found” to `404` without global handlers.

**Example**
*(already present)*

```kotlin
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFound(id: Long) : RuntimeException("Book $id not found")
```

**Verify**

```bash
curl -i http://localhost:8080/api/books/9999
# 404 Not Found (body may vary; global error JSON comes later)
```

**Notes (scope):** no `@ControllerAdvice` yet (that’s Topic 05).

---

## C08 — (Optional) Content Negotiation Intro

**Goal**
See how `Accept` influences representation (minimal demo).

**JSON (default)**

```bash
curl -H "Accept: application/json" -sS http://localhost:8080/api/books
```

**XML (only if you add converter)**

* Add dependency (in this topic you can just observe behavior; full XML support is optional):
  `implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")`

```bash
curl -H "Accept: application/xml" -sS http://localhost:8080/api/books
```

**Notes (scope):** do not spend time on full XML schemas here.

---

## Out of scope (explicitly deferred)

* **Validation** (constraints, error messages) → Topic `04-validation`
* **DTO mapping frameworks / mappers** → Topic `09-dto-mapping`
* **Global error contract (`@ControllerAdvice`, problem+json)** → Topic `05-error-handling`
* **Pagination & sorting** → Topic `10-pagination-sorting`
* **Persistence (JPA/Testcontainers)** → Topics `07-data-jpa`, `13-integration-tests-db`
* **Security, docs, resilience, etc.** → later topics
