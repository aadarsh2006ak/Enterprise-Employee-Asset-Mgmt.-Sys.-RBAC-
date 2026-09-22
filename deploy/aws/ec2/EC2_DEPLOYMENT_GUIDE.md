# 🚀 AWS EC2 Deployment Guide — EAMS (Enterprise Asset Management System)

Complete step-by-step guide to deploy the full **EAMS Microservices Stack** (PostgreSQL, Redis, Spring Boot Backend, React Vite Frontend, Prometheus, and Grafana) on a single **AWS EC2 Instance** using Docker Compose.

---

## 📋 Table of Contents
1. [Prerequisites & Recommended EC2 Specs](#-1-prerequisites--recommended-ec2-specs)
2. [Step 1: Launch an AWS EC2 Instance](#-step-1-launch-an-aws-ec2-instance)
3. [Step 2: Configure Security Group (Firewall Rules)](#-step-2-configure-security-group-firewall-rules)
4. [Step 3: Connect to EC2 Instance](#-step-3-connect-to-ec2-instance)
5. [Step 4: Clone & Run Automated Setup Script](#-step-4-clone--run-automated-setup-script)
6. [Step 5: Verify Deployment & Access URLs](#-step-5-verify-deployment--access-urls)
7. [Step 6 (Optional): Domain & Free SSL Setup (HTTPS)](#-step-6-optional-domain--free-ssl-setup-https)
8. [Troubleshooting & Maintenance Commands](#-troubleshooting--maintenance-commands)

---

## ⚙️ 1. Prerequisites & Recommended EC2 Specs

| Parameter | Recommended Specification | Free Tier Friendly? |
|---|---|---|
| **Operating System (AMI)** | Ubuntu Server 24.04 LTS (64-bit x86) | ✅ Yes |
| **Instance Type** | `t3.small` (2 vCPU, 2GB RAM) or `t2.medium` | `t2.micro` works too (Script automatically creates 4GB Swap memory to prevent build OOM) |
| **Storage (EBS Volume)** | 20 GB – 30 GB gp3 SSD | ✅ Yes (AWS gives 30GB free EBS storage) |
| **Network** | Default VPC, Auto-assign Public IP = **Enable** | ✅ Yes |

---

## 🛠️ Step 1: Launch an AWS EC2 Instance

1. Log in to your [AWS Management Console](https://console.aws.amazon.com/).
2. In the top search bar, type **EC2** and click on **EC2**.
3. Click the orange **"Launch instance"** button.
4. Fill in the following details:
   - **Name**: `eams-production-server`
   - **Application and OS Images (Amazon Machine Image)**: Select **Ubuntu** (Ubuntu Server 24.04 LTS 64-bit (x86)).
   - **Instance type**: Choose `t3.small` (recommended for smooth running) or `t2.micro` (Free Tier).
   - **Key pair (login)**:
     - Click **Create new key pair**.
     - Name: `eams-key`.
     - Key pair type: **RSA**.
     - Private key file format: `.pem` (for OpenSSH/Mac/Linux/Windows PowerShell).
     - Click **Create key pair** (a `.pem` file will be downloaded to your computer — keep it safe!).
   - **Network settings**:
     - Check **Allow SSH traffic from** -> `Anywhere (0.0.0.0/0)` or `My IP`.
     - Check **Allow HTTP traffic from the internet** (`0.0.0.0/0`).
     - Check **Allow HTTPS traffic from the internet** (`0.0.0.0/0`).
   - **Configure storage**:
     - Change `8 GiB` to **`20 GiB`** (gp3 SSD).
5. Click **"Launch instance"**.

---

## 🔒 Step 2: Configure Security Group (Firewall Rules)

To access Prometheus, Grafana, and Backend API directly if needed, add these inbound rules:

1. In EC2 Dashboard, go to **Network & Security** -> **Security Groups**.
2. Click on the Security Group attached to your new EC2 instance (e.g. `launch-wizard-1` or `eams-sg`).
3. Click **Edit inbound rules** and ensure the following rules are present:

| Type | Protocol | Port Range | Source | Description |
|---|---|---|---|---|
| **SSH** | TCP | `22` | `0.0.0.0/0` (or My IP) | SSH Terminal Access |
| **HTTP** | TCP | `80` | `0.0.0.0/0` | Frontend React & Nginx Proxy |
| **HTTPS** | TCP | `443` | `0.0.0.0/0` | Secure SSL Traffic |
| **Custom TCP** | TCP | `3001` | `0.0.0.0/0` | Grafana Live Dashboards |
| **Custom TCP** | TCP | `8080` | `0.0.0.0/0` | Spring Boot Direct API (Optional) |
| **Custom TCP** | TCP | `9090` | `0.0.0.0/0` | Prometheus Metrics (Optional) |

4. Click **Save rules**.

---

## 💻 Step 3: Connect to EC2 Instance

### Option A: Via Browser (Easiest - EC2 Instance Connect)
1. Go to EC2 Instances list, select `eams-production-server`.
2. Click **Connect** button at the top.
3. Choose **EC2 Instance Connect** tab and click **Connect**.
4. A terminal will open right inside your web browser!

### Option B: Via Windows PowerShell / Terminal (SSH)
1. Open PowerShell and navigate to the folder where your `eams-key.pem` was downloaded:
   ```powershell
   cd ~/Downloads
   ```
2. Set permissions (if needed on Linux/Mac: `chmod 400 eams-key.pem`).
3. Run the SSH command (replace `<YOUR_EC2_PUBLIC_IP>` with your instance's IPv4):
   ```bash
   ssh -i "eams-key.pem" ubuntu@<YOUR_EC2_PUBLIC_IP>
   ```

---

## ⚡ Step 4: Clone & Run Automated Setup Script

Once inside the EC2 Ubuntu terminal, run the following commands:

```bash
# 1. Clone your GitHub repository
git clone https://github.com/aadarsh2006ak/Enterprise-Employee-Asset-Mgmt.-Sys.-RBAC-.git eams-project

# 2. Enter project directory
cd eams-project

# 3. Give execution permission to the setup script
chmod +x deploy/aws/ec2/setup-ec2.sh

# 4. Run the automated deployment script
./deploy/aws/ec2/setup-ec2.sh
```

### What `setup-ec2.sh` automatically does:
- ✅ Updates system packages & dependencies.
- ✅ Creates **4 GB Swap Space** (vital for JVM memory stability & smooth Docker builds).
- ✅ Installs **Docker Engine & Docker Compose v2**.
- ✅ Creates `.env` file with a cryptographically secure random `JWT_SECRET`.
- ✅ Builds the Spring Boot 3.3 backend & React Vite frontend containers.
- ✅ Starts all 6 services (`PostgreSQL`, `Redis`, `Backend`, `Frontend`, `Prometheus`, `Grafana`).
- ✅ Validates container health and prints your live public URLs!

---

## 🌐 Step 5: Verify Deployment & Access URLs

Once the script finishes, open your browser and access:

| Service | URL | Credentials |
|---|---|---|
| **Frontend Web App** | `http://<YOUR_EC2_PUBLIC_IP>` | See logins below |
| **Spring Boot API** | `http://<YOUR_EC2_PUBLIC_IP>:8080/api/v1` | REST APIs |
| **Swagger UI (OpenAPI)** | `http://<YOUR_EC2_PUBLIC_IP>/swagger-ui/index.html` or `:8080` | Interactive API documentation |
| **Actuator Health Probe** | `http://<YOUR_EC2_PUBLIC_IP>:8080/actuator/health` | System Health (`{"status":"UP"}`) |
| **Grafana Dashboard** | `http://<YOUR_EC2_PUBLIC_IP>:3001` | **User**: `admin` / **Pass**: `admin` |
| **Prometheus** | `http://<YOUR_EC2_PUBLIC_IP>:9090` | Metrics Engine |

### 🔑 Pre-seeded Role-Based Access Control (RBAC) Accounts:
- **Admin**: `admin@company.com` / `Admin@123`
- **Manager**: `manager@company.com` / `Manager@123`
- **Employee**: `john.doe@company.com` / `Employee@123`

---

## 🔒 Step 6 (Optional): Domain & Free SSL Setup (HTTPS)

If you own a custom domain (e.g. `eams.yourdomain.com`):

1. **Point DNS**: Add an **A Record** in your DNS provider (Route53 / GoDaddy / Cloudflare):
   - Name: `eams` (or `@`)
   - Type: `A`
   - Value: `<YOUR_EC2_PUBLIC_IP>`
2. **Install Certbot** on EC2:
   ```bash
   sudo apt-get install -y certbot python3-certbot-nginx
   ```
3. Generate SSL certificate and configure HTTPS auto-renewal:
   ```bash
   sudo certbot --nginx -d eams.yourdomain.com
   ```

---

## 🛠️ Troubleshooting & Maintenance Commands

### Check container status:
```bash
sudo docker compose ps
```

### View live logs for any service:
```bash
# View backend logs
sudo docker compose logs -f backend

# View frontend logs
sudo docker compose logs -f frontend

# View database logs
sudo docker compose logs -f postgres
```

### Restart all services:
```bash
sudo docker compose restart
```

### Pull latest updates & redeploy:
```bash
git pull origin main
sudo docker compose up --build -d
```

### Stop all services:
```bash
sudo docker compose down
```
