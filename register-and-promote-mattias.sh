#!/bin/bash

# Register and Promote User Script (Development)
# This script registers mattiasgebrie@gmail.com with correct password and promotes to SUPER_ADMIN

echo "👑 Register and Promote User (Development)"
echo "========================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

# User details
TARGET_EMAIL="mattiasgebrie@gmail.com"
PASSWORD="Mattias&T5e"

echo
echo "🎯 Target User: $TARGET_EMAIL"
echo "🔐 Password: $PASSWORD"
echo "🌐 Environment: Development"
echo

echo "📝 Step 1: Registering user with correct password..."
echo

# Register the user
REGISTER_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Mattias\",\"lastName\":\"Gebrie\",\"email\":\"$TARGET_EMAIL\",\"password\":\"$PASSWORD\",\"confirmPassword\":\"$PASSWORD\"}" \
  -w "HTTPSTATUS:%{http_code}")

REGISTER_STATUS=$(echo "$REGISTER_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
REGISTER_BODY=$(echo "$REGISTER_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Register Status: $REGISTER_STATUS"
echo "Register Response: $REGISTER_BODY"
echo

if [ "$REGISTER_STATUS" -eq 200 ]; then
    echo "✅ User registered successfully!"
elif [ "$REGISTER_STATUS" -eq 400 ]; then
    echo "⚠️  User might already exist or validation failed"
    echo "📋 Response: $REGISTER_BODY"
else
    echo "❌ Registration failed (Status: $REGISTER_STATUS)"
    echo "Response: $REGISTER_BODY"
fi

echo
echo "🔐 Step 2: Logging in with the registered user..."
echo

# Login to get token
LOGIN_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TARGET_EMAIL\",\"password\":\"$PASSWORD\"}" \
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

    echo "👑 Step 3: Promoting user to SUPER_ADMIN..."
    echo

    # Try different promotion endpoints
    PROMOTION_ENDPOINTS=(
        "/admin/promote/$TARGET_EMAIL"
        "/admin/users/promote"
        "/admin/user/promote"
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
    echo "1. Email verification required"
    echo "2. Account not activated"
    echo "3. Password encoding issue"
    echo "4. Authentication system problem"
fi

echo
echo "👑 Register and promote process complete!"
