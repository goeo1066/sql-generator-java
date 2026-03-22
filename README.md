# sql-generator-java

**Abstraction without magic on top of Spring Data JDBC. Minimize boilerplate, maximize SQL visibility, and treat your database as a first-class citizen — not just a storage engine.**

## Philosophy

**Abstraction is the right direction. Magic is not.**

Higher abstraction that reduces complexity is a good thing. But when the abstraction layer silently generates proxies, triggers lazy loading behind your back, or produces SQL you didn't ask for — that's no longer abstraction. That's magic. And magic breaks at 3 AM in production.

`sql-generator-java` draws a clear line:

- **Abstract the plumbing, not the database.** Boilerplate like column mapping and parameter binding should disappear. But what SQL runs against your database should never be a surprise.
- **The database is not just a storage engine.** `INSERT ... ON CONFLICT DO NOTHING`, functions, procedures — these exist for a reason. Your Java layer should embrace them, not hide them behind an opaque middleware.
- **Embrace each DBMS for what it is.** PostgreSQL is not Oracle. Oracle is not MySQL. Instead of flattening every database into a lowest common denominator, this project exposes each DBMS's unique strengths — COPY API on PostgreSQL, MERGE on Oracle, and so on. This means switching databases may require code changes. That's not a bug. That's the point — it means you were actually using your database, not a watered-down abstraction of it.
- **Get out of the way when needed.** Sometimes you just need to write raw SQL. The framework should make the common case effortless, but never stand between you and your database when you need direct control.
- **Explicit over implicit.** Every query is readable. Every operation is predictable. If you can't explain what happens at the SQL level, the abstraction has gone too far.

## Features

- **Typed JDBC templates** — `PostgreSqlJdbcTemplate<UserEntity>` provides type-safe operations without proxy generation
- **Annotation-driven entity mapping** — `@Table`, `@Pk` for declarative mapping, no runtime bytecode manipulation
- **DB-native write operations** — `insertOnConflictDoNothing`, upsert support that maps directly to real SQL
- **DBMS-specific by design** — each dialect exposes its database's unique capabilities instead of hiding them behind a common interface
- **SQL stays in your hands** — `SelectSpec` with raw SQL WHERE + named parameter binding. You write the logic, the framework handles the wiring
- **Escape hatch by design** — when the abstraction isn't enough, drop down to raw SQL without fighting the framework
- **Aggregate operations** — `countTotal`, existence checks, etc.
- **No magic** — no proxy generation, no lazy loading, no implicit queries. What you write is what runs

## Installation

Add the JitPack repository and dependency to your project:

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.goeo1066:sql-generator-java:main-SNAPSHOT")
}
```

**Gradle (Groovy)**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.goeo1066:sql-generator-java:main-SNAPSHOT'
}
```

**Maven**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.goeo1066</groupId>
    <artifactId>sql-generator-java</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

> ⚠️ This project is in early development. Use `main-SNAPSHOT` to track the latest changes, or pin to a specific commit hash for stability.

## Quick Start

### 1. Define an Entity

```java
@Data
@Table(schema = "AUTH", name = "USER")
public class UserEntity extends DeletableEntityBase {
    @Pk
    private UUID code;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private String role;
    private int failedLoginCount;
    private OffsetDateTime lockedUntil;
    private OffsetDateTime lastLoginAt;
}
```

### 2. Write a DAO

```java
@RequiredArgsConstructor
@Component
public class UserDao {
    private final PostgreSqlJdbcTemplate<UserEntity> userJdbc;

    public long countByEmail(Email email) {
        return userJdbc.countTotal(WhereBuilders.selectByEmail(email));
    }

    @Transactional
    public void createUser(UserEntity user) {
        userJdbc.insertOnConflictDoNothing(List.of(user));
    }

    private static class WhereBuilders {
        public static SelectSpec selectByEmail(Email email) {
            SelectSpec spec = SelectSpec.ofDefault();
            spec.setWhere("AND T.EMAIL = :email");
            spec.setSqlParameterSource(
                new MapSqlParameterSource("email", email.asValue()));
            return spec;
        }
    }
}
```

That's it. No repository interfaces to extend, no query derivation to debug, no JPQL to learn. Abstraction that removes noise, not visibility.

## Comparison

| | JPA/Hibernate | Spring Data JDBC | sql-generator-java |
|---|---|---|---|
| Boilerplate | Low (implicit) | Medium | Low (explicit) |
| SQL visibility | Hidden | Partial | Full |
| DB-native ops | Workaround | Manual | Built-in |
| DBMS-specific features | Abstracted away | Not addressed | Exposed by design |
| Proxy / lazy loading | Yes (magic) | No | No |
| Predictability | Low | High | High |
| Raw SQL when needed | Fighting the framework | Native | Native |
| Learning curve | High | Low | Low |

## Tech Stack

- Java 17+
- Spring Data JDBC
- PostgreSQL (primary, first-class support) — Oracle and MySQL planned
- Gradle

## Roadmap

This project is in early development. Some features work, many are planned. Here's where things stand:

### Working
- [x] Annotation-driven entity mapping (`@Table`, `@Pk`)
- [x] Typed JDBC templates with PostgreSQL
- [x] `insertOnConflictDoNothing`
- [x] `SelectSpec` with raw SQL WHERE + named parameters
- [x] Aggregate operations (`countTotal`)

### In Progress
- [ ] **Read-only query support** — lightweight result mapping for SELECT-only use cases where full entity binding is overkill
- [ ] Test coverage expansion

### Planned
- [ ] **Spring Boot Starter** — auto-configuration support so you can get started with just a dependency and `application.yml`. Convention over configuration for the plumbing, full control for the queries
- [ ] **PostgreSQL COPY API support** — bulk data loading using PostgreSQL's native COPY protocol
- [ ] **Stored procedure / function call helpers** — first-class support for calling DB-side logic from Java
- [ ] **Oracle dialect** — with Oracle-specific features (MERGE, hints, etc.)
- [ ] **MySQL dialect** — with MySQL-specific features (`ON DUPLICATE KEY UPDATE`, etc.)
- [ ] Upsert (`INSERT ... ON CONFLICT DO UPDATE`) support
- [ ] Batch operations
- [ ] Maven Central publication (currently available via JitPack)

> The roadmap reflects the project's core belief: build what matters first, and don't abstract what isn't proven yet.

## Project Status

🚧 **Early-stage / Work in progress**

The core idea is validated and partial functionality is working, but this is not production-ready yet. The API surface may change as the project evolves.

Feedback, ideas, and contributions are welcome — especially if you share the frustration with middleware-heavy ORMs.

## License

[Apache License 2.0](LICENSE)
