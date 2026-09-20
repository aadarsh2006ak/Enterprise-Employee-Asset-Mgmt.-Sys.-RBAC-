#!/usr/bin/env bash
# ==============================================================================
# Enterprise Asset Management System (EAMS) - AWS ECS Fargate Deployment Script
# Bash Edition for Linux/macOS/CI Runners
# ==============================================================================

set -e

AWS_ACCOUNT_ID=${1:-$AWS_ACCOUNT_ID}
AWS_REGION=${2:-"ap-south-1"}
CLUSTER_NAME=${3:-"eams-production-cluster"}
IMAGE_TAG=${4:-"latest"}

if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo "Usage: ./deploy-aws.sh <AWS_ACCOUNT_ID> [AWS_REGION] [CLUSTER_NAME] [IMAGE_TAG]"
    echo "Error: AWS_ACCOUNT_ID is required."
    exit 1
fi

echo "================================================================="
echo "   EAMS Production Deployment to AWS ECS Fargate ($AWS_REGION)   "
echo "================================================================="

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "--> Authenticating Docker with Amazon ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "--> Ensuring ECR Repositories exist..."
for repo in eams-backend eams-frontend; do
    aws ecr describe-repositories --repository-names "$repo" --region "$AWS_REGION" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "$repo" --region "$AWS_REGION" --image-scanning-configuration scanOnPush=true
done

echo "--> Building and pushing Backend..."
BACKEND_IMAGE="${ECR_REGISTRY}/eams-backend:${IMAGE_TAG}"
docker build -t eams-backend:local ./eams-backend
docker tag eams-backend:local "$BACKEND_IMAGE"
docker push "$BACKEND_IMAGE"

echo "--> Building and pushing Frontend..."
FRONTEND_IMAGE="${ECR_REGISTRY}/eams-frontend:${IMAGE_TAG}"
docker build -t eams-frontend:local ./eams-frontend
docker tag eams-frontend:local "$FRONTEND_IMAGE"
docker push "$FRONTEND_IMAGE"

echo "--> Triggering Zero-Downtime ECS Rolling Update..."
aws ecs update-service --cluster "$CLUSTER_NAME" --service eams-backend-service --force-new-deployment --region "$AWS_REGION" || true
aws ecs update-service --cluster "$CLUSTER_NAME" --service eams-frontend-service --force-new-deployment --region "$AWS_REGION" || true

echo "================================================================="
echo "   [SUCCESS] Deployment initiated successfully!                  "
echo "================================================================="
