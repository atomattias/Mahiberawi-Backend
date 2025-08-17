#!/bin/bash

# Check User Exists Script (Development)
# This script checks if mattiasgebrie@gmail.com exists and their current role

echo "🔍 Check User Exists (Development)"
echo "================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

# User details
TARGET_EMAIL="mattiasgebrie@gmail.com"

echo
echo "🎯 Checking user: $TARGET_EMAIL"
echo "🌐 Environment: Development"
echo

echo "🔍 Step 1: Checking if user exists..."
echo

# Try to get user info (this might require authentication)
USER_RESPONSE=$(curl -s -X GET "$DEV_URL/auth/user/email/$TARGET_EMAIL" \
  -H "Content-Type: application/json" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$USER_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
USER_BODY=$(echo "$USER_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "User Check Status: $HTTP_STATUS"
echo "User Check Response: $USER_BODY"
echo

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "✅ User exists!"
    echo "📋 User details: $USER_BODY"
elif [ "$HTTP_STATUS" -eq 404 ]; then
    echo "❌ User not found: $TARGET_EMAIL"
    echo "💡 The user might not be registered yet"
elif [ "$HTTP_STATUS" -eq 403 ]; then
    echo "🔒 Access denied - endpoint requires authentication"
    echo "💡 We need to authenticate first to check user details"
else
    echo "❓ Unknown response (Status: $HTTP_STATUS)"
    echo "Response: $USER_BODY"
fi

echo
echo "🔍 Step 2: Trying alternative endpoints..."
echo

# Try different endpoints that might work without auth
ENDPOINTS=(
    "/admin/users/email/$TARGET_EMAIL"
    "/users/email/$TARGET_EMAIL"
    "/auth/users/email/$TARGET_EMAIL"
)

for endpoint in "${ENDPOINTS[@]}"; do
    echo "🔍 Testing: $endpoint"
    
    RESPONSE=$(curl -s -X GET "$DEV_URL$endpoint" \
      -H "Content-Type: application/json" \
      -w "HTTPSTATUS:%{http_code}")
    
    STATUS=$(echo "$RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    BODY=$(echo "$RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$STATUS" -eq 200 ]; then
        echo "✅ SUCCESS: $endpoint"
        echo "   Response: $BODY"
    elif [ "$STATUS" -eq 404 ]; then
        echo "❌ NOT FOUND: $endpoint"
    elif [ "$STATUS" -eq 403 ]; then
        echo "🔒 FORBIDDEN: $endpoint"
    else
        echo "❓ UNKNOWN ($STATUS): $endpoint"
    fi
    echo
done

echo "🔍 User check complete!"
