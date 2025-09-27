// This module inherits configuration from the parent build.gradle.kts
// Add any module-specific dependencies or configurations here if needed

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.flywaydb)
    implementation(libs.kotlin.reflect)
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.junitJupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotlin.test)
    
}

plugins {
    alias(libs.plugins.kotlin.spring.jpa)
}