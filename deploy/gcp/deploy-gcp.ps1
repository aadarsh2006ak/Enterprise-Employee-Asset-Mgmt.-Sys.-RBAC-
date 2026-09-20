# ==============================================================================
# Enterprise Asset Management System (EAMS) - GCP Cloud Run Deployment Script
# PowerShell Edition for Windows DevOps Engineers
# ==============================================================================

param (
    [Parameter(Mandatory=$true)]
    [string]$GcpProjectId,

    [Parameter(Mandatory=$false)]
    [string]$GcpRegion = "asia-south1",

    [Parameter(Mandatory=$false)]
    [string]$RepoName = "eams-repo"
)

$ErrorActionPreference = "Stop"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "     EAMS Deployment to Google Cloud Run ($GcpRegion)            " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Verify gcloud CLI
try {
    gcloud auth print-access-token >$null
    Write-Host "[✓] Google Cloud SDK authenticated!" -ForegroundColor Green
} catch {
    Write-Error "[!] gcloud is not authenticated. Please run 'gcloud auth login' and 'gcloud config set project $GcpProjectId'."
    exit 1
}

$Registry = "$GcpRegion-docker.pkg.dev/$GcpProjectId/$RepoName"

# 2. Configure Docker for Google Artifact Registry
Write-Host "`n[1/4] Configuring Docker authentication for Google Artifact Registry..." -ForegroundColor Yellow
gcloud auth configure-docker "$GcpRegion-docker.pkg.dev" --quiet

# 3. Build and Push Backend
Write-Host "`n[2/4] Building and pushing Backend..." -ForegroundColor Yellow
$BackendImage = "$Registry/eams-backend:latest"
docker build -t eams-backend:gcp ./eams-backend
docker tag eams-backend:gcp $BackendImage
docker push $BackendImage

# 4. Build and Push Frontend
Write-Host "`n[3/4] Building and pushing Frontend..." -ForegroundColor Yellow
$FrontendImage = "$Registry/eams-frontend:latest"
docker build -t eams-frontend:gcp ./eams-frontend
docker tag eams-frontend:gcp $FrontendImage
docker push $FrontendImage

# 5. Deploy to Cloud Run
Write-Host "`n[4/4] Deploying to Cloud Run..." -ForegroundColor Yellow
gcloud run deploy eams-backend --image $BackendImage --region $GcpRegion --platform managed --allow-unauthenticated --cpu 1 --memory 1Gi --min-instances 1
gcloud run deploy eams-frontend --image $FrontendImage --region $GcpRegion --platform managed --allow-unauthenticated --cpu 0.5 --memory 512Mi --min-instances 1

Write-Host "`n=================================================================" -ForegroundColor Green
Write-Host "   [SUCCESS] GCP Cloud Run Deployment Completed!                 " -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
