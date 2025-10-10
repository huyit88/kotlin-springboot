````markdown
# 16-jwt-auth — Challenge 1 (Issue + Verify HS256 JWT, stateless)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.security)`
  - `implementation(libs.spring.boot.starter.web)`
  - `implementation(libs.jackson.kotlin)`
  - `implementation(libs.java.jwt)`
  - `testImplementation(libs.spring.boot.starter.test)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-security:3.3.4")`
  - `implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")`
  - `implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")`
  - `implementation("com.auth0:java-jwt:4.4.0")`
  - `testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")`

> Run on **port 8080** for this module.

---

## Problem A — Stateless Security Chain (permit login/docs/health)

### Requirement
- Configure `SecurityFilterChain`:
  - **CSRF disabled**, **STATELESS** sessions, **cors{}** enabled.
  - Permit: `POST /auth/login`, `GET /health`, `/swagger-ui/**`, `/v3/api-docs/**`.
  - Everything else requires **authentication**.
- Keep path examples:
  - `/api/secure/ping` → authenticated (any role).
  - `/api/admin/secret` → `ROLE_ADMIN` only.

### Acceptance criteria
- `GET /health` → 200 without token.
- `GET /api/secure/ping` without token → **401**.
- `GET /api/admin/secret` without token → **401**.

### Suggested Import Path
```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.http.HttpMethod
````

### Command to verify/run

```bash
./gradlew :16-jwt-auth:bootRun

curl -i http://localhost:8080/health
curl -i http://localhost:8080/api/secure/ping
curl -i http://localhost:8080/api/admin/secret
```

---

## Problem B — JWT Properties + Provider (HS256)

### Requirement

* Add `@ConfigurationProperties("jwt")`:

  * `secret: String` (base64 or plain), `issuer: String`, `audience: String`, `ttlSeconds: Long` (default **900**).
* Create a **JwtProvider** component:

  * `fun createToken(sub: String, roles: List<String>, now: Instant = Instant.now(), ttlSeconds: Long = props.ttlSeconds): String`

    * Set standard claims: `sub`, `iss`, `aud`, `iat`, `exp`.
    * Add custom claim `roles: [ "USER", "ADMIN" ]`.
    * Sign with **HS256** using `secret`.
  * `fun verify(token: String): DecodedJWT`

    * Verify signature/`exp`/`iss`/`aud`. Throw on failure.

### Acceptance criteria

* Creating and verifying a token with correct secret/claims works.
* Wrong `iss`/`aud`/signature → verification fails.

### Suggested Import Path

```kotlin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant
```

### Command to verify/run

```bash
# No runtime curl yet; exercised via Problem C/D endpoints after wiring.
```

---

## Problem C — Login endpoint (issue token)

### Requirement

* In-memory users (or reuse your existing in-memory config):

  * `user/pass` → ROLE_USER
  * `admin/pass` → ROLE_ADMIN
* `POST /auth/login` accepts JSON `{ "username": "...", "password": "..." }`.

  * Authenticate against `AuthenticationManager` (or manual check with `PasswordEncoder` for this exercise).
  * On success: issue JWT with `sub=username`, `roles` claim (no `ROLE_` prefix in claim), and **return**:

    ```json
    { "accessToken": "<jwt>" }
    ```
  * On failure → **401** (Problem Details if you already wired it).

### Acceptance criteria

* Correct credentials return **200** with JSON body containing `accessToken`.
* Wrong credentials return **401**.

### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import com.fasterxml.jackson.annotation.JsonProperty
```

### Command to verify/run

```bash
# login as user
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}' | jq -r .accessToken)
echo "$TOKEN" | cut -c1-30

# login as admin
ADM=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"pass"}' | jq -r .accessToken)
echo "$ADM" | cut -c1-30
```

---

## Problem D — JWT Filter (verify + set SecurityContext)

### Requirement

* Create a once-per-request filter:

  * Read `Authorization: Bearer <token>`.
  * Use `JwtProvider.verify(...)`.
  * Map `roles` claim → authorities: `ROLE_` + value (e.g., `"ADMIN"` → `"ROLE_ADMIN"`).
  * Set `UsernamePasswordAuthenticationToken(principal=sub, credentials=null, authorities=...)` into `SecurityContext`.
* Register filter **before** `UsernamePasswordAuthenticationFilter`.
* Controller endpoints:

  * `GET /api/secure/ping` → returns `{ "ok": true, "sub": "<subject>" }`.
  * `GET /api/admin/secret` → returns `{ "secret": "42" }` (ADMIN only).

### Acceptance criteria

* Call `/api/secure/ping` with `Authorization: Bearer $TOKEN` → **200** with your `sub`.
* Call `/api/admin/secret` with `$TOKEN` (user) → **403**.
* Call `/api/admin/secret` with `$ADM` (admin) → **200**.

### Suggested Import Path

```kotlin
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter
```

### Command to verify/run

```bash
# user token works for /api/secure/ping
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/secure/ping

# user token forbidden on admin path
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/admin/secret

# admin token allowed
curl -s -H "Authorization: Bearer $ADM" http://localhost:8080/api/admin/secret
```

---

## Problem E — Expiration & clock leeway (no waiting)

### Requirement

* Extend `/auth/login` to support an **optional** query param `ttlSeconds` (for testing only).

  * If present, override default TTL when issuing the token (allow `0` and very small values).
* The filter must reject **expired** tokens with **401** (Problem Details if wired).
* Add a quick negative test:

  * Obtain a token with `ttlSeconds=0`.
  * Use it immediately → should be **401** due to `exp` ≤ `now`.

### Acceptance criteria

* `POST /auth/login?ttlSeconds=0` returns a token.
* Using that token on `/api/secure/ping` returns **401** with “expired” (or a generic invalid-token message).

### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.RequestParam
```

### Command to verify/run

```bash
EXP=$(curl -s -X POST "http://localhost:8080/auth/login?ttlSeconds=0" \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}' | jq -r .accessToken)

# should be 401 (expired)
curl -i -H "Authorization: Bearer $EXP" http://localhost:8080/api/secure/ping
```

---

## Problem F — Error JSON (reuse your Problem Details handlers)

### Requirement

* Reuse your previous **Security 401/403** JSON handlers:

  * Missing/invalid/expired token → **401** `application/problem+json`
  * Authenticated but forbidden → **403** `application/problem+json`
* Keep messages short; no stack traces.

### Acceptance criteria

* 401/403 responses match your global style (Problem Details).

### Suggested Import Path

```kotlin
// reuse from previous module; otherwise implement AuthenticationEntryPoint & AccessDeniedHandler
```

### Command to verify/run

```bash
# 401: no token
curl -i http://localhost:8080/api/secure/ping

# 403: user token to admin path
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/admin/secret
```

---

### Notes

* **Claim mapping**: claim `roles: ["USER","ADMIN"]` → authorities `ROLE_USER`, `ROLE_ADMIN`.
* **Strict validation**: check `iss`, `aud`, `exp`. Add small leeway if your clock is off.
* **Secrets**: load from config; never hardcode. For real systems, prefer **RS256** with key rotation (out of scope for this step).
* **CORS**: if your SPA calls these endpoints, ensure CORS is configured (from prior topic).
