#!/bin/bash

# Alternative Promote User to Super Admin Script (Development)
# This script tries different approaches to promote mattiasgebrie@gmail.com

echo "👑 Alternative Promote User to Super Admin (Development)"
echo "======================================================"

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

# User details
TARGET_EMAIL="mattiasgebrie@gmail.com"

echo
echo "🎯 Target User: $TARGET_EMAIL"
echo "🌐 Environment: Development"
echo

echo "🔐 Step 1: Trying different passwords..."
echo

# Try different passwords that might work
PASSWORDS=(
    "M4tt145_T5e"
    "TestPassword123!"
    "Password123!"
    "mattias123"
    "admin123"
)

for password in "${PASSWORDS[@]}"; do
    echo "🔑 Trying password: $password"
    
    LOGIN_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$TARGET_EMAIL\",\"password\":\"$password\"}" \
      -w "HTTPSTATUS:%{http_code}")
    
    HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ SUCCESS! Login worked with password: $password"
        echo "📋 Response: $LOGIN_BODY"
        
        # Extract token and promote
        TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$TOKEN" ]; then
            echo "🔑 Token obtained: ${TOKEN:0:20}..."
            echo "👑 Promoting to SUPER_ADMIN..."
            
            PROMOTE_RESPONSE=$(curl -s -X POST "$DEV_URL/admin/promote/$TARGET_EMAIL" \
              -H "Content-Type: application/json" \
              -H "Authorization: Bearer $TOKEN" \
              -d "{\"role\":\"SUPER_ADMIN\"}" \
              -w "HTTPSTATUS:%{http_code}")
            
            PROMOTE_STATUS=$(echo "$PROMOTE_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
            PROMOTE_BODY=$(echo "$PROMOTE_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
            
            if [ "$PROMOTE_STATUS" -eq 200 ]; then
                echo "🎉 SUCCESS! User promoted to SUPER_ADMIN!"
                echo "✅ Response: $PROMOTE_BODY"
                exit 0
            else
                echo "❌ Promotion failed (Status: $PROMOTE_STATUS)"
                echo "Response: $PROMOTE_BODY"
            fi
        fi
        break
    else
        echo "❌ Failed with password: $password"
    fi
    echo
done

echo
echo "🔐 Step 2: Trying to register the user first..."
echo

# Try to register the user if login failed
REGISTER_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Mattias\",\"lastName\":\"Gebrie\",\"email\":\"$TARGET_EMAIL\",\"password\":\"M4tt145_T5e\",\"confirmPassword\":\"M4tt145_T5e\"}" \
  -w "HTTPSTATUS:%{http_code}")

REGISTER_STATUS=$(echo "$REGISTER_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
REGISTER_BODY=$(echo "$REGISTER_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Register Status: $REGISTER_STATUS"
echo "Register Response: $REGISTER_BODY"
echo

if [ "$REGISTER_STATUS" -eq 200 ] || [ "$REGISTER_STATUS" -eq 400 ]; then
    echo "✅ User registration attempted (might already exist)"
    echo "🔐 Now trying to login with the registered user..."
    echo
    
    # Try login again after registration
    LOGIN_RESPONSE=$(curl -s -X POST "$DEV_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$TARGET_EMAIL\",\"password\":\"M4tt145_T5e\"}" \
      -w "HTTPSTATUS:%{http_code}")
    
    HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ Login successful after registration!"
        TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$TOKEN" ]; then
            echo "👑 Promoting to SUPER_ADMIN..."
            
            PROMOTE_RESPONSE=$(curl -s -X POST "$DEV_URL/admin/promote/$TARGET_EMAIL" \
              -H "Content-Type: application/json" \
              -H "Authorization: Bearer $TOKEN" \
              -d "{\"role\":\"SUPER_ADMIN\"}" \
              -w "HTTPSTATUS:%{http_code}")
            
            PROMOTE_STATUS=$(echo "$PROMOTE_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
            PROMOTE_BODY=$(echo "$PROMOTE_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
            
            if [ "$PROMOTE_STATUS" -eq 200 ]; then
                echo "🎉 SUCCESS! User promoted to SUPER_ADMIN!"
                echo "✅ Response: $PROMOTE_BODY"
            else
                echo "❌ Promotion failed (Status: $PROMOTE_STATUS)"
                echo "Response: $PROMOTE_BODY"
            fi
        fi
    else
        echo "❌ Login still failed after registration"
        echo "Response: $LOGIN_BODY"
    fi
fi

echo
echo "👑 Alternative promotion process complete!"
