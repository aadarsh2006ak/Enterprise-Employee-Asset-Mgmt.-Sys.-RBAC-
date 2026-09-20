# Enterprise Employee & Asset Management System (EAMS)

[![CI Pipeline](https://github.com/company/eams/actions/workflows/ci.yml/badge.svg)](.github/workflows/ci.yml)
[![AWS ECS Fargate Deploy](https://github.com/company/eams/actions/workflows/deploy-aws.yml/badge.svg)](.github/workflows/deploy-aws.yml)
[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Container-2496ED.svg)](https://www.docker.com/)
[![AWS Fargate](https://img.shields.io/badge/AWS-ECS%20Fargate-FF9900.svg)](https://aws.amazon.com/fargate/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

A production-grade, crash-resilient Enterprise Employee and Asset Management System built with **Spring Boot 3 (Java 17)**, **React 18 + Vite + Tailwind**, **PostgreSQL 15**, **Redis 7**, **Prometheus & Grafana Observability**, and **Flyway Migrations**. The system features strict Role-Based Access Control (RBAC), JWT Authentication with Token Rotation, Optimistic/Pessimistic Concurrency Controls, Distributed Token-Bucket Rate Limiting, and Immutable Audit Logging with JSONB State Diffs.

---

## 🏛 1. Production Architecture (AWS ECS Fargate)

```
                       [ Internet Users / Web Clients ]
                                      │
                                      ▼
                        [ AWS Route 53 (DNS) ]
                                      │
                                      ▼
                   [ AWS Application Load Balancer (ALB) ]
                       / (SSL Termination & WAF) \
                      /                           \
           Path: /*  /                             \  Path: /api/*, /actuator/*
                    ▼                               ▼
         [ ECS Service: Frontend ]       [ ECS Service: Backend ]
         • React 18 + Vite               • Spring Boot 3.3 (Java 17)
         • Nginx Reverse Proxy           • Port 8080 (G1GC Container Tuned)
         • 0.5 vCPU, 1 GB RAM            • 1.0 vCPU, 2 GB RAM (2–10 Auto-Scaled Tasks)
         • Fargate Serverless            • Fargate Serverless
                                                  │
                   ┌──────────────────────────────┴──────────────────────────────┐
                   ▼                                                             ▼
      [ Amazon RDS PostgreSQL 15 ]                              [ Amazon ElastiCache Redis 7 ]
      • Multi-AZ with Automated Backups                         • Distributed Token-Bucket Rate Limiter
      • KMS Encrypted Storage                                   • Session Cache & Query Cache
      • HikariCP Connection Pool                                • Token Revocation Blacklist
```

---

## 🚀 2. Quick Start: 1-Command Local Stack

Start all 6 microservices (Postgres, Redis, Spring Boot Backend, Nginx/React Frontend, Prometheus, Grafana) locally:

```powershell
# 1. Initialize environment variables
cp .env.example .env

# 2. Build and launch all 6 containers in detached mode
docker compose up --build -d

# 3. Verify health status
docker compose ps
```

### 🌐 Microservice Service Map:
| Service | Endpoint | Default Credentials / Details |
|---|---|---|
| **Frontend Web App** | [http://localhost](http://localhost) | Admin: `admin` / `Admin@123` |
| **Backend API Gateway** | [http://localhost:8080/api/v1](http://localhost:8080/api/v1) | REST APIs |
| **Swagger UI Documentation** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interactive OpenAPI 3.0 Explorer |
| **Spring Actuator Health** | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) | Readiness & Liveness probes |
| **Grafana Dashboards** | [http://localhost:3001](http://localhost:3001) | `admin` / `admin` (Pre-configured JVM Dashboard) |
| **Prometheus Metrics** | [http://localhost:9090](http://localhost:9090) | Scrapes `/actuator/prometheus` every 5s |
| **PostgreSQL 15** | `localhost:5432` | DB: `eams`, User: `postgres`, Pass: `postgres` |
| **Redis 7** | `localhost:6379` | Distributed Caches & Token Bucket Bucket4j Store |

---

## 🧪 3. Automated Testing & Quality Assurance

The codebase includes an enterprise-grade, comprehensive test suite covering Unit, Security, and Real-Database Integration tests.

```powershell
cd eams-backend
mvn clean test
```

### Test Coverage Highlights:
- **Unit Testing (JUnit 5 & Mockito)**: Service layer verification (`AssetServiceImplTest`, `UserServiceImplTest`, `AuditLogServiceImplTest`, `ExportServiceImplTest`) and Web layer testing (`AuthControllerTest`, `EmployeeControllerTest`, `AssetControllerTest`, `DepartmentControllerTest`, `AdminUserControllerTest`, `AuditLogControllerTest`, `ExportControllerTest`).
- **Distributed Security Filters**: `RateLimitingFilterTest` verifying token-bucket burst resistance and IP rate limiting.
- **Integration Testing with Testcontainers**: Real PostgreSQL and Redis ephemeral containers (`AuthIntegrationTest`, `RbacSecurityIntegrationTest`, `AssetConcurrencyIntegrationTest`).
- **Total Test Count**: **75 / 75 Tests Passed (0 Failures, 0 Errors)**.

---

## ⚡ 4. Apache JMeter 500–1000 Concurrent User Load Tests

A dedicated, parameterized JMeter test plan is located under `load-tests/` to validate high concurrency, cache efficiency, and latency SLAs under extreme load.

```powershell
# Run load test and generate visual HTML dashboard
cd load-tests
.\run-load-test.ps1 -Threads 500 -RampUp 30 -Duration 180
```

- **Thread Group 1 (500 Users)**: High-frequency paginated searches, filters, and asset detail retrieval.
- **Thread Group 2 (300 Users)**: Concurrent asset assignments, approvals, and state machine transitions.
- **Thread Group 3 (200 Users)**: Large-scale audit trail analytics and CSV/Excel streaming exports.
- **SLA Target**: Throughput > 200 req/sec, p95 Latency < 800ms, Error Rate < 0.1%.

---

## ☁️ 5. Cloud Deployment (AWS ECS Fargate & CI/CD)

### AWS ECS Fargate Blueprints:
- [deploy/aws/ecs-task-backend.json](deploy/aws/ecs-task-backend.json): Serverless container definition with JVM container tuning (`-XX:+UseG1GC`, `-XX:MaxRAMPercentage=75.0`), AWS Secrets Manager integration, CloudWatch logging, and Actuator healthcheck.
- [deploy/aws/ecs-task-frontend.json](deploy/aws/ecs-task-frontend.json): Production Nginx reverse proxy container definition.
- [deploy/aws/deploy-aws.ps1](deploy/aws/deploy-aws.ps1): Automated Windows PowerShell deployment pipeline to Amazon ECR and ECS Fargate.
- [deploy/aws/deploy-aws.sh](deploy/aws/deploy-aws.sh): Automated Bash deployment script for Linux and CI runners.

### GitHub Actions CI/CD Workflows:
1. [`.github/workflows/ci.yml`](.github/workflows/ci.yml): Runs Maven test verification, ESLint, TypeScript build, and Docker multi-stage sanity checks on every pull request.
2. [`.github/workflows/deploy-aws.yml`](.github/workflows/deploy-aws.yml): Automated Continuous Delivery to Amazon ECR and zero-downtime rolling deployment to AWS ECS Fargate.
3. [`.github/workflows/deploy-gcp.yml`](.github/workflows/deploy-gcp.yml): Secondary automated deployment workflow for Google Cloud Run.

---

## 📚 6. Documentation Index

- 📘 [DEPLOYMENT.md](DEPLOYMENT.md) — Complete Production Deployment Architecture, AWS ECS Setup & Troubleshooting Guide.
- 📊 [MONITORING.md](MONITORING.md) — Prometheus Scraping, Micrometer Metrics, Grafana Dashboards & Alert Thresholds.
- 🧪 [load-tests/README.md](load-tests/README.md) — JMeter Load Testing Execution, CLI Parameters & Optimization Analysis.
