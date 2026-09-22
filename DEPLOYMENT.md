# Enterprise Asset Management System (EAMS) — Production Deployment Guide

This document provides complete instructions for deploying the EAMS application to **AWS ECS Fargate (Primary Target)**, **Local/Dedicated Docker Compose**, and **Google Cloud Run**.

---

## 🏗️ 1. Architecture Overview (AWS ECS Fargate)

```
                       [ Internet Users / Clients ]
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
       • 0.5 vCPU, 1 GB RAM            • 1.0 vCPU, 2 GB RAM
       • Fargate Serverless            • Fargate Serverless
                                                │
                 ┌──────────────────────────────┴──────────────────────────────┐
                 ▼                                                             ▼
    [ Amazon RDS PostgreSQL 15 ]                              [ Amazon ElastiCache Redis 7 ]
    • Multi-AZ DB Cluster                                     • Cluster Mode / Standalone
    • Automated Backups & KMS Encryption                      • Distributed Token-Bucket Rate Limiter
    • Connection Pooling via HikariCP                         • Redis Cache Layer
```

---

## 🚀 2. Quick Local Deployment (Docker Compose)

To start the full enterprise stack locally (PostgreSQL, Redis, Backend, Frontend, Prometheus, Grafana) in one command:

```powershell
# 1. Copy environment variables template
cp .env.example .env

# 2. Build and run all 6 microservices in background
docker compose up --build -d

# 3. Verify health status of all containers
docker compose ps
```

### Local Endpoint Map:
| Service | Endpoint | Credentials / Details |
|---|---|---|
| **Frontend Web App** | `http://localhost` | Admin: `admin@company.com` / `Admin@123` |
| **Spring Boot Backend** | `http://localhost:8080/api/v1` | REST APIs & OpenAPI Swagger docs |
| **Actuator Health** | `http://localhost:8080/actuator/health` | Upstream Readiness/Liveness probe |
| **Prometheus** | `http://localhost:9090` | Time-series scraper for JVM & HTTP metrics |
| **Grafana** | `http://localhost:3001` | User: `admin` / Password: `admin` |
| **PostgreSQL** | `localhost:5432` | DB: `eams`, User: `postgres`, Pass: `postgres` |
| **Redis** | `localhost:6379` | Token bucket rate limiting store |

To shut down:
```powershell
docker compose down -v
```

---

## ☁️ 3. AWS EC2 Deployment (Fast, Easy & Free Tier Friendly)

For single VM deployment on AWS EC2 (Ubuntu 24.04 LTS) with automated swap, Docker, and full stack orchestration:

```bash
# 1. SSH into your EC2 Instance
ssh -i "eams-key.pem" ubuntu@<YOUR_EC2_PUBLIC_IP>

# 2. Clone repo and run automated setup script
git clone https://github.com/aadarsh2006ak/Enterprise-Employee-Asset-Mgmt.-Sys.-RBAC-.git eams-project
cd eams-project
chmod +x ./deploy/aws/ec2/setup-ec2.sh
./deploy/aws/ec2/setup-ec2.sh
```

> 📖 **Full Walkthrough**: Detailed console instructions, security group configurations, and domain/SSL setup can be found in [EC2_DEPLOYMENT_GUIDE.md](file:///deploy/aws/ec2/EC2_DEPLOYMENT_GUIDE.md).

---

## ☁️ 4. AWS ECS Fargate Production Deployment

### Prerequisites:
1. **AWS CLI** installed and configured (`aws configure`).
2. **Amazon ECR** repositories (`eams-backend`, `eams-frontend`).
3. **Amazon RDS PostgreSQL** instance created in private VPC subnets.
4. **Amazon ElastiCache Redis** cluster created in private VPC subnets.
5. **AWS Secrets Manager** entries created for secrets:
   - `eams/prod/db-password`
   - `eams/prod/jwt-secret`
6. **Application Load Balancer (ALB)** with target groups for backend (port 8080, health check `/actuator/health`) and frontend (port 80, health check `/`).

### Step-by-Step Deployment:

#### Option A: Automated PowerShell Deployment Script
```powershell
.\deploy\aws\deploy-aws.ps1 -AwsAccountId "<YOUR_AWS_ACCOUNT_ID>" -AwsRegion "ap-south-1" -ClusterName "eams-production-cluster"
```

#### Option B: Automated Linux / CI Bash Script
```bash
chmod +x ./deploy/aws/deploy-aws.sh
./deploy/aws/deploy-aws.sh <YOUR_AWS_ACCOUNT_ID> ap-south-1 eams-production-cluster latest
```

#### Option C: Automated CI/CD via GitHub Actions
1. Navigate to your GitHub repository **Settings** -> **Secrets and variables** -> **Actions**.
2. Add the following repository secrets:
   - `AWS_ACCESS_KEY_ID`: IAM user access key with ECR and ECS deployment permissions.
   - `AWS_SECRET_ACCESS_KEY`: IAM user secret access key.
3. Every push to the `main` branch will automatically trigger `.github/workflows/deploy-aws.yml`, building containers, pushing to Amazon ECR, and initiating a zero-downtime rolling deployment on ECS Fargate.

---

## 🛡️ 4. Production Hardening Checklist

- [x] **Unprivileged Containers**: Backend runs under non-root system user `eamsuser:eamsgroup`.
- [x] **JVM Container Tuning**: `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0` configured for optimal memory usage and GC pauses.
- [x] **Secrets Management**: Sensitive credentials (DB passwords, JWT secret) mapped via AWS Secrets Manager ARN, never baked into Docker images.
- [x] **Graceful Rolling Updates**: AWS ECS Fargate tasks utilize Actuator health checks (`/actuator/health`) before routing production traffic.
- [x] **Multi-Stage Builds**: Minimalist scratch/alpine base images minimize CVE vulnerability surface area.
