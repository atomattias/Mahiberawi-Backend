#!/bin/bash

# Final Promote User to Super Admin Script (Development)
# This script promotes mattiasgebrie@gmail.com to SUPER_ADMIN role

echo "👑 Final Promote User to Super Admin (Development)"
echo "================================================"

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

# User details
TARGET_EMAIL="mattiasgebrie@gmail.com"
ADMIN_EMAIL="mattiasgebrie@gmail.com"
ADMIN_PASSWORD="M4tt145&T5e"

echo
echo "🎯 Target User: $TARGET_EMAIL"
echo "🔑 Admin Credentials: $ADMIN_EMAIL"
echo "🔐 Password: $ADMIN_PASSWORD"
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

    # Try different promotion endpoints
    PROMOTION_ENDPOINTS=(
        "/admin/promote/$TARGET_EMAIL"
        "/admin/users/promote"
        "/admin/user/promote"
        "/admin/role/update"
    )

    for endpoint in "${PROMOTION_ENDPOINTS[@]}"; do
        echo "🔍 Trying endpoint: $endpoint"
        
        PROMOTE_RESPONSE=$(curl -s -X POST "$DEV_URL$endpoint" \
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
            echo "👑 You can now access all admin features and manage the system."
            echo "📱 You should see admin features in your Expo Go app now."
            exit 0
        elif [ "$PROMOTE_STATUS" -eq 404 ]; then
            echo "❌ Endpoint not found: $endpoint"
        elif [ "$PROMOTE_STATUS" -eq 403 ]; then
            echo "❌ Permission denied for endpoint: $endpoint"
        else
            echo "❌ Failed to promote user via $endpoint (Status: $PROMOTE_STATUS)"
            echo "Response: $PROMOTE_BODY"
        fi
        echo
    done

    echo "❌ All promotion endpoints failed"
    echo "💡 The user might need to be promoted manually or the endpoints might be different"

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
echo "👑 Final promotion process complete!"
