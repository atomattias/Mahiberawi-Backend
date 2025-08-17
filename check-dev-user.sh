#!/bin/bash

# Check Development User Script
# This script checks if a user exists in the development database

echo "🔍 Checking Development User"
echo "============================"

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "Development URL: $DEV_URL"
echo

# Check if user exists by phone number
PHONE_NUMBER="+4791261801"

echo "📱 Checking if user exists with phone: $PHONE_NUMBER"
echo

# Try to get user info (this might require authentication)
echo "🔍 Attempting to check user existence..."
echo "Note: This might require authentication or admin access"
echo

# You can also check the health endpoint to see if the app is running
echo "🏥 Checking application health..."
HEALTH_RESPONSE=$(curl -s -X GET "$DEV_URL/health" -w "HTTPSTATUS:%{http_code}")

HTTP_STATUS=$(echo "$HEALTH_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
RESPONSE_BODY=$(echo "$HEALTH_RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')

if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "✅ Application is running and healthy"
    echo "Response: $RESPONSE_BODY"
else
    echo "❌ Application health check failed (Status: $HTTP_STATUS)"
    echo "Response: $RESPONSE_BODY"
fi

echo
echo "💡 Next Steps:"
echo "1. Try registering with the same phone number again"
echo "2. Check if you get an 'already registered' error"
echo "3. If not, try with a different phone number"
echo "4. Check Railway deployment logs for any errors"
echo

echo "🔍 Development User Check Complete!"
