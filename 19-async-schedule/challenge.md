# Topic 19: Async & Scheduling

---

### Dependencies

Already available in `spring-boot-starter`.
If missing, add:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}
```

---

### Problem A — Fire-and-Forget Async Task

#### Requirement

Create a service that simulates sending an email asynchronously.

* Enable async processing via `@EnableAsync`.
* Add an endpoint `POST /api/notify/{email}` that triggers an async method.
* The async method should:

  * Sleep for 1 second (simulate heavy work).
  * Log the thread name and the email sent.
* The controller returns immediately (`202 Accepted` + body: `"Dispatched to <email>"`).

#### Acceptance criteria

* API responds instantly (<200 ms) with `202`.
* Logs show the async method executes on a thread named `async-*`.
* Async code does **not** block the HTTP thread.

#### Suggested Import Path

```kotlin
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.Service
import org.springframework.context.annotation.Bean
```

#### Command to verify/run

```bash
./gradlew :19-async-schedule:bootRun
curl -X GET http://localhost:8080/api/notify/test@example.com -i
```

---

### Problem B — Return a Computed Value (CompletableFuture)

#### Requirement

Add a new async endpoint `/api/compute/{n}`:

* Returns immediately with HTTP 202.
* Launches async computation using `@Async` that:

  * Squares the number `n`.
  * Sleeps 500 ms.
  * Returns result as `CompletableFuture<Int>`.
* Add another endpoint `/api/result/{n}` returning the **last computed result** from memory if available (blocking read).

#### Acceptance criteria

* Async computation doesn’t block request.
* Result accessible later through `/api/result/{n}`.
* Logs show separate thread pool usage (`async-*` threads).

#### Suggested Import Path

```kotlin
import java.util.concurrent.CompletableFuture
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
```

#### Command to verify/run

```bash
curl -i -X POST http://localhost:8080/api/compute/5
sleep 1
curl -i -X GET  http://localhost:8080/api/result/5
```

---

### Problem C — Scheduled Heartbeat

#### Requirement

Create a scheduled task that prints a “heartbeat” every 5 seconds.

* Enable scheduling via `@EnableScheduling`.
* Log message format: `"Heartbeat at <timestamp> on <thread>"`.
* Use `@Scheduled(fixedRate = 5000)`.

#### Acceptance criteria

* After startup, logs show periodic heartbeat messages.
* Thread name starts with `scheduling-`.
* Continues running without blocking other tasks.

#### Suggested Import Path

```kotlin
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
```

#### Command to verify/run

```bash
./gradlew :19-async-schedule:bootRun
# Observe logs every 5s
```

---

### Problem D — Cron Job for Cleanup

#### Requirement

Create a scheduled cleanup that runs **every minute** (using cron).

* Prints “Cleaning temporary files...” with a timestamp.
* Simulate 2 s workload using `Thread.sleep(2000)`.
* Ensure overlapping runs don’t occur (avoid double execution).

#### Acceptance criteria

* Cleanup runs once per minute.
* Overlaps prevented (use simple `AtomicBoolean` lock).
* Logs show start and end times clearly.

#### Suggested Import Path

```kotlin
import java.util.concurrent.atomic.AtomicBoolean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
```

#### Command to verify/run

```bash
./gradlew :19-async-schedule:bootRun
# Observe: “Cleaning temporary files…” appears once per minute
```
