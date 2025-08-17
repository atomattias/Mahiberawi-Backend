#!/bin/bash

# Check Twilio Configuration Script (Development)
# This script checks if Twilio variables are properly configured

echo "🔍 Check Twilio Configuration (Development)"
echo "==========================================="

# Development environment URL
DEV_URL="https://web-development-a36e.up.railway.app/api"

echo
echo "Development URL: $DEV_URL"
echo

echo "🔍 Checking Twilio configuration..."
echo

# Test endpoints that might reveal Twilio configuration
TEST_ENDPOINTS=(
    "/health"
    "/api/health"
    "/config/sms"
    "/config/twilio"
    "/debug/sms"
    "/admin/config"
)

for endpoint in "${TEST_ENDPOINTS[@]}"; do
    echo "🔍 Testing: $endpoint"
    
    RESPONSE=$(curl -s -X GET "$DEV_URL$endpoint" \
      -H "Content-Type: application/json" \
      -w "HTTPSTATUS:%{http_code}")
    
    HTTP_STATUS=$(echo "$RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed -E 's/HTTPSTATUS:[0-9]*$//')
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ SUCCESS (200): $endpoint"
        echo "   Response: $RESPONSE_BODY"
    elif [ "$HTTP_STATUS" -eq 404 ]; then
        echo "❌ NOT FOUND (404): $endpoint"
    else
        echo "❓ UNKNOWN ($HTTP_STATUS): $endpoint"
        echo "   Response: $RESPONSE_BODY"
    fi
    echo
done

echo "📋 Required Twilio Variables for Development:"
echo "============================================="
echo "TWILIO_ACCOUNT_SID_DEV=<your-account-sid>"
echo "TWILIO_AUTH_TOKEN_DEV=<your-auth-token>"
echo "TWILIO_FROM_NUMBER_DEV=<your-twilio-number>"
echo "SMS_ENABLED_DEV=true"
echo

echo "🔍 Checking Railway variables..."
railway variables

echo
echo "💡 Troubleshooting Steps:"
echo "1. Verify all Twilio variables are set in Railway"
echo "2. Check if SMS_ENABLED_DEV=true"
echo "3. Verify Twilio credentials are valid"
echo "4. Check Railway logs for SMS errors"
echo

echo "🔍 Twilio Configuration Check Complete!"
