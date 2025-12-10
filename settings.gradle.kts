rootProject.name = "kotlin-springboot"

rootDir
  .listFiles { f -> f.isDirectory && f.name.matches(Regex("""\d{2}-.*""")) }
  ?.sortedBy { it.name }
  ?.forEach { include(":${it.name}") }

// Multi-module structure for Problem E (under 27-architecture)
include(":27-architecture:module-catalog")
include(":27-architecture:module-customer")
include(":27-architecture:module-orders")
include(":27-architecture:app")

// Microservices (under 28-microservices)
include(":28-microservices:customers-service")
include(":28-microservices:orders-service")
