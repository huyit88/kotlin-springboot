// This module inherits configuration from the parent build.gradle.kts
// Add any module-specific dependencies or configurations here if needed
dependencies {    
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.h2.database)
}

plugins {
    alias(libs.plugins.kotlin.spring.jpa)
}