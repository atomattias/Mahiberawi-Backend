#!/bin/bash

# JWT Secret Generator Script
# This script generates secure JWT secrets for all environments

echo "🔐 JWT Secret Generator"
echo "======================"

echo
echo "Generating secure JWT secrets for all environments..."
echo

# Generate Development JWT Secret
DEV_JWT_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)
DEV_REFRESH_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)

# Generate Staging JWT Secret
STAGING_JWT_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)
STAGING_REFRESH_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)

# Generate Production JWT Secret
PROD_JWT_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)
PROD_REFRESH_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)

echo "✅ Generated secure JWT secrets for all environments!"
echo

echo "🔧 DEVELOPMENT Environment:"
echo "=========================="
echo "JWT_SECRET_DEV=$DEV_JWT_SECRET"
echo "JWT_REFRESH_SECRET_DEV=$DEV_REFRESH_SECRET"
echo

echo "🧪 STAGING Environment:"
echo "======================"
echo "JWT_SECRET_STAGING=$STAGING_JWT_SECRET"
echo "JWT_REFRESH_SECRET_STAGING=$STAGING_REFRESH_SECRET"
echo

echo "🚀 PRODUCTION Environment:"
echo "========================="
echo "JWT_SECRET=$PROD_JWT_SECRET"
echo "JWT_REFRESH_SECRET=$PROD_REFRESH_SECRET"
echo

echo "📋 Copy these secrets to your Railway environment variables:"
echo "============================================================"
echo

echo "For Development Railway Project:"
echo "JWT_SECRET_DEV=$DEV_JWT_SECRET"
echo "JWT_REFRESH_SECRET_DEV=$DEV_REFRESH_SECRET"
echo

echo "For Staging Railway Project:"
echo "JWT_SECRET_STAGING=$STAGING_JWT_SECRET"
echo "JWT_REFRESH_SECRET_STAGING=$STAGING_REFRESH_SECRET"
echo

echo "For Production Railway Project:"
echo "JWT_SECRET=$PROD_JWT_SECRET"
echo "JWT_REFRESH_SECRET=$PROD_REFRESH_SECRET"
echo

echo "⚠️  IMPORTANT SECURITY NOTES:"
echo "============================"
echo "1. Keep these secrets secure and private"
echo "2. Never commit them to version control"
echo "3. Use different secrets for each environment"
echo "4. Store them only in Railway environment variables"
echo "5. If compromised, generate new secrets immediately"
echo

echo "🔐 JWT Secret Generation Complete!"
