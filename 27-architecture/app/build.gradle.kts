plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.mgmt)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.h2.database)
    implementation(project(":27-architecture:module-catalog"))
    implementation(project(":27-architecture:module-customer"))
    implementation(project(":27-architecture:module-orders"))
}

