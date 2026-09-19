# Enterprise Employee & Asset Management System (RBAC)

A production-grade, crash-resilient Enterprise Employee and Asset Management System built with **Spring Boot 3**, **PostgreSQL 15**, **Redis 7**, and **Flyway Migrations**, implementing Role-Based Access Control (RBAC), JWT Authentication, Optimistic/Pessimistic Concurrency Controls, and Immutable Audit Logging.

---

## 🏛 High-Level Architecture (Week 1 Foundation)

```
                     ┌────────────────────────────────┐
                     │    Client Applications / UI    │
                     └───────────────┬────────────────┘
                                     │ HTTPS
                                     ▼
                     ┌────────────────────────────────┐
                     │    Spring Boot 3.3 Backend     │
                     │  - HikariCP Connection Pool    │
                     │  - Optimistic (@Version) Lock  │
                     │  - Flyway Schema Migrations    │
                     │  - Bounded Async Executors     │
                     └───────┬────────────────┬───────┘
                             │                │
                             ▼                ▼
            ┌──────────────────┐    ┌──────────────────┐
            │  PostgreSQL 15   │    │     Redis 7      │
            │  (3NF + Indexes) │    │ (TTL Caches/Ops) │
            └──────────────────┘    └──────────────────┘
```

---

## 📁 Project Structure

```
├── docker-compose.yml              # Local multi-container dev environment (App + DB + Redis)
├── .env.example                    # Environment variable configuration template
├── eams-backend/
│   ├── Dockerfile                  # Multi-stage container build (Temurin JDK 17 -> JRE 17 Alpine)
│   ├── pom.xml                     # Maven dependencies & build configuration
│   ├── src/main/java/com/company/eams/
│   │   ├── EamsApplication.java    # Application entry point (@EnableJpaAuditing, @EnableAsync)
│   │   ├── config/
│   │   │   ├── AsyncConfig.java    # Bounded thread pools with CallerRunsPolicy (Section 9.11)
│   │   │   ├── RedisConfig.java    # Redis CacheManager with TTL policies (Section 10.3)
│   │   │   └── OpenApiConfig.java  # Swagger OpenAPI 3.0 configuration
│   │   ├── entity/                 # JPA Domain Entities
│   │   │   ├── BaseEntity.java     # Auditing timestamps + @Version optimistic locking
│   │   │   ├── Role.java           # RBAC Roles (ADMIN, MANAGER, EMPLOYEE)
│   │   │   ├── Permission.java     # Granular action permissions
│   │   │   ├── User.java           # System users
│   │   │   ├── RefreshToken.java   # Hashed refresh tokens for rotation
│   │   │   ├── Department.java     # Department units & department heads
│   │   │   ├── Employee.java       # Employee profiles with self-referencing hierarchy
│   │   │   ├── AssetCategory.java  # Asset categories (Laptop, Monitor, Mobile, etc.)
│   │   │   ├── Asset.java          # Hardware / Software asset inventory
│   │   │   ├── AssetAssignment.java# Asset assignment tracking with partial unique index
│   │   │   ├── AssetRequest.java   # Employee self-service asset requests
│   │   │   ├── AuditLog.java       # Immutable audit logs with native JSONB state diffs
│   │   │   └── enums/              # Status & Action enums
│   └── src/main/resources/
│       ├── application.yml         # Tuned HikariCP, Tomcat threads, Actuator & Redis
│       ├── application-dev.yml     # Development profile
│       ├── application-prod.yml    # Production profile
│       └── db/migration/
│           ├── V1__init_schema.sql # Complete 3NF database schema & partial indexes
│           └── V2__seed_initial_data.sql # Initial roles, permissions, categories & Admin user
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Docker & Docker Compose** (Recommended for zero-dependency setup)
- OR **Java 17+** & **PostgreSQL 15+** & **Redis 7+** installed locally

### 2. Environment Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

### 3. Running with Docker Compose (Local Dev)
To start PostgreSQL, Redis, and the Spring Boot Backend simultaneously:
```bash
docker compose up --build -d
```

### 4. Database Migrations & Initial Credentials
Flyway will automatically execute `V1__init_schema.sql` and `V2__seed_initial_data.sql` on startup:
- **Default Super Admin**: `admin`
- **Default Password**: `Admin@123`
- **Default Roles**: `ADMIN`, `MANAGER`, `EMPLOYEE`

### 5. API Documentation & Health Check
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Actuator Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
