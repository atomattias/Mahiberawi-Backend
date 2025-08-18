#!/bin/bash

# Direct Promotion Script for mattiasgebrie@gmail.com
echo "👑 Direct Promotion of mattiasgebrie@gmail.com to Super Admin"
echo "============================================================="

DEV_URL="https://web-development-a36e.up.railway.app/api"
EMAIL="mattiasgebrie@gmail.com"
PASSWORD="M4tt145_T5e"

echo "🎯 Target: $EMAIL"
echo "🌐 Environment: Development"
echo

# Step 1: Login to get token
echo "🔐 Step 1: Authenticating..."
LOGIN_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Login Status: $HTTP_STATUS"
echo "Login Response: $LOGIN_BODY"
echo

if [ "$HTTP_STATUS" -eq 200 ]; then
    # Extract token from response
    TOKEN=$(echo "$LOGIN_BODY" | jq -r '.token')
    
    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo "❌ Failed to extract token from login response"
        echo "💡 Trying alternative token extraction..."
        TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    fi
    
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
        "/admin/promote/$EMAIL"
        "/admin/users/promote"
        "/admin/user/promote"
        "/admin/role/update"
        "/admin/promote-to-super-admin/$EMAIL"
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
            echo "🎉 SUCCESS! User $EMAIL has been promoted to SUPER_ADMIN!"
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
    echo "💡 The secure promotion endpoint may not be deployed yet"
    echo "💡 Try setting up the environment variable and deploying again"

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
