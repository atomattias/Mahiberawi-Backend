#!/bin/bash

# Production Environment Deployment Script
# This script helps deploy to the production environment

echo "=== Deploying to Production Environment ==="
echo "==========================================="

# Configuration
PROD_BRANCH="production"
PROD_PROJECT_NAME="Mahiberawi-Backend"
PROD_RAILWAY_URL="https://mahiberawi-backend-production.up.railway.app"

echo
echo "Target Environment: PRODUCTION"
echo "Branch: $PROD_BRANCH"
echo "Project: $PROD_PROJECT_NAME"
echo "URL: $PROD_RAILWAY_URL"

# Safety check - confirm production deployment
echo
echo "⚠️  WARNING: You are about to deploy to PRODUCTION"
echo "   This will affect live users and data"
read -p "   Are you sure you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "❌ Production deployment cancelled"
    exit 1
fi

# Check if we're on the production branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$PROD_BRANCH" ]; then
    echo
    echo "⚠️  Warning: You're currently on branch '$CURRENT_BRANCH'"
    echo "   Production deployment should be from '$PROD_BRANCH' branch"
    read -p "   Do you want to switch to $PROD_BRANCH? (y/n): " SWITCH_BRANCH
    
    if [ "$SWITCH_BRANCH" = "y" ] || [ "$SWITCH_BRANCH" = "Y" ]; then
        echo "   Switching to $PROD_BRANCH..."
        git checkout $PROD_BRANCH
    else
        echo "   Continuing with current branch..."
    fi
fi

# Check for uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    echo
    echo "⚠️  Warning: You have uncommitted changes"
    git status --short
    read -p "   Do you want to commit these changes? (y/n): " COMMIT_CHANGES
    
    if [ "$COMMIT_CHANGES" = "y" ] || [ "$COMMIT_CHANGES" = "Y" ]; then
        echo "   Committing changes..."
        git add .
        read -p "   Enter commit message: " COMMIT_MESSAGE
        git commit -m "$COMMIT_MESSAGE"
    fi
fi

# Update Railway configuration for production
echo
echo "🔧 Updating Railway configuration for production..."
cp railway-production.json railway.json
git add railway.json
git commit -m "Update Railway config for production deployment" || echo "No changes to commit"

# Final confirmation
echo
echo "🔍 Final Deployment Check:"
echo "   Branch: $(git branch --show-current)"
echo "   Last commit: $(git log -1 --oneline)"
echo "   Remote: origin/$PROD_BRANCH"
read -p "   Proceed with production deployment? (yes/no): " FINAL_CONFIRM

if [ "$FINAL_CONFIRM" != "yes" ]; then
    echo "❌ Production deployment cancelled"
    exit 1
fi

# Push to production branch
echo
echo "📤 Pushing to $PROD_BRANCH branch..."
git push origin $PROD_BRANCH

if [ $? -eq 0 ]; then
    echo "✅ Successfully pushed to $PROD_BRANCH"
    echo
    echo "🚀 Railway will automatically deploy from the $PROD_BRANCH branch"
    echo "   Check deployment status at: https://railway.app/project/[your-prod-project-id]"
    echo
    echo "🔗 Production URL: $PROD_RAILWAY_URL"
    echo "📊 Health Check: $PROD_RAILWAY_URL/api/health"
    echo
    echo "⚠️  Monitor the deployment logs for any issues"
    echo
    echo "🔧 Production settings applied:"
    echo "   - Spring Profile: production"
    echo "   - Memory: 1024MB max, 512MB initial"
    echo "   - Restart retries: 10"
else
    echo "❌ Failed to push to $PROD_BRANCH"
    exit 1
fi

echo
echo "=== Production Deployment Complete ==="
