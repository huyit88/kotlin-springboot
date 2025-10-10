// This module inherits configuration from the parent build.gradle.kts
// Add any module-specific dependencies or configurations here if needed

dependencies {
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.jackson.kotlin)
  implementation(libs.java.jwt)
  implementation(libs.springdoc.openapi.starter.webmvc.ui)
  testImplementation(libs.spring.boot.starter.test)
}
