#!/bin/bash

# Remove User Script (Development)
# This script removes a user from the development database

echo "🗑️ Remove User (Development)"
echo "============================"

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "Development URL: $DEV_URL"
echo

# Get phone number
read -p "Enter the phone number to remove: " PHONE_NUMBER

if [ -z "$PHONE_NUMBER" ]; then
    echo "❌ Phone number is required"
    exit 1
fi

echo
echo "🗑️ Removing user with phone: $PHONE_NUMBER"
echo

# First, let's try to find the user
echo "🔍 Checking if user exists..."
FIND_RESPONSE=$(curl -s -X GET "$DEV_URL/auth/user/phone/$PHONE_NUMBER" \
  -H "Content-Type: application/json" \
  -w "HTTPSTATUS:%{http_code}")

FIND_STATUS=$(echo "$FIND_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
FIND_BODY=$(echo "$FIND_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

echo "Find User Status: $FIND_STATUS"
echo "Find User Response: $FIND_BODY"
echo

# Try different removal endpoints
REMOVAL_ENDPOINTS=(
    "/admin/users/phone/$PHONE_NUMBER"
    "/admin/users/delete"
    "/auth/user/delete"
    "/admin/delete-user"
)

echo "🗑️ Trying different removal endpoints..."
echo

for endpoint in "${REMOVAL_ENDPOINTS[@]}"; do
    echo "🔍 Testing: $endpoint"
    
    # Try DELETE method first
    DELETE_RESPONSE=$(curl -s -X DELETE "$DEV_URL$endpoint" \
      -H "Content-Type: application/json" \
      -w "HTTPSTATUS:%{http_code}")
    
    DELETE_STATUS=$(echo "$DELETE_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    DELETE_BODY=$(echo "$DELETE_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$DELETE_STATUS" -eq 200 ]; then
        echo "✅ SUCCESS (200): $endpoint"
        echo "   Response: $DELETE_BODY"
        echo "🎉 User removed successfully!"
        break
    elif [ "$DELETE_STATUS" -eq 404 ]; then
        echo "❌ NOT FOUND (404): $endpoint"
    elif [ "$DELETE_STATUS" -eq 403 ]; then
        echo "🔒 FORBIDDEN (403): $endpoint - might require admin auth"
        
        # Try with POST method for this endpoint
        echo "   🔄 Trying POST method..."
        POST_RESPONSE=$(curl -s -X POST "$DEV_URL$endpoint" \
          -H "Content-Type: application/json" \
          -d "{\"phoneNumber\":\"$PHONE_NUMBER\"}" \
          -w "HTTPSTATUS:%{http_code}")
        
        POST_STATUS=$(echo "$POST_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
        POST_BODY=$(echo "$POST_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
        
        if [ "$POST_STATUS" -eq 200 ]; then
            echo "✅ SUCCESS (200): $endpoint (POST)"
            echo "   Response: $POST_BODY"
            echo "🎉 User removed successfully!"
            break
        else
            echo "   ❌ POST also failed ($POST_STATUS): $POST_BODY"
        fi
    else
        echo "❓ UNKNOWN ($DELETE_STATUS): $endpoint"
        echo "   Response: $DELETE_BODY"
    fi
    echo
done

echo
echo "💡 Manual Removal Options:"
echo "1. Use Railway CLI to access the database directly"
echo "2. Use a database management tool"
echo "3. Check if there's an admin panel in your app"
echo

echo "🗑️ User Removal Complete!"
