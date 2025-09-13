# 03-dependency-injection — Challenge 2 (Problem Set)

## Problem A — Default vs Qualified Bean

**Goal**
Implement two `MessageFormatter` beans and expose two endpoints that prove selection by default vs qualifier.

**Requirements**

* Create an interface: `MessageFormatter { fun format(raw: String): String }`.
* Provide two beans:

  * `PlainFormatter` (default): returns input unchanged. Mark as **`@Primary`**.
  * `ShoutFormatter` (named): uppercases and appends `!`. Bean name must be **`"shoutFormatter"`**.
* Create a `GreetingService` that builds a message using a `GreetingRepository` template `"Hello, %s"`.
* Create a `LoudGreetingService` that uses **`@Qualifier("shoutFormatter")`**.
* Endpoints (GET):

  * `/greet?name=Huy` → uses default formatter.
  * `/greet/loud?name=Huy` → uses qualified formatter.

**Constraints**

* **Constructor injection only** (no field/setter).
* Use `@Service`/`@Repository` stereotypes appropriately.
* Keep beans stateless.

**Files to create/update**

* `03-dependency-injection/src/main/kotlin/com/example/di/core/MessageFormatter.kt`
* `03-dependency-injection/src/main/kotlin/com/example/di/core/GreetingRepository.kt`
* `03-dependency-injection/src/main/kotlin/com/example/di/core/GreetingService.kt`
* `03-dependency-injection/src/main/kotlin/com/example/di/api/GreetingController.kt`

**Verification**

```bash
./gradlew :03-dependency-injection:bootRun
curl -s "http://localhost:8080/api/greet?name=Huy"
# expected: {"message":"Hello, Huy"}

curl -s "http://localhost:8080/api/greet/loud?name=Huy"
# expected: {"message":"HELLO, HUY!"}
```

**Acceptance criteria**

* `/greet` returns **200** with `{"message":"Hello, Huy"}`.
* `/greet/loud` returns **200** with `{"message":"HELLO, HUY!"}`.
* `PlainFormatter` is `@Primary`; `ShoutFormatter` bean name is `shoutFormatter`.

---

## Problem B — Ordered Strategy Pipeline (`List<T>`)

**Goal**
Normalize the `name` with an **ordered** chain of rules before formatting.

**Requirements**

* Define `Rule { fun apply(s: String): String }`.
* Provide three beans with `@Order(n)`:

  1. `TrimRule` (1): trim.
  2. `CollapseSpacesRule` (2): replace multiple spaces with single space.
  3. `UpperRule` (3): uppercase.
* Create `NormalizationPipeline` that injects `List<Rule>` (ordered), folds over them, then formats with the **default** formatter and repository template.
* Endpoint (GET): `/greet/normalize?name=   huy    nguyen   `.

**Constraints**

* Use `@Order` to guarantee execution order (lower first).
* `List<Rule>` must be injected via constructor.

**Files to create/update**

* `03-dependency-injection/src/main/kotlin/com/example/di/core/Rules.kt`
* `03-dependency-injection/src/main/kotlin/com/example/di/core/NormalizationPipeline.kt`
* `03-dependency-injection/src/main/kotlin/com/example/di/api/GreetingController.kt` (add endpoint)

**Verification**

```bash
curl -s --get "http://localhost:8080/api/greet/normalize" --data-urlencode "name=   huy    nguyen   "
# expected: {"message":"HELLO, HUY NGUYEN"}
```

**Acceptance criteria**

* Pipeline uses **all** rules in the specified order.
* Response is **200** and equals `{"message":"HELLO, HUY NGUYEN"}`.

---

## Problem C — Bean Registry (`Map<String, T>`)

**Goal**
Pick a formatter **by bean name** at runtime.

**Requirements**

* Create `FormatterRegistry` that injects `Map<String, MessageFormatter>`.
* Endpoint (GET): `/greet/by-name?strategy={beanName}&name={name}`.

  * When `strategy=shoutFormatter`, it must use `ShoutFormatter`.
  * If `strategy` is unknown → respond **400** with JSON: `{"error":"Unknown formatter: <name>"}`.

**Constraints**

* Do not use reflection or manual conditionals for known names; rely on the injected **map keys**.
* Controller must translate unknown strategy to **400** (no stack traces in body).

**Files to create/update**

* `03-dependency-injection/src/main/kotlin/com/example/di/core/FormatterRegistry.kt`
* `03-dependency-injection/src/main/kotlin/com/example/di/api/GreetingController.kt` (add endpoint)

**Verification**

```bash
curl -s "http://localhost:8080/api/greet/by-name?strategy=shoutFormatter&name=Huy"
# expected: {"message":"HELLO, HUY!"}

curl -i -s "http://localhost:8080/api/greet/by-name?strategy=unknown&name=Huy"
# expected status: 400
# expected body: {"error":"Unknown formatter: unknown"}
```

**Acceptance criteria**

* Known strategy returns **200** and correct body.
* Unknown strategy returns **400** with the specified JSON error.
* `Map<String, MessageFormatter>` injection is used (not manual registry).

---

### Commands (for all problems)

```bash
./gradlew :03-dependency-injection:bootRun
```

---

### Notes

* `@Primary` affects **single-bean** injection only (not list order).
* Use `@Order` for deterministic pipeline composition.
* Prefer **constructor injection**; keep beans **stateless**.
