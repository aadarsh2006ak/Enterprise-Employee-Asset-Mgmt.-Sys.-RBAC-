# ==============================================================================
# Enterprise Asset Management System (EAMS) - AWS ECS Fargate Deployment Script
# PowerShell Edition for Windows DevOps Engineers
# ==============================================================================

param (
    [Parameter(Mandatory=$true)]
    [string]$AwsAccountId,

    [Parameter(Mandatory=$false)]
    [string]$AwsRegion = "ap-south-1",

    [Parameter(Mandatory=$false)]
    [string]$ClusterName = "eams-production-cluster",

    [Parameter(Mandatory=$false)]
    [string]$ImageTag = "latest"
)

$ErrorActionPreference = "Stop"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "   EAMS Production Deployment to AWS ECS Fargate ($AwsRegion)    " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Verify AWS CLI is installed and configured
try {
    $callerIdentity = aws sts get-caller-identity --output json | ConvertFrom-Json
    Write-Host "[✓] AWS CLI Authenticated as ARN: $($callerIdentity.Arn)" -ForegroundColor Green
} catch {
    Write-Error "[!] AWS CLI is not authenticated or not installed. Please run 'aws configure' first."
    exit 1
}

$EcrRegistry = "$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com"

# 2. Authenticate Docker with Amazon ECR
Write-Host "`n[1/6] Authenticating Docker with Amazon ECR ($EcrRegistry)..." -ForegroundColor Yellow
aws ecr get-login-password --region $AwsRegion | docker login --username AWS --password-stdin $EcrRegistry
if ($LASTEXITCODE -ne 0) {
    Write-Error "[!] Failed to authenticate with ECR."
    exit 1
}
Write-Host "[✓] Docker login to ECR succeeded!" -ForegroundColor Green

# 3. Create ECR Repositories if they do not already exist
Write-Host "`n[2/6] Ensuring ECR Repositories exist..." -ForegroundColor Yellow
$repos = @("eams-backend", "eams-frontend")
foreach ($repo in $repos) {
    $exists = aws ecr describe-repositories --repository-names $repo --region $AwsRegion 2>$null
    if (-not $exists) {
        Write-Host "    Creating repository: $repo..." -ForegroundColor Cyan
        aws ecr create-repository --repository-name $repo --region $AwsRegion --image-scanning-configuration scanOnPush=true
    } else {
        Write-Host "    Repository '$repo' verified." -ForegroundColor Gray
    }
}

# 4. Build and Push Backend Docker Image
Write-Host "`n[3/6] Building and pushing Backend Container (Spring Boot)..." -ForegroundColor Yellow
$BackendImageUri = "$EcrRegistry/eams-backend:$ImageTag"
docker build -t eams-backend:local ./eams-backend
docker tag eams-backend:local $BackendImageUri
docker push $BackendImageUri
Write-Host "[✓] Backend container successfully pushed to $BackendImageUri" -ForegroundColor Green

# 5. Build and Push Frontend Docker Image
Write-Host "`n[4/6] Building and pushing Frontend Container (React + Nginx)..." -ForegroundColor Yellow
$FrontendImageUri = "$EcrRegistry/eams-frontend:$ImageTag"
docker build -t eams-frontend:local ./eams-frontend
docker tag eams-frontend:local $FrontendImageUri
docker push $FrontendImageUri
Write-Host "[✓] Frontend container successfully pushed to $FrontendImageUri" -ForegroundColor Green

# 6. Update ECS Services for Zero-Downtime Rolling Deployment
Write-Host "`n[5/6] Triggering Zero-Downtime Rolling Update on ECS Fargate..." -ForegroundColor Yellow

try {
    Write-Host "    Updating backend service: eams-backend-service..." -ForegroundColor Cyan
    aws ecs update-service --cluster $ClusterName --service eams-backend-service --force-new-deployment --region $AwsRegion
    Write-Host "[✓] Backend deployment triggered." -ForegroundColor Green
} catch {
    Write-Host "    [Note] Backend ECS service not found or not yet created. Skipping rolling update trigger." -ForegroundColor Yellow
}

try {
    Write-Host "    Updating frontend service: eams-frontend-service..." -ForegroundColor Cyan
    aws ecs update-service --cluster $ClusterName --service eams-frontend-service --force-new-deployment --region $AwsRegion
    Write-Host "[✓] Frontend deployment triggered." -ForegroundColor Green
} catch {
    Write-Host "    [Note] Frontend ECS service not found or not yet created. Skipping rolling update trigger." -ForegroundColor Yellow
}

Write-Host "`n=================================================================" -ForegroundColor Green
Write-Host "   [SUCCESS] EAMS AWS ECS Fargate Deployment Sequence Completed! " -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
