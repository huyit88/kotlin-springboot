# Challenge 10: pagination-sorting

## Description
A new Kotlin Spring Boot challenge

## Getting Started

### Project Structure
```
10-pagination-sorting/
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
./gradlew :10-pagination-sorting:bootRun
```

### Test the Application
```bash
# Run all tests
./gradlew :10-pagination-sorting:test

# Build the project
./gradlew :10-pagination-sorting:build
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
