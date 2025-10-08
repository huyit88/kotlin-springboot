# 15-security-basics — Challenge 1 (Stateless + Roles + Clean 401/403)

### Dependencies
_Add only if this subproject doesn’t already have them._
- Version catalog (preferred):
  - `implementation(libs.spring.boot.starter.security)`
  - `implementation(libs.spring.boot.starter.web)`
  - `testImplementation(libs.spring.boot.starter.test)`
- Direct coordinates (alternative):
  - `implementation("org.springframework.boot:spring-boot-starter-security:3.3.4")`
  - `implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")`
  - `testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")`

> Run this module on port **8080** to avoid clashes.

---

### Problem A — Minimal security chain (public health, auth everywhere else)

#### Requirement
- Create `SecurityConfig` with **stateless** HTTP (`STATELESS`), **CSRF disabled**, and **HTTP Basic** enabled (for demo).
- Permit `GET /health` (controller returns `{"status":"UP"}`).
- Require **authentication** for any other path (e.g., `GET /api/secure/ping` should be protected).

#### Acceptance criteria 
- `GET /health` → **200** without credentials.
- `GET /api/secure/ping`:
  - Without credentials → **401** and `WWW-Authenticate: Basic` header.
  - With valid credentials (from Problem B) → **200**.

#### Suggested Import Path
```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.bind.annotation.*
````

#### Command to verify/run

```bash
./gradlew :15-security-basics:bootRun

curl -i http://localhost:8080/health
curl -i http://localhost:8080/api/secure/ping
```

---

### Problem B — In-memory users + role-based **path** rules

#### Requirement

* Add a `PasswordEncoder` (BCrypt).
* Configure an `InMemoryUserDetailsManager` with:

  * user: `user` / `pass` → role `USER`
  * user: `admin` / `pass` → role `ADMIN`
* Path rules:

  * `GET /api/users/**` → roles `USER` or `ADMIN`
  * `DELETE /api/users/{id}` → role `ADMIN` only
* Implement minimal controller methods for those routes (can return simple JSON stubs).

#### Acceptance criteria

* `GET /api/users` with `user:pass` → **200**.
* `DELETE /api/users/1` with `user:pass` → **403**.
* `DELETE /api/users/1` with `admin:pass` → **200** (or **204**).

#### Suggested Import Path

```kotlin
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.http.ResponseEntity
```

#### Command to verify/run

```bash
# GET allowed for USER
curl -i -u user:pass http://localhost:8080/api/users

# DELETE forbidden for USER
curl -i -X DELETE -u user:pass http://localhost:8080/api/users/1

# DELETE allowed for ADMIN
curl -i -X DELETE -u admin:pass http://localhost:8080/api/users/1

# SECURE PING
curl -i -X DELETE -u user:pass  http://localhost:8080/api/secure/ping
# PONG
```

---

### Problem C — **Method** security with @PreAuthorize

#### Requirement

* Enable method security: `@EnableMethodSecurity`.
* Create a service method, e.g., `UserAdminService.get(id)` annotated `@PreAuthorize("hasRole('ADMIN')")`.
* Route `GET /api/users/{id}/sensitive` should delegate to this service (from Problem B), so **both** path rule and method rule protect it.

#### Acceptance criteria

* `GET /api/users/1/sensitive` with `user:pass` → **403** because method guard denies it.
* `GET /api/users/1/sensitive` with `admin:pass` → succeeds.

#### Suggested Import Path

```kotlin
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
```

#### Command to verify/run

```bash
curl -i -X GET -u user:pass  http://localhost:8080/api/users/1/sensitive
curl -i -X GET -u admin:pass http://localhost:8080/api/users/1/sensitive
```

---

### Problem D — Clean **401/403** JSON (Problem Details)

#### Requirement

* Implement a custom `AuthenticationEntryPoint` (401) and `AccessDeniedHandler` (403) that return
  `application/problem+json` with fields:

  * `title`: `"Unauthorized"` / `"Forbidden"`
  * `status`: 401 / 403
  * `detail`: short message (e.g., “Missing or invalid credentials”, “Insufficient permissions”)
  * Optional: include request path as `instance`.
* Wire them via `http.exceptionHandling{}` in `SecurityConfig`.
* Keep HTTP Basic for auth, but responses for failures must be JSON.

#### Acceptance criteria

* Unauthenticated request to `/api/secure/ping` → **401** with `Content-Type: application/problem+json` and the JSON body.
* Forbidden request (`DELETE /api/users/1` with `user:pass`) → **403** with the same JSON shape.

#### Suggested Import Path

```kotlin
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
```

#### Command to verify/run

```bash
# 401 as problem+json
curl -s -D - http://localhost:8080/api/secure/ping

# 403 as problem+json
curl -i -s -D -X DELETE -u user:pass http://localhost:8080/api/users/1
```

---

### Problem E — CORS for browser clients (preflight)

#### Requirement

* Add a `CorsConfigurationSource` bean allowing:

  * Origin: `http://localhost:3000`
  * Methods: `GET, POST, PUT, PATCH, DELETE`
  * Headers: `*`
* Enable CORS in `SecurityConfig` (`http.cors{}`).
* Add an endpoint `POST /api/users` (stub OK) to test preflight.

#### Acceptance criteria

* Preflight (`OPTIONS`) from allowed origin returns **200** with:

  * `Access-Control-Allow-Origin: http://localhost:3000`
  * `Access-Control-Allow-Methods` contains `POST`
* Simple cross-origin `POST` from that origin includes `Access-Control-Allow-Origin` header in response.

#### Suggested Import Path

```kotlin
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
```

#### Command to verify/run

```bash
# Invalid CORS request
curl -i -X OPTIONS "http://localhost:8080/api/users" \
  -H "Origin: http://localhost:3001" \
  -H "Access-Control-Request-Method: POST"

# Preflight
curl -i -X OPTIONS "http://localhost:8080/api/users" \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST"

# Simple request (should succeed with proper creds/roles if protected)
curl -i -u user:pass -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -d '{"name":"Huy"}' \
  http://localhost:8080/api/users
```
