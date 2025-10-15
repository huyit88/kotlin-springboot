// This module inherits configuration from the parent build.gradle.kts
// Add any module-specific dependencies or configurations here if needed

dependencies {
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.caffeine)
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}
