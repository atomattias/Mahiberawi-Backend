#!/bin/bash

# Promote User to Super Admin Script (Development)
# This script promotes mattiasgebrie@gmail.com to SUPER_ADMIN role

echo "👑 Promote User to Super Admin (Development)"
echo "==========================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

# User details
TARGET_EMAIL="mattiasgebrie@gmail.com"
ADMIN_EMAIL="mattiasgebrie@gmail.com"
ADMIN_PASSWORD="M4tt145_T5e"

echo
echo "🎯 Target User: $TARGET_EMAIL"
echo "🔑 Admin Credentials: $ADMIN_EMAIL"
echo "🌐 Environment: Development"
echo

echo "🔐 Step 1: Authenticating as admin..."
echo

# Login to get admin token
LOGIN_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Login Status: $HTTP_STATUS"
echo "Login Response: $LOGIN_BODY"
echo

if [ "$HTTP_STATUS" -eq 200 ]; then
    # Extract token from response
    TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        echo "❌ Failed to extract token from login response"
        exit 1
    fi
    
    echo "✅ Login successful! Token obtained."
    echo "🔑 Token: ${TOKEN:0:20}..."
    echo

    echo "👑 Step 2: Promoting user to SUPER_ADMIN..."
    echo

    # Promote user to super admin
    PROMOTE_RESPONSE=$(curl -s -X POST "$DEV_URL/admin/promote/$TARGET_EMAIL" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{\"role\":\"SUPER_ADMIN\"}" \
      -w "HTTPSTATUS:%{http_code}")

    PROMOTE_STATUS=$(echo "$PROMOTE_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    PROMOTE_BODY=$(echo "$PROMOTE_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

    echo "Promote Status: $PROMOTE_STATUS"
    echo "Promote Response: $PROMOTE_BODY"
    echo

    if [ "$PROMOTE_STATUS" -eq 200 ]; then
        echo "🎉 SUCCESS! User $TARGET_EMAIL has been promoted to SUPER_ADMIN!"
        echo "✅ The user now has full administrative privileges."
    elif [ "$PROMOTE_STATUS" -eq 404 ]; then
        echo "❌ User not found: $TARGET_EMAIL"
        echo "💡 Make sure the user exists in the system"
    elif [ "$PROMOTE_STATUS" -eq 403 ]; then
        echo "❌ Permission denied"
        echo "💡 Make sure you're logged in as an admin user"
    else
        echo "❌ Failed to promote user (Status: $PROMOTE_STATUS)"
        echo "Response: $PROMOTE_BODY"
    fi

else
    echo "❌ Login failed (Status: $HTTP_STATUS)"
    echo "Response: $LOGIN_BODY"
    echo
    echo "💡 Possible issues:"
    echo "1. User doesn't exist"
    echo "2. Wrong password"
    echo "3. User account is not active"
    echo "4. Email verification required"
fi

echo
echo "👑 Promotion process complete!"
