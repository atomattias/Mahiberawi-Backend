#!/bin/bash

# Staging Environment Deployment Script
# This script helps deploy to the staging environment

echo "=== Deploying to Staging Environment ==="
echo "======================================="

# Configuration
STAGING_BRANCH="staging"
STAGING_PROJECT_NAME="Mahiberawi-Backend-Staging"
STAGING_RAILWAY_URL="https://mahiberawi-backend-staging.up.railway.app"

echo
echo "Target Environment: STAGING"
echo "Branch: $STAGING_BRANCH"
echo "Project: $STAGING_PROJECT_NAME"
echo "URL: $STAGING_RAILWAY_URL"

# Check if we're on the staging branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$STAGING_BRANCH" ]; then
    echo
    echo "⚠️  Warning: You're currently on branch '$CURRENT_BRANCH'"
    echo "   Staging deployment should be from '$STAGING_BRANCH' branch"
    read -p "   Do you want to switch to $STAGING_BRANCH? (y/n): " SWITCH_BRANCH
    
    if [ "$SWITCH_BRANCH" = "y" ] || [ "$SWITCH_BRANCH" = "Y" ]; then
        echo "   Switching to $STAGING_BRANCH..."
        git checkout $STAGING_BRANCH
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

# Update Railway configuration for staging
echo
echo "🔧 Updating Railway configuration for staging..."
cp railway-staging.json railway.json
git add railway.json
git commit -m "Update Railway config for staging deployment" || echo "No changes to commit"

# Confirm staging deployment
echo
echo "⚠️  STAGING DEPLOYMENT CONFIRMATION"
echo "   This will deploy to the staging environment for testing"
read -p "   Proceed with staging deployment? (y/n): " CONFIRM

if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "❌ Staging deployment cancelled"
    exit 1
fi

# Push to staging branch
echo
echo "📤 Pushing to $STAGING_BRANCH branch..."
git push origin $STAGING_BRANCH

if [ $? -eq 0 ]; then
    echo "✅ Successfully pushed to $STAGING_BRANCH"
    echo
    echo "🚀 Railway will automatically deploy from the $STAGING_BRANCH branch"
    echo "   Check deployment status at: https://railway.app/project/[your-staging-project-id]"
    echo
    echo "🔗 Staging URL: $STAGING_RAILWAY_URL"
    echo "📊 Health Check: $STAGING_RAILWAY_URL/api/health"
    echo
    echo "🧪 Test your changes in staging before deploying to production"
    echo
    echo "🔧 Staging settings applied:"
    echo "   - Spring Profile: staging"
    echo "   - Memory: 512MB max, 256MB initial"
    echo "   - Restart retries: 10"
else
    echo "❌ Failed to push to $STAGING_BRANCH"
    exit 1
fi

echo
echo "=== Staging Deployment Complete ==="
