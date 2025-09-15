// This module inherits configuration from the parent build.gradle.kts
// Add any module-specific dependencies or configurations here if needed

dependencies {    
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.h2.database)
}

plugins {
    kotlin("plugin.jpa") version "2.2.0"
}

/**
noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}
*/