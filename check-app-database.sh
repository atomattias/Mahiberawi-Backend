#!/bin/bash

# Check Application Database Script (Development)
# This script checks the database that the application is actually using

echo "🔍 Check Application Database (Development)"
echo "=========================================="

echo
echo "🔍 The application uses internal Railway network, not TCP proxy."
echo "Let's check if we can access the same database the app uses."
echo

# Try to connect using the same connection details as the application
# But we can't access internal Railway network from outside
# So let's check if the application is actually running and creating tables

echo "📊 Checking application health and database status..."
echo

# Check if application is running
echo "1️⃣ Application health check:"
HEALTH_RESPONSE=$(curl -s -X GET "https://web-development-a36e.up.railway.app/api/health")
echo "Health Response: $HEALTH_RESPONSE"

echo

# Check if we can get any database-related info from the app
echo "2️⃣ Checking if application has any database info endpoints:"
DB_INFO_RESPONSE=$(curl -s -X GET "https://web-development-a36e.up.railway.app/api/admin/db-info" \
  -H "Content-Type: application/json" \
  -w "HTTPSTATUS:%{http_code}")

DB_INFO_STATUS=$(echo "$DB_INFO_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
DB_INFO_BODY=$(echo "$DB_INFO_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "DB Info Status: $DB_INFO_STATUS"
echo "DB Info Response: $DB_INFO_BODY"

echo

echo "💡 The issue is clear now:"
echo "1. Application connects to: postgres.railway.internal:5432/railway"
echo "2. We connect to: shuttle.proxy.rlwy.net:53517/railway"
echo "3. These might be different database instances!"
echo

echo "🔧 Solution:"
echo "1. Check Railway logs to see if tables are being created"
echo "2. Restart the application to trigger table creation"
echo "3. Or check if there's a database migration issue"
echo

echo "🔍 Application database check complete!"
