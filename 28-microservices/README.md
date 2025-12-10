# Challenge 28: Microservices

## Description
This challenge implements two independent microservices: `customers-service` and `orders-service`, demonstrating service-to-service communication, fault isolation, and API gateway patterns.

## Project Structure
```
28-microservices/
├── customers-service/          # Customers microservice (port 8081)
│   ├── src/main/kotlin/com/example/customers/
│   │   ├── Application.kt
│   │   ├── Customer.kt
│   │   ├── CustomerController.kt
│   │   └── CustomerService.kt
│   ├── src/main/resources/application.yml
│   └── build.gradle.kts
├── orders-service/             # Orders microservice (port 8082)
│   ├── src/main/kotlin/com/example/orders/
│   │   ├── Application.kt
│   │   ├── Order.kt
│   │   ├── OrderController.kt
│   │   ├── OrderService.kt
│   │   ├── CustomerClientService.kt
│   │   ├── WebClientConfig.kt
│   │   └── dto/
│   │       ├── RemoteCustomerDto.kt
│   │       ├── OrderDetailsResponse.kt
│   │       └── OrderSummaryResponse.kt
│   ├── src/main/resources/application.yml
│   └── build.gradle.kts
├── challenges.md               # Detailed challenge requirements
└── README.md                   # This file
```

## Running the Services

### Start Customers Service (Terminal 1)
```bash
cd customers-service
../gradlew bootRun
```
Service runs on **http://localhost:8081**

### Start Orders Service (Terminal 2)
```bash
cd orders-service
../gradlew bootRun
```
Service runs on **http://localhost:8082**

## API Endpoints

### Customers Service (Port 8081)
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/slow/{id}` - Get customer with 3s delay (for timeout testing)

### Orders Service (Port 8082)
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/{id}/details` - Get order with customer details (calls customers-service)
- `GET /api/client/orders/{id}/summary` - Get order summary (gateway pattern)

## Testing

### Problem A - Basic Services
```bash
# Test customers service
curl -s http://localhost:8081/api/customers/1

# Test orders service
curl -s http://localhost:8082/api/orders/100
```

### Problem B - Service-to-Service Call
```bash
# Get order details (calls customers-service)
curl -s http://localhost:8082/api/orders/100/details | jq

# Test with customers-service down (should return 502)
curl -i http://localhost:8082/api/orders/100/details
```

### Problem D - Timeout Handling
```bash
# Direct slow endpoint (takes ~3s)
curl -w "\nTime: %{time_total}\n" -s http://localhost:8081/api/customers/slow/1

# Through orders-service (should timeout in ~1s with 504)
curl -w "\nTime: %{time_total}\n" -i http://localhost:8082/api/orders/100/details
```

### Problem E - API Gateway Pattern
```bash
# Get order summary (flattened response)
curl -s http://localhost:8082/api/client/orders/100/summary | jq
```

## Implementation Details

### Problem A - Split Monolith
- Two independent Spring Boot applications
- Each with its own in-memory data store
- No shared code modules

### Problem B - Synchronous Service Calls
- Orders service uses WebClient to call customers service
- Error handling for service unavailability (502 Bad Gateway)

### Problem C - Bounded Contexts
- Each service owns its own DTOs
- No shared domain models
- `RemoteCustomerDto` in orders-service for deserializing customer data

### Problem D - Fault Isolation
- WebClient configured with 1-second timeout
- Slow endpoint in customers-service (3s delay)
- Timeout handling returns 504 Gateway Timeout

### Problem E - API Gateway Stub
- Orders service acts as gateway facade
- Flattened response structure for clients
- Hides internal service structure

## Notes
- Each service can be built, run, and deployed independently
- Services use in-memory data stores (no shared database)
- WebClient is configured with timeouts for fault tolerance
- DTOs are service-specific to maintain bounded contexts

## Resources
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [WebClient Documentation](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
