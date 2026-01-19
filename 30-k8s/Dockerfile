# Build stage
FROM gradle:8-jdk21 AS builder
# Copy root Gradle configuration files
COPY build.gradle.kts settings.gradle.kts /app/
COPY gradle /app/gradle
# Copy the module we need to build
COPY 30-k8s /app/30-k8s
WORKDIR /app
RUN gradle :30-k8s:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/30-k8s/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]