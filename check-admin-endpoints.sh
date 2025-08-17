#!/bin/bash

# Check Admin Endpoints Script (Development)
# This script checks for admin endpoints that might allow direct user promotion

echo "🔍 Check Admin Endpoints (Development)"
echo "====================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "🔍 Checking for admin endpoints that might allow direct user promotion..."
echo

# List of admin endpoints to try
ADMIN_ENDPOINTS=(
    "/admin/promote/mattiasgebrie@gmail.com"
    "/admin/users/mattiasgebrie@gmail.com/promote"
    "/admin/users/promote"
    "/admin/user/promote"
    "/auth/admin/promote"
    "/admin/super-admin"
    "/admin/role/update"
)

echo "🔍 Testing admin endpoints for direct promotion..."
echo

for endpoint in "${ADMIN_ENDPOINTS[@]}"; do
    echo "🔍 Testing: $endpoint"
    
    # Try POST method
    POST_RESPONSE=$(curl -s -X POST "$DEV_URL$endpoint" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"mattiasgebrie@gmail.com\",\"role\":\"SUPER_ADMIN\"}" \
      -w "HTTPSTATUS:%{http_code}")
    
    POST_STATUS=$(echo "$POST_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    POST_BODY=$(echo "$POST_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$POST_STATUS" -eq 200 ]; then
        echo "✅ SUCCESS (POST): $endpoint"
        echo "   Response: $POST_BODY"
    elif [ "$POST_STATUS" -eq 401 ]; then
        echo "🔒 UNAUTHORIZED (POST): $endpoint - needs authentication"
    elif [ "$POST_STATUS" -eq 403 ]; then
        echo "🚫 FORBIDDEN (POST): $endpoint - needs admin privileges"
    elif [ "$POST_STATUS" -eq 404 ]; then
        echo "❌ NOT FOUND (POST): $endpoint"
    else
        echo "❓ UNKNOWN ($POST_STATUS) (POST): $endpoint"
        echo "   Response: $POST_BODY"
    fi
    echo
done

echo "🔍 Checking for password reset endpoints..."
echo

# Try password reset endpoints
PASSWORD_ENDPOINTS=(
    "/auth/forgot-password"
    "/auth/reset-password"
    "/admin/reset-password"
    "/auth/password/reset"
)

for endpoint in "${PASSWORD_ENDPOINTS[@]}"; do
    echo "🔍 Testing: $endpoint"
    
    RESET_RESPONSE=$(curl -s -X POST "$DEV_URL$endpoint" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"mattiasgebrie@gmail.com\"}" \
      -w "HTTPSTATUS:%{http_code}")
    
    RESET_STATUS=$(echo "$RESET_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    RESET_BODY=$(echo "$RESET_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$RESET_STATUS" -eq 200 ]; then
        echo "✅ SUCCESS: $endpoint"
        echo "   Response: $RESET_BODY"
    elif [ "$RESET_STATUS" -eq 404 ]; then
        echo "❌ NOT FOUND: $endpoint"
    else
        echo "❓ UNKNOWN ($RESET_STATUS): $endpoint"
        echo "   Response: $RESET_BODY"
    fi
    echo
done

echo "🔍 Admin endpoints check complete!"
