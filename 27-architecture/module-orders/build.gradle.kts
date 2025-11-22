plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dep.mgmt)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(project(":27-architecture:module-catalog"))
    implementation(project(":27-architecture:module-customer"))
}

