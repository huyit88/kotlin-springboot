# CHALLENGE.md — Topic 27: GraphQL

### Dependencies

Add only what’s needed for GraphQL:

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-graphql")
}
```

---

### Problem A — Schema-First Query (user & users)

#### Requirement

Create a schema and resolvers for reading users from an **in-memory** store.

* `src/main/resources/graphql/schema.graphqls`

  ```graphql
  type User { id: ID!, fullName: String!, email: String }
  type Query {
    user(id: ID!): User
    users: [User!]!
  }
  ```
* In-memory repo seeded with at least 2 users.
* Resolver methods: `Query.user(id)` and `Query.users()`.

#### Acceptance criteria

* Query `user(id:"1"){ id fullName }` → returns one user or `null` when missing.
* Query `users{ id fullName }` → returns an array with ≥2 entries.
* No DB or external calls.

#### Suggested Import Path

```kotlin
import org.springframework.stereotype.*
import org.springframework.graphql.data.method.annotation.*
```

#### Command to verify/run

```bash
./gradlew :26-graphql:bootRun

# Single user
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user(id:\"1\"){ id fullName email } }"}' | jq

# All users
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users { id fullName } }"}' | jq
```

---

### Problem B — Mutation (renameUser)

#### Requirement

Add a mutation to update a user’s name in memory.

* Extend schema:

  ```graphql
  type Mutation {
    renameUser(id: ID!, name: String!): User!
  }
  ```
* Implement resolver `Mutation.renameUser(id, name)` that:

  * Looks up user; if not found, throw `IllegalArgumentException("User not found")`.
  * Updates `fullName` and returns the updated user.

#### Acceptance criteria

* Mutation call returns updated fields:

  ```graphql
  mutation { renameUser(id:"1", name:"Ada L.") { id fullName } }
  ```
* A missing user returns a GraphQL error (HTTP 200 with `"errors"` array).
* Subsequent `Query.user` reflects the new name.

#### Suggested Import Path

```kotlin
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.Argument
```

#### Command to verify/run

```bash
./gradlew :26-graphql:bootRun
# Update
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { renameUser(id:\"1\", name:\"Ada L.\"){ id fullName } }"}' | jq

# Read back
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user(id:\"1\"){ id fullName } }"}' | jq
```

---

### Problem C — Field Resolver (nested orders on User)

#### Requirement

Add a nested field `orders` to `User` and resolve it per user.

* Extend schema:

  ```graphql
  type Order { id: ID!, total: Float! }
  extend type User { orders: [Order!]! }
  ```
* Implement `@SchemaMapping(typeName = "User", field = "orders") fun orders(user: User): List<Order>`.
* Use an in-memory `OrderRepository` with a few orders mapped to user IDs.

#### Acceptance criteria

* Query returns nested data:

  ```graphql
  { user(id:"1"){ id fullName orders { id total } } }
  ```
* Users without orders return an empty array (not null).
* No DB; all in-memory.

#### Suggested Import Path

```kotlin
import org.springframework.graphql.data.method.annotation.SchemaMapping
```

#### Command to verify/run

```bash
./gradlew :26-graphql:bootRun
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user(id:\"1\"){ id fullName orders { id total } } }"}' | jq
```

---

### Problem D — Batch Loading with DataLoader (avoid N+1)

#### Requirement

Replace per-user fetching with **batch loading** to prevent N+1 lookups.

* Register a `DataLoader` bean that batches by userId:

  * Type: `MappedBatchLoader<String, List<Order>>`.
* Use the loader in the `orders(user: User)` resolver to load by key instead of querying one-by-one.
* Add simple logging in the loader to show **one batch call** per GraphQL request.

#### Acceptance criteria

* Query:

  ```graphql
  { users { id fullName orders { id total } } }
  ```

  shows **all users with orders**.
* Logs indicate **a single batch** retrieval (e.g., “order batch load: [1,2,3]”), not one per user.
* Behavior matches Problem C results.

#### Suggested Import Path

```kotlin
import org.dataloader.DataLoader
import org.dataloader.MappedBatchLoader
import org.springframework.context.annotation.Bean
import java.util.concurrent.CompletableFuture
```

#### Command to verify/run

```bash
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users { id fullName orders { id total } } }"}' | jq
# Check logs for a single "batch load" line
```

---

### Problem E — Enable GraphiQL Playground (dev convenience)

#### Requirement

Enable the embedded GraphiQL UI for manual exploration.

* Add to `application.yml`:

  ```yaml
  spring:
    graphql:
      graphiql:
        enabled: true
  ```
* Open the UI at `/graphiql` and run the queries from Problems A–D.

#### Acceptance criteria

* Visiting `http://localhost:8080/graphiql` shows the editor.
* Executing queries/mutations works and matches previous results.

#### Suggested Import Path

*(No code imports; configuration only.)*

#### Command to verify/run

```bash
./gradlew :26-graphql:bootRun
# Open in browser:
# http://localhost:8080/graphiql
```

---

## Hints

* Resolvers must match schema operation names exactly.
* Keep everything **in-memory** to honor the topic scope.
* For DataLoader, ensure you return a `CompletableFuture` and map **every requested key**.
* GraphQL returns HTTP 200 even on resolver errors; check the `"errors"` array for failures.

✅ **End of CHALLENGE.md**
