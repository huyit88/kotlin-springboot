#  Topic 20: File Upload & Download

---

### Dependencies

Add only what’s new for file handling:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

---

### Problem A — Single File Upload

#### Requirement

Create an endpoint to accept one uploaded file.

* Endpoint: `POST /api/files`
* Accept multipart form with parameter `file`.
* Store uploaded file under local folder `uploads/`.
* Respond with:

  * `201 Created`
  * Header `Location: /api/files/{filename}`

#### Acceptance criteria

* File physically saved at `uploads/{originalFilename}`.
* Re-upload with same name overwrites.
* Response returns instantly (no heavy processing).
* Works from both `curl` and browser form.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.*
```

#### Command to verify/run

```bash
mkdir -p uploads
./gradlew :20-file-upload-download:bootRun
curl -F "file=@README.md" http://localhost:8080/api/files -i
```

---

### Problem B — File Download (Attachment)

#### Requirement

Expose an endpoint to serve the previously uploaded file.

* Endpoint: `GET /api/files/{name}`
* Read from `uploads/{name}`.
* Return binary content as attachment.
* Headers:

  * `Content-Disposition: attachment; filename={name}`
  * Correct `Content-Type` via `Files.probeContentType(...)`.

#### Acceptance criteria

* Browser triggers a **download**, not inline view.
* Status `200 OK`.
* Non-existent file → `404`.

#### Suggested Import Path

```kotlin
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import java.nio.file.*
```

#### Command to verify/run

```bash
curl -OJ http://localhost:8080/api/files/README.md
```

---

### Problem C — Multiple File Upload

#### Requirement

* Endpoint: `POST /api/files/many`
* Accept several files in parameter `files`.
* Store all into `uploads/`.
* Return JSON array of stored filenames.

#### Acceptance criteria

* All files written successfully.
* Empty list → `400 Bad Request`.
* Response JSON sample:

  ```json
  ["a.txt","b.txt"]
  ```

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.*
```

#### Command to verify/run

```bash
curl -F "files=@.gitignore" -F "files=@build.gradle.kts" http://localhost:8080/api/files/many
```

---

### Problem D — Streaming Download for Large Files

#### Requirement

* Endpoint: `GET /api/files/stream/{name}`
* Implement using `StreamingResponseBody`.
* Stream file chunks progressively (avoid buffering entire file).
* Return as attachment.

#### Acceptance criteria

* File download starts instantly even for large files.
* Memory usage remains low (verified via logs/metrics).
* `Content-Disposition` and `Content-Type` set properly.
* Still returns `404` for missing files.

#### Suggested Import Path

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.http.*
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.nio.file.*
```

#### Command to verify/run

```bash
curl -OJ http://localhost:8080/api/files/stream/heavy-img.jpg
```
