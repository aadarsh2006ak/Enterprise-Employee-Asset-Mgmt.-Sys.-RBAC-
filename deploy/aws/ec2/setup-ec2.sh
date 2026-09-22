#!/bin/bash
# ==============================================================================
# Enterprise Asset Management System (EAMS) — AWS EC2 One-Click Setup Script
# Works on Ubuntu 22.04 LTS / Ubuntu 24.04 LTS
# ==============================================================================

set -e

echo "================================================================="
echo "   🚀 Starting EAMS Automated Deployment on AWS EC2              "
echo "================================================================="

# 1. Update system packages
echo -e "\n[1/6] Updating system packages..."
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y curl git ufw jq apt-transport-https ca-certificates gnupg lsb-release

# 2. Setup 4GB Swap Memory (Prevents Out-Of-Memory during builds on t2.micro / t3.small)
echo -e "\n[2/6] Configuring Swap Space (4GB for safe builds & JVM)..."
if [ ! -f /swapfile ]; then
    sudo fallocate -l 4G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    echo "Swap space successfully configured."
else
    echo "Swap space already exists."
fi

# 3. Install Docker Engine & Docker Compose Plugin
echo -e "\n[3/6] Installing Docker & Docker Compose..."
if ! command -v docker &> /dev/null; then
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    # Allow current user to run docker without sudo
    sudo usermod -aG docker $USER
    sudo systemctl enable docker
    sudo systemctl start docker
    echo "Docker installed successfully."
else
    echo "Docker is already installed."
fi

# 4. Prepare Environment Configuration (.env)
echo -e "\n[4/6] Setting up environment configuration..."
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        cp .env.example .env
        # Generate secure random 64-char JWT secret
        RANDOM_JWT=$(openssl rand -hex 32)
        sed -i "s/YOUR_JWT_SECRET_CODE/$RANDOM_JWT/g" .env
        echo "Created .env with auto-generated secure JWT secret."
    else
        echo "Warning: .env.example not found. Please ensure .env exists."
    fi
else
    echo ".env file already exists."
fi

# 5. Build and Launch Containers via Docker Compose
echo -e "\n[5/6] Building and Starting EAMS Stack (Postgres, Redis, Backend, Frontend, Prometheus, Grafana)..."
sudo docker compose down || true
sudo docker compose up --build -d

# 6. Verify Service Health
echo -e "\n[6/6] Verifying Running Services..."
sleep 15
sudo docker compose ps

# Fetch Public IP
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 || curl -s ifconfig.me || echo "your-ec2-ip")

echo -e "\n================================================================="
echo "   ✅ EAMS SUCCESSFULLY DEPLOYED TO AWS EC2!                     "
echo "================================================================="
echo "   🌐 Frontend Web App:     http://$PUBLIC_IP"
echo "   🔌 Spring Boot Backend:  http://$PUBLIC_IP:8080/api/v1"
echo "   📖 Swagger API Docs:     http://$PUBLIC_IP:8080/swagger-ui/index.html"
echo "   📊 Grafana Monitoring:   http://$PUBLIC_IP:3001 (admin / admin)"
echo "   📈 Prometheus Metrics:   http://$PUBLIC_IP:9090"
echo "================================================================="
echo "Default Login Credentials:"
echo "   👑 Admin User:      admin@company.com  /  Admin@123"
echo "   👔 Manager User:    manager@company.com / Manager@123"
echo "   💻 Employee User:   john.doe@company.com / Employee@123"
echo "================================================================="
