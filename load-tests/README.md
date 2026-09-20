# EAMS High-Concurrency Load Testing Suite (500–1000 Users)

This directory contains the automated performance benchmarking and load testing infrastructure for the **Enterprise Asset Management System (EAMS)**.

---

## 🚀 Key Scenarios Tested

The JMeter test plan (`eams-load-test-500-1000-users.jmx`) simulates **500 to 1000 concurrent enterprise users**:

1. **Authentication & Token Extraction**: `POST /api/v1/auth/login` (JSON Extractor dynamically extracts `${accessToken}`).
2. **Current User Profile Inspection**: `GET /api/v1/auth/me` with Bearer auth.
3. **Paginated Asset Catalog**: `GET /api/v1/assets?page=0&size=20` (validates the N+1 batch query fix).
4. **Employee Directory Lookup**: `GET /api/v1/employees?page=0&size=20`.
5. **Organizational Hierarchy & Department Cache**: `GET /api/v1/departments/list`.
6. **Streaming CSV Asset Export**: `GET /api/v1/exports/direct/assets?format=CSV` under high concurrent load.

---

## 🛠️ How To Run the Load Test

### Option 1: Automated Script (Recommended)

1. Start the backend with the load-testing profile:
   ```bash
   cd eams-backend
   mvn spring-boot:run -Dspring-boot.run.profiles=loadtest
   ```
2. Execute the PowerShell or Batch runner script:
   ```powershell
   # 500 users, 60s ramp-up, 300s duration (5 minutes)
   .\run-load-test.ps1 -Threads 500 -RampUp 60 -Duration 300

   # Or 1000 users test:
   .\run-load-test.ps1 -Threads 1000 -RampUp 60 -Duration 300
   ```
3. The script will automatically open the interactive **HTML Performance Dashboard** in your browser when finished.

### Option 2: JMeter CLI (Direct)

```bash
jmeter -n -t eams-load-test-500-1000-users.jmx \
       -l results/results.jtl \
       -e -o results/html-report \
       -Jthreads=1000 \
       -Jrampup=60 \
       -Jduration=300
```

### Option 3: JMeter GUI Mode (For Debugging / Visual inspection)

```bash
jmeter -t eams-load-test-500-1000-users.jmx
```

---

## 📊 Performance SLA & Key Metrics

- **Throughput (RPS)**: Measured across all endpoints.
- **Latency SLAs**:
  - `P90 Response Time`: < 500ms
  - `P95 Response Time`: < 1000ms
  - `P99 Response Time`: < 2000ms
- **Error Rate Target**: < 0.1%

---

## 🔧 Database & Concurrency Bottleneck Fixes Implemented

1. **Batch Fetching for Active Assignments**:
   - Replaced individual queries in `AssetServiceImpl.getAllAssets` with `assignmentRepository.findActiveAssignmentsByAssetIdsIn(assetIds)`.
   - Reduced database roundtrips per page from **51 SQL queries** to **2 SQL queries** (96% reduction).
2. **EntityGraph Join Fetching for Streaming Export**:
   - `AssetAssignmentRepository.findAllWithDetails()` prevents N+1 queries during bulk CSV/Excel exports.
3. **HikariCP & Tomcat Thread Pool Tuning**:
   - `application-loadtest.yml` configures HikariCP max pool size to 60 and Tomcat max worker threads to 400 with 10,000 connection capacity.
