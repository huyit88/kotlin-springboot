# Challenge 02: rest-api

## Description
Create a REST API with CRUD operations

## Getting Started

### Project Structure
```
02-rest-api/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/     # Your Kotlin source files go here
│   │   └── resources/
│   │       └── application.yml     # Application configuration
│   └── test/
│       ├── kotlin/com/example/     # Your test files go here
│       └── resources/
│           └── application-test.yml # Test configuration
├── build.gradle.kts                # Module-specific dependencies
└── README.md                       # This file
```

### Implementation Steps
1. Create your main Spring Boot application class in `src/main/kotlin/com/example/`
2. Implement your controllers, services, and other components
3. Add any required dependencies to `build.gradle.kts`
4. Write tests in `src/test/kotlin/com/example/`

### Run the Application
```bash
./gradlew :02-rest-api:bootRun
```

### Test the Application
```bash
# Run all tests
./gradlew :02-rest-api:test

# Build the project
./gradlew :02-rest-api:build
```

## Challenge Tasks

### Basic Requirements
- [ ] Create main Spring Boot application class
- [ ] Implement the core functionality
- [ ] Add proper error handling
- [ ] Write comprehensive tests
- [ ] Add API documentation

### Advanced Requirements
- [ ] Add input validation
- [ ] Implement logging
- [ ] Add metrics/monitoring
- [ ] Performance optimization

## Common Dependencies

Add these to your `build.gradle.kts` as needed:

```kotlin
dependencies {
    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2") // or your preferred database
    
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

## Notes
- Add your implementation notes here
- Document any assumptions or design decisions
- Include references to useful resources

## Resources
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/)


### Commands
Totally—here’s a quick **curl cheat sheet** to verify your `02-rest-api` endpoints.
(Assumes the app runs on `http://localhost:8080`.)

---

## 1) List all books

```bash
curl -sS http://localhost:8080/api/books | jq
```

Expected (initial seed):

```json
[
  { "id": 1, "title": "Kotlin in Action" }
]
```

## 2) Get by id (exists)

```bash
curl -sS http://localhost:8080/api/books/1 | jq
```

Expected:

```json
{ "id": 1, "title": "Kotlin in Action" }
```

## 3) Get by id (not found)

```bash
curl -i http://localhost:8080/api/books/9999
```

Look for:

```
HTTP/1.1 404
```

(If you added the optional `@ControllerAdvice`, you’ll also see a JSON body like `{"status":404,"message":"Book 9999 not found"}`.)

## 4) Create a book (POST)

```bash
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{ "title": "Clean Architecture" }'
```

Look for:

```
HTTP/1.1 200
```

…and a JSON body like:

```json
{ "id": 2, "title": "Clean Architecture" }
```

### (Optional) Return 201 + Location

If you modify your POST to return `201 Created` and set `Location`, then check:

```
HTTP/1.1 201
Location: /api/books/2
```

Follow it:

```bash
curl -sS http://localhost:8080/api/books/2 | jq
```

## 5) Simple pagination (if you implemented `page`/`size`)

```bash
curl -sS "http://localhost:8080/api/books?page=0&size=2" | jq
```

## 6) Delete a book (if you added DELETE)

```bash
curl -i -X DELETE http://localhost:8080/api/books/2
```

Look for:

```
HTTP/1.1 204
```

Verify it’s gone:

```bash
curl -i http://localhost:8080/api/books/2
# expect 404
```

---

### Handy tips

* Add `-i` to any command to see **status code** and **headers**.
* Add `| jq` to pretty-print JSON (install with `brew install jq` on macOS).
* For quick testing with different payloads:

```bash
TITLE="Domain-Driven Design"
curl -sS -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{ \"title\": \"${TITLE}\" }" | jq
```

If you want, I can also give you the curl set for **PUT/PATCH** once you add those endpoints.
