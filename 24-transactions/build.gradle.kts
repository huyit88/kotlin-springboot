dependencies {    
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.h2.database)
}

plugins {
    alias(libs.plugins.kotlin.spring.jpa)
}