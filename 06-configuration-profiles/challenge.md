# 06-configuration-profiles — Challenge 1: Switch behavior and properties by profile

### Dependencies
- _New deps for this challenge (if not already at root):_
  - **Optional (IDE metadata for `@ConfigurationProperties`)**  
    - Version catalog: `kapt(libs.spring.boot.configuration.processor)`  
    - Direct: `kapt("org.springframework.boot:spring-boot-configuration-processor:3.3.4")`  
  - _Runtime behavior works without this; it only generates metadata for IDE help._

---

### Problem A — Profile-specific properties with `@ConfigurationProperties`

#### Requirement
- Create a typed properties class `BillingProperties(currency: String, taxRate: Double)` bound to prefix `billing`.
- Provide base values in `application.yml` and overrides in `application-dev.yml`.
- Expose `GET /api/config/billing` returning `{"currency":"...","taxRate":...}`.
- Must use `@ConfigurationProperties("billing")` and enable scanning (`@ConfigurationPropertiesScan`).
- No code changes when switching profiles; values come from profile files.

#### Acceptance criteria
- With **no active profile**: response uses base values (e.g., `USD`, `0.10`).
- With `--args="--spring.profiles.active=dev"`: response uses dev overrides (e.g., `EUR`, `0.20`).
- Uses constructor injection; no static/global access to `Environment`.

#### Suggested Import Path
```kotlin
// Properties
import org.springframework.boot.context.properties.ConfigurationProperties

// Config
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

// Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
````

#### Command to verify/run

```bash
# Base (no profile)
./gradlew :06-configuration-profiles:bootRun
curl -s http://localhost:8080/api/config/billing
# expect: {"currency":"USD","taxRate":0.10}

# Dev profile
./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=dev"
SPRING_PROFILES_ACTIVE=dev ./gradlew :06-configuration-profiles:bootRun
curl -s http://localhost:8080/api/config/billing
# expect: {"currency":"EUR","taxRate":0.20}
```

---

### Problem B — Conditional beans with `@Profile`

#### Requirement

* Define `Mailer` interface with two beans:

  * `InMemoryMailer` annotated `@Profile("dev")`.
  * `SmtpMailer` annotated `@Profile("prod")`.
* Controller endpoint `GET /api/mail/ping` returns `{"impl":"in-memory"}` or `{"impl":"smtp"}` depending on the active profile.
* Ensure the application starts in both profiles and selects the correct bean.

#### Acceptance criteria

* With `--args="--spring.profiles.active=dev"` → endpoint returns in-memory impl.
* With `--args="--spring.profiles.active=prod"` → endpoint returns SMTP impl.
* No bean definition conflicts; constructor injection only.

#### Suggested Import Path

```kotlin
// Beans
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

// Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
# Dev
./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=dev"
curl -s http://localhost:8080/api/mail/ping
# expect: {"impl":"in-memory"}

# Prod
./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=prod"
curl -s http://localhost:8080/api/mail/ping
# expect: {"impl":"smtp"}
```

---

### Problem C — Multiple profiles & override precedence

#### Requirement

* Add `application-dev.yml` and `application-featureX.yml` defining the same key:

  * `app.color` = `"green"` in **dev**
  * `app.color` = `"red"` in **featureX**
* Expose `GET /api/config/color` returning `{"color":"..."}`.
* Demonstrate that activation **order** controls which wins:

  * `--args="--spring.profiles.active=dev,featureX"` → `"red"`
  * `--args="--spring.profiles.active=featureX,dev"` → `"green"`

#### Acceptance criteria

* Endpoint reflects the override from the **last-loaded profile** based on activation order.
* Changing order flips the response deterministically.
* Adding an env var `APP_COLOR=purple` overrides both files.

#### Suggested Import Path

```kotlin
// Read via @Value or ConfigurationProperties (pick one; keep consistent)
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
# dev,featureX
./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=dev,featureX"
curl -s http://localhost:8080/api/config/color
# expect: {"color":"red"}

# featureX,dev
./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=featureX,dev"
curl -s http://localhost:8080/api/config/color
# expect: {"color":"green"}

# Env var overrides
APP_COLOR=purple ./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=dev,featureX"
curl -s http://localhost:8080/api/config/color
# expect: {"color":"purple"}
```

---

### Problem D — Profile groups (one switch → many profiles)

#### Requirement

* In `application.yml`, define a group:
  `spring.profiles.group.prod-full = [prod, tracing, metrics]`
* Create `application-tracing.yml` and `application-metrics.yml` with simple booleans:

  * `observability.tracing.enabled: true`
  * `observability.metrics.enabled: true`
* Expose `GET /api/config/active` returning:

  * `{"active":["prod","tracing","metrics"]}` when running with `-Dspring.profiles.active=prod-full`.
* Also expose `GET /api/config/observability` returning tracing/metrics flags.

#### Acceptance criteria

* With `-Dspring.profiles.active=prod-full`, group expands and both flags resolve to `true`.
* Without group, flags default per `application.yml` (e.g., `false`).

#### Suggested Import Path

```kotlin
// Environment & profiles
import org.springframework.core.env.Environment

// Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
```

#### Command to verify/run

```bash
# Group activation
./gradlew :06-configuration-profiles:bootRun --args="--spring.profiles.active=prod-full"
curl -s http://localhost:8080/api/config/active
# expect: {"active":["prod","tracing","metrics"]}

curl -s http://localhost:8080/api/config/observability
# expect: {"tracing":true,"metrics":true}
```
