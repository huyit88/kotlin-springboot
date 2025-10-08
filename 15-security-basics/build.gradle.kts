// This module inherits configuration from the parent build.gradle.kts
// Add any module-specific dependencies or configurations here if needed

dependencies {
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.test)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
}
