# CHALLENGE.md — Topic 29: Deployment with Docker

---

### Dependencies

No new Gradle dependencies are required for this topic.
You only need:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator") // for health endpoint
}
```

(Actuator is just to give Docker a real health check target.)

---

## Problem A — Multi-stage Docker Build & Run

#### Requirement

Containerize your existing Spring Boot app using a **multi-stage Dockerfile**.

* Create `Dockerfile` in the module root (e.g. `30-docker/`):

  1. **Build stage**

     * Base image: `gradle:8-jdk21`
     * Copy Gradle project into `/app`.
     * `WORKDIR /app`.
     * Run `gradle bootJar --no-daemon`.
  2. **Run stage**

     * Base image: `eclipse-temurin:21-jre`.
     * Copy fat jar from `/app/build/libs/*.jar` to `/app/app.jar`.
     * Expose port `8080`.
     * `ENTRYPOINT ["java", "-jar", "/app/app.jar"]`.

* App must still listen on port `8080` inside the container.

#### Acceptance criteria

* `docker build` succeeds without using the local Gradle cache dirs from host.
* Running container with `-p 8080:8080` allows:

  * `GET /actuator/health` → `200`.
  * `GET /api/ping` (or any simple endpoint you already have) → `200`.

#### Suggested Import Path

*(No new Kotlin imports; use existing controllers.)*

#### Command to verify/run

```bash
# From 30-docker module root (where Dockerfile is)
docker build -t demo-app:local .

docker run --rm -p 8080:8080 demo-app:local

# In another terminal
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/api/ping
```

---

## Problem B — Run as Non-root + Externalized Config

#### Requirement

Harden the container and wire configuration via environment variables.

Update your **run stage** in `Dockerfile`:

* Create a non-root user:

  ```dockerfile
  RUN useradd -r -u 1001 appuser
  USER appuser
  ```
* Keep jar at `/app/app.jar`.
* Ensure the app binds to port `8080` as non-root (default is fine).
* Use **environment variables** for config:

  * `SPRING_PROFILES_ACTIVE=docker`
  * `SERVER_PORT=8080` (explicit)
* Define `application-docker.yml`:

  ```yaml
  server:
    port: 8080

  management:
    endpoints:
      web:
        exposure:
          include: health,info
  ```

#### Acceptance criteria

* Running the container without special privileges still works.
* When started with `SPRING_PROFILES_ACTIVE=docker`, the app:

  * Uses the `docker` profile (log it at startup or expose via `/actuator/env`).
  * Still responds on `8080`.
* Removing the profile environment variable falls back to default profile.

#### Suggested Import Path

*(No extra Kotlin imports; config-only.)*

#### Command to verify/run

```bash
docker build -f 29-docker-deployment/Dockerfile -t demo-app:nonroot .

# With docker profile
docker run --rm \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SERVER_PORT=8080 \
  -p 8080:8080 \
  demo-app:nonroot

curl -s http://localhost:8080/actuator/health
```

---

## Problem C — Docker Compose: App + Environment Variables

#### Requirement

Use **docker-compose** to manage your app container and environment in one file.

* Create `docker-compose.yml` in module root:

  ```yaml
  version: "3.9"
  services:
    app:
      image: demo-app:nonroot
      container_name: demo-app
      ports:
        - "8080:8080"
      environment:
        SPRING_PROFILES_ACTIVE: docker
        SERVER_PORT: 8080
  ```

* Ensure the image name matches what you built in Problem B (`demo-app:nonroot`).

#### Acceptance criteria

* Running `docker compose up`:

  * Starts the container.
  * Exposes `8080` on your host.
  * Uses `docker` profile (same behavior as Problem B).
* `docker compose down` stops and removes the container cleanly.

#### Suggested Import Path

*(N/A — compose/config only.)*

#### Command to verify/run

```bash
# Build image first
docker build -t demo-app:nonroot .

# Start with compose
docker compose up

# In another terminal:
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/api/ping

# Tear down
docker compose down
```
