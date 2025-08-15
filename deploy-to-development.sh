#!/bin/bash

# Development Environment Deployment Script
# This script helps deploy to the development environment

echo "=== Deploying to Development Environment ==="
echo "==========================================="

# Configuration
DEV_BRANCH="development"
DEV_PROJECT_NAME="Mahiberawi-Backend-Dev"
DEV_RAILWAY_URL="https://mahiberawi-backend-dev.up.railway.app"

echo
echo "Target Environment: DEVELOPMENT"
echo "Branch: $DEV_BRANCH"
echo "Project: $DEV_PROJECT_NAME"
echo "URL: $DEV_RAILWAY_URL"

# Check if we're on the development branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$DEV_BRANCH" ]; then
    echo
    echo "⚠️  Warning: You're currently on branch '$CURRENT_BRANCH'"
    echo "   Development deployment should be from '$DEV_BRANCH' branch"
    read -p "   Do you want to switch to $DEV_BRANCH? (y/n): " SWITCH_BRANCH
    
    if [ "$SWITCH_BRANCH" = "y" ] || [ "$SWITCH_BRANCH" = "Y" ]; then
        echo "   Switching to $DEV_BRANCH..."
        git checkout $DEV_BRANCH
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

# Update Railway configuration for development
echo
echo "🔧 Updating Railway configuration for development..."
cp railway-development.json railway.json
git add railway.json
git commit -m "Update Railway config for development deployment" || echo "No changes to commit"

# Push to development branch
echo
echo "📤 Pushing to $DEV_BRANCH branch..."
git push origin $DEV_BRANCH

if [ $? -eq 0 ]; then
    echo "✅ Successfully pushed to $DEV_BRANCH"
    echo
    echo "🚀 Railway will automatically deploy from the $DEV_BRANCH branch"
    echo "   Check deployment status at: https://railway.app/project/[your-dev-project-id]"
    echo
    echo "🔗 Development URL: $DEV_RAILWAY_URL"
    echo "📊 Health Check: $DEV_RAILWAY_URL/api/health"
    echo
    echo "🔧 Development settings applied:"
    echo "   - Spring Profile: development"
    echo "   - Memory: 256MB max, 128MB initial"
    echo "   - Restart retries: 5"
else
    echo "❌ Failed to push to $DEV_BRANCH"
    exit 1
fi

echo
echo "=== Development Deployment Complete ==="
