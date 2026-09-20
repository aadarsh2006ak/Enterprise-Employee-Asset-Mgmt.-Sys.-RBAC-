# Enterprise Asset Management System (EAMS) — Observability & Monitoring Guide

This guide details the real-time observability stack (Prometheus + Grafana + Spring Boot Actuator Micrometer) built for EAMS.

---

## 📊 1. Observability Architecture

```
   [ Spring Boot Backend ] ──(Exposes /actuator/prometheus)──> [ Prometheus ] ──> [ Grafana Dashboards ]
      • JVM Heap & GC Pause                                       (Scrapes every 5s)      (Port 3001)
      • HikariCP Connection Pool
      • HTTP Request Latency (p95, p99)
      • Error Rates (5xx, 4xx)
```

---

## 🚀 2. Accessing Monitoring Locally

When running the system with `docker compose up -d`:

| Service | URL | Credentials | Description |
|---|---|---|---|
| **Grafana Dashboard** | `http://localhost:3001` | `admin` / `admin` | Real-time visual metrics dashboard |
| **Prometheus Web UI** | `http://localhost:9090` | None | PromQL query runner & scraper target status |
| **Backend Raw Metrics** | `http://localhost:8080/actuator/prometheus` | None | OpenMetrics/Prometheus text stream |

---

## 📈 3. Key Dashboard Metrics Tracked

The pre-provisioned Grafana dashboard (`EAMS - Enterprise Asset Management System Overview`) tracks:

1. **System Health & JVM Status**:
   - **JVM Heap Memory Usage**: Used vs Committed vs Max memory (`jvm_memory_used_bytes{area="heap"}`).
   - **Garbage Collection Pauses**: G1GC pause time duration and frequency (`jvm_gc_pause_seconds_sum`).
   - **Thread Count**: Live threads, daemon threads, and peak active threads (`jvm_threads_live_threads`).
   - **CPU Utilization**: Process CPU vs System total CPU usage (`process_cpu_usage`, `system_cpu_usage`).

2. **Database & Connection Pool (HikariCP)**:
   - **Active vs Idle Connections**: Monitored to detect connection pool starvation (`hikaricp_connections_active`, `hikaricp_connections_idle`).
   - **Connection Acquire Timeout**: Alerts if acquiring a connection exceeds threshold (`hikaricp_connections_timeout_total`).

3. **HTTP API Performance & SLA**:
   - **Throughput (Requests / Second)**: `rate(http_server_requests_seconds_count[1m])`.
   - **Latency (p95, p99, Average)**: Endpoint response time distribution (`http_server_requests_seconds_max`).
   - **Error Rate & Status Codes**: Tracking 2xx (Success), 4xx (Client error), and 5xx (Server error).

---

## 🔔 4. Recommended Alerting Thresholds

| Metric | Warning Threshold | Critical Threshold | Recommended Action |
|---|---|---|---|
| **JVM Heap Usage** | > 80% for 5m | > 90% for 2m | Scale ECS Task CPU/Memory or analyze memory dump |
| **HikariCP Active Connections** | > 80% pool cap | > 95% pool cap | Optimize N+1 queries, increase pool size in `application.yml` |
| **HTTP 5xx Error Rate** | > 1% of traffic | > 5% of traffic | Inspect CloudWatch/Docker logs for unhandled exceptions |
| **API Latency (p95)** | > 800ms | > 2000ms | Check DB indexes, slow query logs, or Redis hit ratio |
